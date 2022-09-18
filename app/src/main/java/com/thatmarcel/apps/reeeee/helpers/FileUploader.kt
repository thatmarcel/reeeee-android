package com.thatmarcel.apps.reeeee.helpers

import android.net.Uri
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType

class FileUploader {
    @OptIn(DelicateCoroutinesApi::class)
    companion object {
        fun uploadFile(contentUri: Uri, ipAddress: String, password: String, completion: ((success: Boolean) -> Unit)?) {
            GlobalScope.launch(Dispatchers.IO) {
                val file = contentUri.contentUriTools.writeContentsToTemporaryFile()

                if (file == null) {
                    if (completion != null) {
                        launch(Dispatchers.Main) {
                            completion(false)
                        }
                    }

                    return@launch
                }

                val deviceConnection = DeviceConnection(ipAddress, password)

                val successfullyConnectedViaSSH = deviceConnection.startSession()

                if (!successfullyConnectedViaSSH) {
                    FileUploadStatusMessage.displayError(FileUploadErrorType.SSH_FAILED)

                    if (completion != null) {
                        launch(Dispatchers.Main) {
                            completion(false)
                        }
                    }

                    return@launch
                }

                Thread.sleep(1000)

                deviceConnection.uploadFile(file, contentUri.contentUriTools.generateDisplayName(), "application/pdf".toMediaType())

                deviceConnection.endSession()

                try {
                    file.delete()
                } catch (_: Exception) {}

                FileUploadStatusMessage.displaySuccess()

                if (completion != null) {
                    launch(Dispatchers.Main) {
                        completion(false)
                    }
                }
            }
        }

        fun uploadFile(contentUri: Uri, completion: ((success: Boolean) -> Unit)?) {
            uploadFile(contentUri, PersistentKVS.ipAddress, PersistentKVS.password, completion)
        }
    }
}