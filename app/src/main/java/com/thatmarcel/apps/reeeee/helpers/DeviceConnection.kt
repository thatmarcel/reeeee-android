package com.thatmarcel.apps.reeeee.helpers

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException

class DeviceConnection(ipAddress: String, private val password: String) {
    private val sshClient = SSHClient(ipAddress, 22)

    fun startSession(): Boolean {
        val connectionResult = sshClient.connect("root", password)

        if (connectionResult != SSHConnectionResult.SUCCESS) {
            return false
        }

        val result = sshClient.runCommand("/sbin/ip addr list")
            ?: return false

        if (!result.contains("10.11.99.1")) {
            sshClient.runCommand("/sbin/ip addr add 10.11.99.1/29 dev usb0")
        }

        sshClient.startPortForwarding(59743, "10.11.99.1", 80)

        return true
    }

    fun endSession() {
        sshClient.runCommand("/sbin/ip addr del 10.11.99.1/29 dev usb0")

        sshClient.disconnect()
    }

    fun uploadFile(file: File, fileDisplayName: String, mediaType: MediaType) {
        val client = OkHttpClient()

        val formBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileDisplayName, file.asRequestBody(mediaType))
            .build()

        val request = Request.Builder()
            .url("http://localhost:59743/upload")
            .post(formBody)
            .build()

        try {
            client.newCall(request).execute()
        } catch (_: SocketTimeoutException) {
            // Handle uploading timeout (is web ui on?)
            FileUploadStatusMessage.displayError(FileUploadErrorType.HTTP_TIMEOUT)
        } catch (_: ConnectException) {
            // Handle uploading connect error (is the ip address correct?)
            FileUploadStatusMessage.displayError(FileUploadErrorType.HTTP_CONNECT_FAILED)
        }
    }
}