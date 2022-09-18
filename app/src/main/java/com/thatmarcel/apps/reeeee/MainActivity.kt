package com.thatmarcel.apps.reeeee

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.doAfterTextChanged
import com.thatmarcel.apps.reeeee.databinding.ActivityMainBinding
import com.thatmarcel.apps.reeeee.helpers.FilePicker
import com.thatmarcel.apps.reeeee.helpers.FileUploader
import com.thatmarcel.apps.reeeee.helpers.NetworkScanner
import com.thatmarcel.apps.reeeee.helpers.PersistentKVS

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var filePicker: FilePicker

    private lateinit var ipAddressEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var findIPAddressButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        progressBar = findViewById(R.id.content_main_ip_address_progress_bar)

        prepareButtons()
        prepareEditTexts()

        filePicker = FilePicker(this)

        binding.fab.setOnClickListener {
            filePicker.launch { contentUri ->
                startLoading()

                FileUploader.uploadFile(contentUri) {
                    stopLoading()
                }
            }
        }
    }

    private fun startLoading() {
        findIPAddressButton.isEnabled = false
        binding.fab.isEnabled = false

        progressBar.visibility = View.VISIBLE
    }

    private fun stopLoading() {
        findIPAddressButton.isEnabled = true
        binding.fab.isEnabled = true

        progressBar.visibility = View.GONE
    }

    private fun prepareButtons() {
        findIPAddressButton = findViewById(R.id.content_main_find_ip_address_button)

        findIPAddressButton.setOnClickListener {
            startLoading()

            NetworkScanner().findDevices { connectedToWifi, devices ->
                stopLoading()

                if (!connectedToWifi) {
                    Toast.makeText(this, "You are not connected to a WiFi network", Toast.LENGTH_LONG).show()
                    return@findDevices
                }

                if (devices.size < 1) {
                    Toast.makeText(this, "No device found", Toast.LENGTH_LONG).show()
                    return@findDevices
                }

                val deviceIP = devices[0]

                PersistentKVS.ipAddress = deviceIP

                ipAddressEditText.setText(deviceIP)

                Toast.makeText(this, "Device found", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun prepareEditTexts() {
        ipAddressEditText = findViewById(R.id.content_main_ip_address_input_edit_text)
        ipAddressEditText.setText(PersistentKVS.ipAddress)

        ipAddressEditText.doAfterTextChanged {
            PersistentKVS.ipAddress = ipAddressEditText.text.toString()
        }

        ipAddressEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ipAddressEditText.clearFocus()
            }

            return@setOnEditorActionListener false
        }

        passwordEditText = findViewById(R.id.content_main_password_input_edit_text)
        passwordEditText.setText(PersistentKVS.password)

        passwordEditText.doAfterTextChanged {
            PersistentKVS.password = passwordEditText.text.toString()
        }

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                passwordEditText.clearFocus()
            }

            return@setOnEditorActionListener false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }
}