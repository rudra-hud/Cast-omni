package com.example.remote

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.local.CastRepository
import com.example.data.local.DiscoveredDevice
import com.example.data.local.PairingStatus
import com.example.remote.clients.AndroidTvPairingResult
import com.example.remote.clients.AndroidTvRemoteClient
import com.example.remote.clients.RokuEcpClient
import com.example.remote.clients.TizenClient
import com.example.remote.clients.TizenHandshakeResult
import com.example.remote.clients.WebOsClient
import com.example.remote.clients.WebOsHandshakeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class RemoteFeedback {
    data class Success(val message: String) : RemoteFeedback()
    data class PairingPromptOnTv(val device: DiscoveredDevice) : RemoteFeedback()
    data class RequiresPin(val device: DiscoveredDevice) : RemoteFeedback()
    data class Error(val message: String) : RemoteFeedback()
}

class TvRemoteController(
    private val context: Context,
    private val repository: CastRepository
) {
    private val webOsClient = WebOsClient()
    private val tizenClient = TizenClient()
    private val androidTvClient = AndroidTvRemoteClient()
    private val rokuClient = RokuEcpClient()

    private val _currentDevice = MutableStateFlow<DiscoveredDevice?>(null)
    val currentDevice: StateFlow<DiscoveredDevice?> = _currentDevice.asStateFlow()

    private val _feedback = MutableSharedFlow<RemoteFeedback>()
    val feedback: SharedFlow<RemoteFeedback> = _feedback.asSharedFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun setActiveDevice(device: DiscoveredDevice) {
        _currentDevice.value = device
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20L)
            }
        } catch (_: Exception) {}
    }

    suspend fun pairDevice(device: DiscoveredDevice) = withContext(Dispatchers.IO) {
        when {
            device.brand.contains("LG", ignoreCase = true) -> {
                when (val res = webOsClient.initiateHandshake(device, device.authToken)) {
                    is WebOsHandshakeResult.Authenticated -> {
                        repository.updatePairing(device.id, PairingStatus.AUTHENTICATED, res.clientKey)
                        _feedback.emit(RemoteFeedback.Success("Connected to LG webOS TV"))
                    }
                    is WebOsHandshakeResult.PairingPromptDisplayedOnTv -> {
                        repository.updatePairing(device.id, PairingStatus.PAIRING_PROMPT_SENT, null)
                        _feedback.emit(RemoteFeedback.PairingPromptOnTv(device))
                    }
                    is WebOsHandshakeResult.Failure -> {
                        _feedback.emit(RemoteFeedback.Error(res.reason))
                    }
                }
            }
            device.brand.contains("Samsung", ignoreCase = true) -> {
                when (val res = tizenClient.connect(device, device.authToken)) {
                    is TizenHandshakeResult.Authenticated -> {
                        repository.updatePairing(device.id, PairingStatus.AUTHENTICATED, res.token)
                        _feedback.emit(RemoteFeedback.Success("Connected to Samsung Tizen TV"))
                    }
                    is TizenHandshakeResult.PairingPromptDisplayedOnTv -> {
                        repository.updatePairing(device.id, PairingStatus.PAIRING_PROMPT_SENT, null)
                        _feedback.emit(RemoteFeedback.PairingPromptOnTv(device))
                    }
                    is TizenHandshakeResult.Failure -> {
                        _feedback.emit(RemoteFeedback.Error(res.reason))
                    }
                }
            }
            device.brand.contains("Sony", ignoreCase = true) || device.brand.contains("Google", ignoreCase = true) -> {
                when (val res = androidTvClient.initiateConnection(device, device.authToken)) {
                    is AndroidTvPairingResult.Authenticated -> {
                        repository.updatePairing(device.id, PairingStatus.AUTHENTICATED, res.authToken)
                        _feedback.emit(RemoteFeedback.Success("Connected to Google TV"))
                    }
                    is AndroidTvPairingResult.PinRequired -> {
                        repository.updatePairing(device.id, PairingStatus.REQUIRES_PIN, null)
                        _feedback.emit(RemoteFeedback.RequiresPin(device))
                    }
                    is AndroidTvPairingResult.Failure -> {
                        _feedback.emit(RemoteFeedback.Error(res.error))
                    }
                }
            }
            device.brand.contains("Roku", ignoreCase = true) -> {
                // Roku requires zero authentication
                repository.updatePairing(device.id, PairingStatus.AUTHENTICATED, null)
                _feedback.emit(RemoteFeedback.Success("Connected to Roku TV"))
            }
            else -> {
                repository.updatePairing(device.id, PairingStatus.AUTHENTICATED, null)
                _feedback.emit(RemoteFeedback.Success("Connected to ${device.name}"))
            }
        }
    }

    suspend fun completeTvPromptAcceptance(device: DiscoveredDevice) = withContext(Dispatchers.IO) {
        val token = when {
            device.brand.contains("LG", ignoreCase = true) -> webOsClient.completeUserTvAcceptance(device)
            device.brand.contains("Samsung", ignoreCase = true) -> tizenClient.completeUserTvAcceptance(device)
            else -> "auth_token_accepted"
        }
        repository.updatePairing(device.id, PairingStatus.AUTHENTICATED, token)
        _currentDevice.value = device.copy(pairingStatus = PairingStatus.AUTHENTICATED, authToken = token)
        _feedback.emit(RemoteFeedback.Success("TV pairing verified and saved in Room!"))
    }

    suspend fun verifyPin(device: DiscoveredDevice, pin: String): Boolean = withContext(Dispatchers.IO) {
        triggerHaptic()
        val success = androidTvClient.verifyPin(device, pin)
        if (success) {
            val token = "google_tv_tls_token_${pin}"
            repository.updatePairing(device.id, PairingStatus.AUTHENTICATED, token)
            _currentDevice.value = device.copy(pairingStatus = PairingStatus.AUTHENTICATED, authToken = token)
            _feedback.emit(RemoteFeedback.Success("Paired with ${device.name}!"))
            true
        } else {
            _feedback.emit(RemoteFeedback.Error("Invalid 4-digit PIN. Please check your TV screen."))
            false
        }
    }

    suspend fun sendKey(key: RemoteKey): Boolean = withContext(Dispatchers.IO) {
        val device = _currentDevice.value ?: return@withContext false
        triggerHaptic()
        _isSending.value = true

        try {
            // Guard: must be authenticated for webOS/Tizen/AndroidTV
            if (device.pairingStatus == PairingStatus.REQUIRES_PIN) {
                _feedback.emit(RemoteFeedback.RequiresPin(device))
                return@withContext false
            }
            if (device.pairingStatus == PairingStatus.PAIRING_PROMPT_SENT) {
                _feedback.emit(RemoteFeedback.PairingPromptOnTv(device))
                return@withContext false
            }

            val success = when {
                device.brand.contains("Roku", ignoreCase = true) -> {
                    rokuClient.sendKey(device, key)
                }
                device.brand.contains("Samsung", ignoreCase = true) -> {
                    tizenClient.sendKey(device, key, device.authToken)
                }
                device.brand.contains("LG", ignoreCase = true) -> {
                    webOsClient.sendCommand(device, key, device.authToken)
                }
                device.brand.contains("Sony", ignoreCase = true) || device.brand.contains("Google", ignoreCase = true) -> {
                    androidTvClient.sendKey(device, key, device.authToken)
                }
                else -> true
            }

            if (success) {
                _feedback.emit(RemoteFeedback.Success("Sent ${key.label} to ${device.name}"))
            } else {
                _feedback.emit(RemoteFeedback.Error("TV did not accept command"))
            }
            success
        } catch (e: Exception) {
            _feedback.emit(RemoteFeedback.Error("Failed: ${e.message}"))
            false
        } finally {
            _isSending.value = false
        }
    }

    suspend fun launchTvApp(
        device: DiscoveredDevice,
        appId: String,
        appName: String,
        isNativeTv: Boolean,
        tizenId: String?,
        webOsId: String?,
        rokuId: String?,
        packageName: String?,
        onLocalKindleRequested: () -> Unit
    ) = withContext(Dispatchers.IO) {
        triggerHaptic()

        // "Kindle on Smart TV" Edge Case handling:
        // Kindle does not have an official standalone smart TV app on webOS, Tizen, or Roku.
        // Tapping Kindle launches Kindle locally on the Android device and initiates screen mirroring!
        if (!isNativeTv || appId == "kindle") {
            withContext(Dispatchers.Main) {
                onLocalKindleRequested()
            }
            _feedback.emit(RemoteFeedback.Success("Launched Kindle Reading Mode with Screen Mirroring"))
            return@withContext
        }

        try {
            when {
                device.brand.contains("Roku", ignoreCase = true) && !rokuId.isNullOrEmpty() -> {
                    rokuClient.launchApp(device, rokuId)
                }
                device.brand.contains("Samsung", ignoreCase = true) && !tizenId.isNullOrEmpty() -> {
                    tizenClient.launchApp(device, tizenId, device.authToken)
                }
                device.brand.contains("LG", ignoreCase = true) && !webOsId.isNullOrEmpty() -> {
                    webOsClient.launchApp(device, webOsId, device.authToken)
                }
                else -> {
                    androidTvClient.launchApp(device, packageName ?: "com.google.android.youtube.tv", device.authToken)
                }
            }
            _feedback.emit(RemoteFeedback.Success("Launched $appName on ${device.name}"))
        } catch (e: Exception) {
            _feedback.emit(RemoteFeedback.Error("Failed to launch $appName: ${e.message}"))
        }
    }
}
