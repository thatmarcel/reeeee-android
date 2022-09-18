package com.thatmarcel.apps.reeeee.helpers

import com.trilead.ssh2.ChannelCondition
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionMonitor
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress

const val AUTHENTICATION_METHOD_PASSWORD = "password"

const val CONDITIONS = (ChannelCondition.STDOUT_DATA
        or ChannelCondition.STDERR_DATA
        or ChannelCondition.CLOSED
        or ChannelCondition.EOF)

class SSHClient(hostname: String, port: Int) : ConnectionMonitor {
    private val connection = Connection(hostname, port)

    fun connect(username: String, password: String): SSHConnectionResult {
        connection.addConnectionMonitor(this)

        try {
            connection.setCompression(true)
        } catch (_: IOException) {}

        try {
            connection.connect()
        } catch (_: IOException) {
            return SSHConnectionResult.NETWORK_CONNECTION_FAILED
        }

        if (!connection.isAuthMethodAvailable(username, AUTHENTICATION_METHOD_PASSWORD)) {
            return SSHConnectionResult.AUTHENTICATION_METHOD_UNAVAILABLE
        }

        if (!connection.authenticateWithPassword(username, password)) {
            return SSHConnectionResult.AUTHENTICATION_FAILED
        }

        return SSHConnectionResult.SUCCESS
    }

    fun disconnect() {
        connection.close()
    }

    fun runCommand(command: String): String? {
        if (!connection.isAuthenticationComplete) {
            return null
        }

        val session = connection.openSession()
        session.execCommand(command)
        session.waitForCondition(CONDITIONS, 0)
        session.close()

        if (session.stderr.available() > 0) {
            return null
        }

        return readInputStream(session.stdout)
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

    fun startPortForwarding(localPort: Int, remoteAddress: String, remotePort: Int): Boolean {
        return try {
            connection.createLocalPortForwarder(
                InetSocketAddress(InetAddress.getLocalHost(), localPort),
                remoteAddress,
                remotePort
            )

            true
        } catch (_: IOException) {
            false
        }
    }

    override fun connectionLost(reason: Throwable?) {}
}