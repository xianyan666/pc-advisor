package com.pchw.app.webview

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView

object WebViewSetup {

    @SuppressLint("SetJavaScriptEnabled")
    fun configure(webView: WebView, context: Context, serverBaseUrl: String) {
        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true

        @Suppress("DEPRECATION")
        settings.allowUniversalAccessFromFileURLs = true

        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setSupportMultipleWindows(false)
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.displayZoomControls = false
        settings.builtInZoomControls = true

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView.webViewClient = ApiProxyWebViewClient(context, serverBaseUrl)
    }
}
