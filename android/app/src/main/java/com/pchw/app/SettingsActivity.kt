package com.pchw.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pchw.app.util.ServerConfig

class SettingsActivity : AppCompatActivity() {
    private lateinit var serverConfig: ServerConfig
    private lateinit var hostInput: EditText
    private lateinit var portInput: EditText
    private lateinit var saveButton: Button
    private lateinit var testButton: Button
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        serverConfig = ServerConfig(this)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        hostInput = findViewById(R.id.host_input)
        portInput = findViewById(R.id.port_input)
        saveButton = findViewById(R.id.save_button)
        testButton = findViewById(R.id.test_button)
        statusText = findViewById(R.id.status_text)

        hostInput.setText(serverConfig.serverHost)
        portInput.setText(serverConfig.serverPort)

        saveButton.setOnClickListener { saveConfig() }
        testButton.setOnClickListener { testConnection() }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun saveConfig() {
        val host = hostInput.text.toString().trim()
        val port = portInput.text.toString().trim()

        if (host.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        serverConfig.serverHost = host
        serverConfig.serverPort = port
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun testConnection() {
        val host = hostInput.text.toString().trim()
        val port = portInput.text.toString().trim()

        if (host.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        statusText.text = getString(R.string.testing)
        testButton.isEnabled = false

        Thread {
            try {
                val url = java.net.URL("http://$host:$port/api/hello")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val code = conn.responseCode
                val body = conn.inputStream.bufferedReader().use { it.readText() }

                runOnUiThread {
                    if (code == 200) {
                        statusText.text = getString(R.string.connection_success, body)
                        statusText.setTextColor(getColor(android.R.color.holo_green_dark))
                    } else {
                        statusText.text = getString(R.string.connection_fail_code, code)
                        statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                    }
                    testButton.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = getString(R.string.connection_error, e.message)
                    statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                    testButton.isEnabled = true
                }
            }
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
