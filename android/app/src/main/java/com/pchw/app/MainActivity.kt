package com.pchw.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.pchw.app.util.ServerConfig
import com.pchw.app.webview.WebViewSetup

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var serverConfig: ServerConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serverConfig = ServerConfig(this)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        webView = findViewById(R.id.webview)
        setupWebView()
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    private var needsReload = false

    private fun setupWebView() {
        WebViewSetup.configure(webView, this, serverConfig.baseUrl)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            needsReload = true
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (needsReload) {
            needsReload = false
            setupWebView()
            webView.reload()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
