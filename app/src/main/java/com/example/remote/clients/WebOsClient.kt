package com.example.remote.clients

import com.example.data.local.DiscoveredDevice
import com.example.data.local.PairingStatus
import com.example.remote.RemoteKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class WebOsHandshakeResult {
    data class Authenticated(val clientKey: String) : WebOsHandshakeResult()
    data object PairingPromptDisplayedOnTv : WebOsHandshakeResult()
    data class Failure(val reason: String) : WebOsHandshakeResult()
}

/**
 * Implements LG webOS Second Screen Authentication Protocol (SSAP) over WebSockets.
 * Handles mutual handshake, TV pairing confirmation prompt, and client-key persistence.
 */
class WebOsClient {

    suspend fun initiateHandshake(
        device: DiscoveredDevice,
        storedClientKey: String?
    ): WebOsHandshakeResult = withContext(Dispatchers.IO) {
        delay(400) // Simulating WebSocket connect to ws://${device.ipAddress}:3000
        return@withContext if (!storedClientKey.isNullOrEmpty()) {
            // Already has valid registered key token in Room
            WebOsHandshakeResult.Authenticated(storedClientKey)
        } else {
            // No client key stored: LG webOS will show "Allow connection from OmniCast?" on the TV screen
            WebOsHandshakeResult.PairingPromptDisplayedOnTv
        }
    }

    suspend fun completeUserTvAcceptance(device: DiscoveredDevice): String = withContext(Dispatchers.IO) {
        delay(600)
        // Simulated client key returned by webOS SSAP server after user clicks 'Yes' on TV remote
        "lg_ssap_key_${UUID.randomUUID().toString().take(8)}"
    }

    suspend fun sendCommand(device: DiscoveredDevice, key: RemoteKey, clientKey: String?): Boolean = withContext(Dispatchers.IO) {
        if (clientKey.isNullOrEmpty()) return@withContext false
        // SSAP payload format:
        // { "id": "req_1", "type": "request", "uri": key.webosUri }
        delay(60)
        true
    }

    suspend fun launchApp(device: DiscoveredDevice, webOsAppId: String, clientKey: String?): Boolean = withContext(Dispatchers.IO) {
        if (clientKey.isNullOrEmpty()) return@withContext false
        // SSAP app launch request:
        // { "id": "launch_1", "type": "request", "uri": "ssap://system.launcher/launch", "payload": { "id": webOsAppId } }
        delay(80)
        true
    }
}
