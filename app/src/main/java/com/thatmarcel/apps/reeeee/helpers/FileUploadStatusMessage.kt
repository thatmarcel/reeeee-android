package com.thatmarcel.apps.reeeee.helpers

import android.widget.Toast
import com.thatmarcel.apps.reeeee.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FileUploadStatusMessage {
    companion object {
        fun displayError(errorType: FileUploadErrorType) {
            runBlocking {
                launch(Dispatchers.Main) {
                    Toast.makeText(App.context, "Something went wrong ($errorType)", Toast.LENGTH_LONG).show()
                }
            }
        }

        fun displaySuccess() {
            runBlocking {
                launch(Dispatchers.Main) {
                    Toast.makeText(App.context, "Uploaded successfully", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}