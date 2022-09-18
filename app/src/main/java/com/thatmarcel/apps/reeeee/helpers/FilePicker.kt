package com.thatmarcel.apps.reeeee.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class FilePicker(launchingActivity: AppCompatActivity) {
    private var completionCallback: ((contentUri: Uri) -> Unit)? = null

    private var resultLauncher: ActivityResultLauncher<Intent> = launchingActivity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contentUri = result.data?.data ?: return@registerForActivityResult

            handlePickerFinishedWithContentUri(contentUri)
        }
    }

    private fun handlePickerFinishedWithContentUri(contentUri: Uri) {
        if (completionCallback != null) {
            completionCallback!!(contentUri)

            completionCallback = null
        }
    }

    fun launch(completion: (contentUri: Uri) -> Unit) {
        completionCallback = completion

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }

        resultLauncher.launch(intent)
    }
}