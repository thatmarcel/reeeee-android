package com.thatmarcel.apps.reeeee.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.thatmarcel.apps.reeeee.App
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.net.Inet4Address
import java.util.concurrent.atomic.AtomicInteger


@OptIn(DelicateCoroutinesApi::class)
class NetworkScanner {
    fun findDevices(completion: (connectedToWifi: Boolean, ipAddress: List<String>) -> Unit) {
        val connectivityManager = App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return

        val isConnectedToWifi = connectivityManager.getNetworkCapabilities(activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        if (!isConnectedToWifi) {
            completion(false, listOf())
            return
        }

        val activeIPv4Routes = connectivityManager.getLinkProperties(activeNetwork)?.routes?.filter { it.isDefaultRoute && it.destination.address is Inet4Address }
        val route = activeIPv4Routes?.first() ?: return
        val gatewayAddress = (route.gateway?.hostAddress ?: return).toString()
        val prefix = gatewayAddress.split(".").dropLast(1).joinToString(".")

        val pingsDone = AtomicInteger(0)
        var index = 0
        repeat(51) {
            GlobalScope.launch(Dispatchers.IO) {
                var scopedIndex = index

                repeat(5) {
                    val address = Inet4Address.getByName("$prefix.$scopedIndex")
                    address.isReachable(500)

                    pingsDone.incrementAndGet()

                    if (pingsDone.get() == 255) {
                        launch(Dispatchers.Main) {
                            val ipProc: Process = Runtime.getRuntime().exec("/system/bin/ip neighbor")
                            ipProc.waitFor()

                            val availableTabletIpAddresses = readInputStream(ipProc.inputStream)
                                .split("\n")
                                .filter { !it.contains("INCOMPLETE") }
                                .map { it
                                    .trim()
                                    .split(" ")
                                }
                                .filter {
                                    it.size >= 5 &&
                                    it[4].lowercase().startsWith("20:50:e7:" /* MAC Address Prefix for Ampak Technology, used by reMarkable 2 at least */)
                                }
                                .map { it[0] }

                            completion(true, availableTabletIpAddresses)
                        }
                    }

                    scopedIndex++
                }
            }

            index++
        }
    }

    private fun readInputStream(inputStream: InputStream): String {
        val reader = BufferedReader(inputStream.reader())
        val content = StringBuilder()

        reader.use {
            var line = it.readLine()
            while (line != null) {
                if (content.isNotEmpty()) {
                    content.append("\n")
                }
                content.append(line)
                line = it.readLine()
            }
        }

        return content.toString()
    }
}