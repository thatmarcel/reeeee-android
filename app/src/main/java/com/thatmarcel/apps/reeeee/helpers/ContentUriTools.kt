package com.thatmarcel.apps.reeeee.helpers

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.OpenableColumns
import com.thatmarcel.apps.reeeee.App
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.util.*

val Uri.contentUriTools
    get() = ContentUriTools(this)

class ContentUriTools(private val uri: Uri) {
    fun generateDisplayName(): String {
        val currentDate = Date.from(Instant.now())
        val formattedDate = DateFormat.getDateInstance(DateFormat.SHORT).format(currentDate)
        val formattedTime = SimpleDateFormat("HH:mm:ss").format(currentDate)
        val timestamp = "$formattedDate, $formattedTime "

        println(timestamp)

        val fileDisplayName = "Unnamed file ($timestamp)"

        val cursor = App.context.contentResolver.query(uri, null, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME)).replace(".pdf", "")
            }
        }

        return fileDisplayName
    }

    fun writeContentsToTemporaryFile(): File? {
        val outputFile = File.createTempFile("content_uri_contents_", ".tmp", App.context.cacheDir)

        val outputStream: OutputStream = FileOutputStream(outputFile)
        val inputStream: InputStream = App.context.contentResolver.openInputStream(uri)
            ?: return null

        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) {
            outputStream.write(buf, 0, len)
        }
        outputStream.close()
        inputStream.close()

        return outputFile
    }
}