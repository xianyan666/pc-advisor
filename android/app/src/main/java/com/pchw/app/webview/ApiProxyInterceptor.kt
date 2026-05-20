package com.pchw.app.webview

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

class ApiProxyWebViewClient(
    private val context: Context,
    private val serverBaseUrl: String
) : WebViewClient() {

    private val apiProxyJs: String =
        loadProxyScript().replace("__SERVER_BASE__", serverBaseUrl)

    /**
     * Read index.html from assets, remove crossorigin attrs,
     * inject API proxy script, return the modified HTML.
     */
    fun buildIndexHtml(): String {
        val html = context.assets.open("www/index.html").bufferedReader().use { it.readText() }
        return html
            .replace("crossorigin ", "")
            .replace("<head>", "<head>\n<script>$apiProxyJs</script>")
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url
        val host = url.host
        val path = url.path ?: ""

        // Only intercept requests to our virtual localhost origin
        if (host != "localhost") return null

        return when {
            // Static assets
            path.startsWith("/assets/") -> loadFromAssets("www$path")
            path == "/favicon.ico" -> loadFromAssets("www/favicon.ico")

            // Product images — bundled locally
            path.startsWith("/images/products/") -> loadFromAssets("www$path")

            // Evaluation images — proxy to real backend
            path.startsWith("/images/evaluations/") ->
                proxyToServer("$serverBaseUrl$path")

            // SPA fallback: Vue router paths → return index.html
            request.isForMainFrame -> {
                val html = buildIndexHtml()
                WebResourceResponse("text/html", "UTF-8",
                    ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)))
            }

            else -> null
        }
    }

    private fun loadFromAssets(assetPath: String): WebResourceResponse? {
        return try {
            val stream = context.assets.open(assetPath)
            val mime = when {
                assetPath.endsWith(".html") -> "text/html"
                assetPath.endsWith(".js") -> "application/javascript"
                assetPath.endsWith(".css") -> "text/css"
                assetPath.endsWith(".webp") -> "image/webp"
                assetPath.endsWith(".jpg") || assetPath.endsWith(".jpeg") -> "image/jpeg"
                assetPath.endsWith(".png") -> "image/png"
                assetPath.endsWith(".ico") -> "image/x-icon"
                else -> "application/octet-stream"
            }
            WebResourceResponse(mime, "UTF-8", stream)
        } catch (e: Exception) {
            null
        }
    }

    private fun proxyToServer(targetUrl: String): WebResourceResponse? {
        return try {
            val conn = URL(targetUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val mime = conn.contentType ?: "application/octet-stream"
            val body = conn.inputStream.readBytes()
            conn.disconnect()
            WebResourceResponse(mime.split(";")[0].trim(), "UTF-8",
                ByteArrayInputStream(body))
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun loadProxyScript(): String {
            return """
(function() {
    var BASE = '__SERVER_BASE__';

    function rewriteUrl(url) {
        if (typeof url === 'string') {
            if (url.startsWith('/api/') || url.startsWith('/images/evaluations/')) {
                return BASE + url;
            }
        }
        return url;
    }

    var origXhrOpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
        arguments[1] = rewriteUrl(url);
        return origXhrOpen.apply(this, arguments);
    };

    var origFetch = window.fetch;
    window.fetch = function(input, init) {
        if (typeof input === 'string') {
            input = rewriteUrl(input);
        } else if (input instanceof Request) {
            var newUrl = rewriteUrl(input.url);
            if (newUrl !== input.url) {
                input = new Request(newUrl, input);
            }
        }
        return origFetch.call(this, input, init);
    };

    var origSrcDesc = Object.getOwnPropertyDescriptor(HTMLImageElement.prototype, 'src');
    if (origSrcDesc && origSrcDesc.set) {
        Object.defineProperty(HTMLImageElement.prototype, 'src', {
            get: origSrcDesc.get,
            set: function(value) {
                origSrcDesc.set.call(this, rewriteUrl(value));
            },
            configurable: true
        });
    }

    console.log('[PC Advisor] API proxy installed, base: ' + BASE);
})();
""".trimIndent()
        }
    }
}
