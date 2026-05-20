package com.pchw.app.webview

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView

object WebViewSetup {

    @SuppressLint("SetJavaScriptEnabled")
    fun configure(webView: WebView, client: ApiProxyWebViewClient) {
        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = false
        settings.allowContentAccess = true

        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.displayZoomControls = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView.webViewClient = client
    }
}
