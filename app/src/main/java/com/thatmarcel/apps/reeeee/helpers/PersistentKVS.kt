package com.thatmarcel.apps.reeeee.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.thatmarcel.apps.reeeee.App

const val SHARED_PREFERENCES_FILE_KEY = "com.thatmarcel.apps.reeeee.SHARED_PREFERENCES"
const val SHARED_PREFERENCES_STORED_DEVICE_IP_ADDRESS_KEY = "device-ip-address"
const val SHARED_PREFERENCES_STORED_DEVICE_PASSWORD_KEY = "device-password"

class PersistentKVS {
    companion object {
        private var __sharedPreferences: SharedPreferences? = null
        private val sharedPreferences: SharedPreferences
            get() {
                if (__sharedPreferences == null) {
                    __sharedPreferences = App.context.getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
                }

                return __sharedPreferences!!
            }

        private var __ipAddress: String? = null
        var ipAddress: String
            get() {
                if (__ipAddress == null) {
                    __ipAddress = sharedPreferences.getString(SHARED_PREFERENCES_STORED_DEVICE_IP_ADDRESS_KEY, "") ?: ""
                }

                return __ipAddress!!
            }
            set(newValue) {
                __ipAddress = newValue

                sharedPreferences.edit {
                    putString(SHARED_PREFERENCES_STORED_DEVICE_IP_ADDRESS_KEY, newValue)
                    apply()
                }
            }

        private var __password: String? = null
        var password: String
            get() {
                if (__password == null) {
                    __password = sharedPreferences.getString(SHARED_PREFERENCES_STORED_DEVICE_PASSWORD_KEY, "") ?: ""
                }

                return __password!!
            }
            set(newValue) {
                __password = newValue

                sharedPreferences.edit {
                    putString(SHARED_PREFERENCES_STORED_DEVICE_PASSWORD_KEY, newValue)
                    apply()
                }
            }
    }
}