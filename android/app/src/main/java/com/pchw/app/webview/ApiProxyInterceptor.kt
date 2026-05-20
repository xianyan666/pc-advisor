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

    private val apiProxyJs: String by lazy {
        loadProxyScript().replace("__SERVER_BASE__", serverBaseUrl)
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val urlString = request.url.toString()

        // Case 1: Main frame loading index.html — inject proxy + rewrite paths
        if (request.isForMainFrame && (urlString.endsWith("index.html") || urlString.endsWith("www/"))) {
            return injectAndRewriteHtml(urlString)
        }

        // Case 2: file:///favicon.ico — serve from assets
        if (urlString.startsWith("file://") && urlString.endsWith("/favicon.ico")) {
            return loadFromAssets("www/favicon.ico")
        }

        // Case 3: /images/evaluations/* — proxy to backend (user uploaded images)
        if (urlString.contains("/images/evaluations/")) {
            val imgPath = urlString.substringAfterLast("/images/evaluations/")
            if (imgPath.isNotBlank()) {
                return proxyToServer("$serverBaseUrl/images/evaluations/$imgPath")
            }
        }

        // Case 4: SPA fallback — vue router paths without file extension
        if (urlString.startsWith("file://") && urlString.contains("android_asset/www") && request.isForMainFrame) {
            val afterWww = urlString.substringAfter("www/")
            if (afterWww.isNotBlank() && !afterWww.contains(".")) {
                return injectAndRewriteHtml(urlString.substringBefore("www/") + "www/index.html")
            }
        }

        return null
    }

    /**
     * Read index.html from assets, inject the API proxy script,
     * rewrite absolute paths to relative paths, and remove crossorigin attrs.
     */
    private fun injectAndRewriteHtml(urlString: String): WebResourceResponse? {
        return try {
            val assetPath = urlString.substringAfter("android_asset/").ifBlank { "www/index.html" }
            val inputStream = context.assets.open(assetPath)
            val html = inputStream.bufferedReader().use { it.readText() }

            var modifiedHtml = html
                // Rewrite absolute paths to relative for local loading
                .replace("href=\"/assets/", "href=\"assets/")
                .replace("src=\"/assets/", "src=\"assets/")
                .replace("href=\"/favicon.ico\"", "href=\"favicon.ico\"")
                .replace("href=\"/images/products/", "href=\"images/products/")
                .replace("src=\"/images/products/", "src=\"images/products/")
                // Remove crossorigin attributes (CORS not supported on file://)
                .replace("crossorigin ", "")
                .replace("crossorigin>", ">")
                // Inject JS proxy after <head>
                .replaceFirst("<head>", "<head>\n<script>$apiProxyJs</script>")

            WebResourceResponse(
                "text/html",
                "UTF-8",
                ByteArrayInputStream(modifiedHtml.toByteArray(Charsets.UTF_8))
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun loadFromAssets(assetPath: String): WebResourceResponse? {
        return try {
            val stream = context.assets.open(assetPath)
            val mime = guessMimeType(assetPath)
            WebResourceResponse(mime, "UTF-8", stream)
        } catch (e: Exception) {
            null
        }
    }

    private fun proxyToServer(targetUrl: String): WebResourceResponse? {
        return try {
            val connection = URL(targetUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val mimeType = connection.contentType ?: "application/octet-stream"
            val bodyStream = connection.inputStream
            val bodyBytes = bodyStream?.readBytes()
            connection.disconnect()

            if (bodyBytes != null) {
                WebResourceResponse(
                    mimeType.split(";")[0].trim(),
                    "UTF-8",
                    ByteArrayInputStream(bodyBytes)
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun guessMimeType(path: String): String {
        return when {
            path.endsWith(".html") -> "text/html"
            path.endsWith(".js") -> "application/javascript"
            path.endsWith(".css") -> "text/css"
            path.endsWith(".webp") -> "image/webp"
            path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".ico") -> "image/x-icon"
            path.endsWith(".svg") -> "image/svg+xml"
            path.endsWith(".json") -> "application/json"
            else -> "application/octet-stream"
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
