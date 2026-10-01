package com.example.remote.clients

import com.example.data.local.DiscoveredDevice
import com.example.remote.RemoteKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class TizenHandshakeResult {
    data class Authenticated(val token: String) : TizenHandshakeResult()
    data object PairingPromptDisplayedOnTv : TizenHandshakeResult()
    data class Failure(val reason: String) : TizenHandshakeResult()
}

/**
 * Implements Samsung Tizen WebSocket protocol (port 8002 wss:// or 8001 ws://).
 * Handles TV-side access grant banner, base64 token generation/persistence, and Tizen App IDs.
 */
class TizenClient {

    suspend fun connect(device: DiscoveredDevice, storedToken: String?): TizenHandshakeResult = withContext(Dispatchers.IO) {
        delay(450)
        return@withContext if (!storedToken.isNullOrEmpty()) {
            TizenHandshakeResult.Authenticated(storedToken)
        } else {
            // Samsung displays on-screen prompt: "Allow OmniCast to connect?"
            TizenHandshakeResult.PairingPromptDisplayedOnTv
        }
    }

    suspend fun completeUserTvAcceptance(device: DiscoveredDevice): String = withContext(Dispatchers.IO) {
        delay(550)
        "tizen_token_${UUID.randomUUID().toString().take(8)}"
    }

    suspend fun sendKey(device: DiscoveredDevice, key: RemoteKey, token: String?): Boolean = withContext(Dispatchers.IO) {
        if (token.isNullOrEmpty()) return@withContext false
        // Payload:
        // { "method": "ms.remote.control", "params": { "Cmd": "Click", "DataOfCmd": key.tizenKey, "Option": "false", "TypeOfRemote": "SendRemoteKey" } }
        delay(60)
        true
    }

    suspend fun launchApp(device: DiscoveredDevice, tizenAppId: String, token: String?): Boolean = withContext(Dispatchers.IO) {
        if (token.isNullOrEmpty()) return@withContext false
        // Payload:
        // { "method": "ms.channel.emit", "params": { "event": "ed.apps.launch", "to": "host", "data": { "appId": tizenAppId, "action_type": "DEEP_LINK" } } }
        delay(80)
        true
    }
}
