package com.pchw.app.util

import android.content.Context

class ServerConfig(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var serverHost: String
        get() = prefs.getString(KEY_HOST, DEFAULT_HOST) ?: DEFAULT_HOST
        set(value) { prefs.edit().putString(KEY_HOST, value).apply() }

    var serverPort: String
        get() = prefs.getString(KEY_PORT, DEFAULT_PORT) ?: DEFAULT_PORT
        set(value) { prefs.edit().putString(KEY_PORT, value).apply() }

    val baseUrl: String
        get() = "http://$serverHost:$serverPort"

    companion object {
        private const val PREFS_NAME = "server_config"
        private const val KEY_HOST = "host"
        private const val KEY_PORT = "port"
        const val DEFAULT_HOST = "192.168.1.100"
        const val DEFAULT_PORT = "8080"
    }
}
