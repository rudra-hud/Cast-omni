package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.casting.CastProjectionService
import com.example.casting.CastQualityProfile
import com.example.casting.CastState
import com.example.casting.CastType
import com.example.casting.CastingEngine
import com.example.casting.MediaProjectionLifecycleHelper
import com.example.data.local.AppDatabase
import com.example.data.local.CastRepository
import com.example.data.local.CastSessionRecord
import com.example.data.local.DiscoveredDevice
import com.example.data.local.PairingStatus
import com.example.data.local.TvAppItem
import com.example.discovery.DeviceDiscoveryEngine
import com.example.discovery.NetworkDiagnosticInfo
import com.example.remote.RemoteFeedback
import com.example.remote.RemoteKey
import com.example.remote.TvRemoteController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavDestination(val label: String) {
    CAST_HOME("Cast"),
    REMOTE("Remote"),
    TV_APPS("TV Apps"),
    MEDIA_4K("4K Media"),
    SETTINGS("Settings")
}

data class KindleReadingSettings(
    val contrastPreset: String = "Paper White", // "Paper White", "Sepia Warm", "OLED Pure Dark"
    val fontSizeScale: Float = 1.2f,
    val isLandscapeOnTv: Boolean = true,
    val blueLightFilter: Boolean = true
)

class CastViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = CastRepository(database.castDao())

    val discoveryEngine = DeviceDiscoveryEngine(application)
    val castingEngine = CastingEngine(application, viewModelScope)
    val remoteController = TvRemoteController(application, repository)
    val projectionHelper = MediaProjectionLifecycleHelper(application)

    // Reactive State
    val devices: StateFlow<List<DiscoveredDevice>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val castHistory: StateFlow<List<CastSessionRecord>> = repository.recentSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tvApps: StateFlow<List<TvAppItem>> = repository.tvApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isScanning: StateFlow<Boolean> = discoveryEngine.isScanning
    val scanProgress: StateFlow<Float> = discoveryEngine.scanProgress
    val networkInfo: StateFlow<NetworkDiagnosticInfo> = discoveryEngine.networkInfo

    val castState: StateFlow<CastState> = castingEngine.castState
    val selectedProfile: StateFlow<CastQualityProfile> = castingEngine.selectedProfile
    val remoteDevice: StateFlow<DiscoveredDevice?> = remoteController.currentDevice

    private val _currentNavDestination = MutableStateFlow(AppNavDestination.CAST_HOME)
    val currentNavDestination: StateFlow<AppNavDestination> = _currentNavDestination.asStateFlow()

    // Dialog & Flow States
    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _activePinDevice = MutableStateFlow<DiscoveredDevice?>(null)
    val activePinDevice: StateFlow<DiscoveredDevice?> = _activePinDevice.asStateFlow()

    private val _pendingTvPromptDevice = MutableStateFlow<DiscoveredDevice?>(null)
    val pendingTvPromptDevice: StateFlow<DiscoveredDevice?> = _pendingTvPromptDevice.asStateFlow()

    private val _showQualitySheet = MutableStateFlow(false)
    val showQualitySheet: StateFlow<Boolean> = _showQualitySheet.asStateFlow()

    private val _kindleSettings = MutableStateFlow(KindleReadingSettings())
    val kindleSettings: StateFlow<KindleReadingSettings> = _kindleSettings.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _mediaProjectionRequestIntent = MutableSharedFlow<Intent>()
    val mediaProjectionRequestIntent: SharedFlow<Intent> = _mediaProjectionRequestIntent.asSharedFlow()

    private var pendingTargetCastDevice: DiscoveredDevice? = null
    private var pendingTargetCastType: CastType = CastType.SCREEN_MIRROR

    init {
        viewModelScope.launch {
            repository.seedDefaultAppsIfEmpty()
        }

        // Listen for Remote Controller feedback
        viewModelScope.launch {
            remoteController.feedback.collect { fb ->
                when (fb) {
                    is RemoteFeedback.Success -> _toastEvent.emit(fb.message)
                    is RemoteFeedback.Error -> _toastEvent.emit(fb.message)
                    is RemoteFeedback.RequiresPin -> _activePinDevice.value = fb.device
                    is RemoteFeedback.PairingPromptOnTv -> _pendingTvPromptDevice.value = fb.device
                }
            }
        }
    }

    fun setNavDestination(dest: AppNavDestination) {
        _currentNavDestination.value = dest
    }

    fun togglePermissionDialog(show: Boolean) {
        _showPermissionDialog.value = show
    }

    fun toggleQualitySheet(show: Boolean) {
        _showQualitySheet.value = show
    }

    fun dismissPinDialog() {
        _activePinDevice.value = null
    }

    fun dismissTvPromptBanner() {
        _pendingTvPromptDevice.value = null
    }

    fun selectQualityProfile(profile: CastQualityProfile) {
        castingEngine.setQualityProfile(profile)
        _showQualitySheet.value = false
    }

    fun startScanning() {
        viewModelScope.launch {
            discoveryEngine.scanForDevices { discovered ->
                viewModelScope.launch {
                    repository.saveDevice(discovered)
                }
            }
        }
    }

    fun toggleDeviceFavorite(device: DiscoveredDevice) {
        viewModelScope.launch {
            repository.toggleFavorite(device.id, !device.isFavorite)
        }
    }

    fun addManualDevice(name: String, brand: String, ipAddress: String, port: Int = 8000) {
        viewModelScope.launch {
            val device = DiscoveredDevice(
                id = "manual_${ipAddress.replace(".", "_")}",
                name = name.ifEmpty { "$brand Smart TV" },
                brand = brand,
                ipAddress = ipAddress,
                port = port,
                protocol = when (brand.lowercase()) {
                    "roku" -> "Roku ECP"
                    "samsung" -> "Tizen WebSocket"
                    "lg" -> "webOS SSAP"
                    "sony", "google" -> "Google TV Remote v2"
                    else -> "UPnP / DLNA"
                },
                model = "$brand Display",
                supports4K = true,
                avgLatencyMs = 60,
                pairingStatus = if (brand.equals("Roku", ignoreCase = true)) PairingStatus.AUTHENTICATED else PairingStatus.UNPAIRED
            )
            repository.saveDevice(device)
            _toastEvent.emit("Added ${device.name}")
        }
    }

    fun removeDevice(id: String) {
        viewModelScope.launch {
            repository.deleteDevice(id)
            _toastEvent.emit("Removed TV device")
        }
    }

    fun selectRemoteDevice(device: DiscoveredDevice) {
        remoteController.setActiveDevice(device)
        viewModelScope.launch {
            remoteController.pairDevice(device)
        }
    }

    fun submitPin(pin: String) {
        val dev = _activePinDevice.value ?: return
        viewModelScope.launch {
            val ok = remoteController.verifyPin(dev, pin)
            if (ok) {
                _activePinDevice.value = null
            }
        }
    }

    fun confirmUserApprovedOnTv() {
        val dev = _pendingTvPromptDevice.value ?: return
        viewModelScope.launch {
            remoteController.completeTvPromptAcceptance(dev)
            _pendingTvPromptDevice.value = null
        }
    }

    fun sendRemoteKey(key: RemoteKey) {
        viewModelScope.launch {
            remoteController.sendKey(key)
        }
    }

    // Android 14+ Screen Projection Flow
    fun requestStartCasting(device: DiscoveredDevice, type: CastType = CastType.SCREEN_MIRROR) {
        pendingTargetCastDevice = device
        pendingTargetCastType = type

        if (type == CastType.SCREEN_MIRROR || type == CastType.KINDLE_READING_MIRROR) {
            // Android 14+ explicitly requires user consent via createScreenCaptureIntent() per session!
            viewModelScope.launch {
                val intent = projectionHelper.createCaptureIntent()
                _mediaProjectionRequestIntent.emit(intent)
            }
        } else {
            // Direct 4K media offload does not require real-time screen capture
            executeActiveCast(device, type)
        }
    }

    fun handleCaptureConsentResult(resultCode: Int, data: Intent?) {
        val dev = pendingTargetCastDevice ?: return
        val type = pendingTargetCastType

        val success = projectionHelper.onCaptureConsentResult(resultCode, data) {
            // Invoked when user or system stops projection
            stopCasting()
        }

        if (success) {
            CastProjectionService.startService(getApplication(), dev.name)
            executeActiveCast(dev, type)
        } else {
            viewModelScope.launch {
                _toastEvent.emit("Screen projection permission was not granted.")
            }
        }
    }

    private fun executeActiveCast(device: DiscoveredDevice, type: CastType) {
        castingEngine.startCasting(device, type)
        remoteController.setActiveDevice(device)
        viewModelScope.launch {
            repository.markLastConnected(device.id)
            _toastEvent.emit("Connected to ${device.name}")
        }
    }

    fun toggleCastPause() = castingEngine.togglePause()
    fun toggleCastMute() = castingEngine.toggleMute()

    fun stopCasting() {
        val telemetry = castingEngine.stopCasting()
        val currentDevice = remoteController.currentDevice.value
        projectionHelper.stopProjection()
        CastProjectionService.stopService(getApplication())

        if (currentDevice != null && telemetry != null) {
            viewModelScope.launch {
                repository.recordSession(
                    CastSessionRecord(
                        deviceName = currentDevice.name,
                        deviceBrand = currentDevice.brand,
                        durationSeconds = telemetry.durationSeconds,
                        resolutionMode = telemetry.resolution,
                        avgLatencyMs = telemetry.latencyMs
                    )
                )
            }
        }
    }

    // "Kindle on Smart TV" Edge Case handling:
    // When tapping Kindle, launch Kindle locally while initiating screen mirroring to the TV
    fun launchKindleReadingMode(targetDevice: DiscoveredDevice?) {
        val dev = targetDevice ?: devices.value.firstOrNull()
        if (dev == null) {
            viewModelScope.launch {
                _toastEvent.emit("Please select a TV to mirror Kindle to")
            }
            return
        }

        // 1. Engage low-latency high-contrast reading screen mirror
        requestStartCasting(dev, CastType.KINDLE_READING_MIRROR)

        // 2. Launch Kindle app locally on Android (with fallback to Amazon reader URL or Play Store)
        val context = getApplication<Application>()
        try {
            val kindleIntent = context.packageManager.getLaunchIntentForPackage("com.amazon.kindle")
            if (kindleIntent != null) {
                kindleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(kindleIntent)
            } else {
                // If Kindle app is not installed, open Kindle Cloud Reader in browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://read.amazon.com/")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            }
        } catch (_: Exception) {}

        viewModelScope.launch {
            _toastEvent.emit("Kindle Reader opened with 4K TV Screen Mirroring")
        }
    }

    fun launchTvApp(app: TvAppItem) {
        val dev = remoteController.currentDevice.value ?: devices.value.firstOrNull()
        if (dev == null) {
            viewModelScope.launch {
                _toastEvent.emit("Please select a TV to launch ${app.name}")
            }
            return
        }

        viewModelScope.launch {
            remoteController.launchTvApp(
                device = dev,
                appId = app.id,
                appName = app.name,
                isNativeTv = app.isNativeTvApp,
                tizenId = app.tizenAppId,
                webOsId = app.webOsAppId,
                rokuId = app.rokuAppId,
                packageName = app.androidPackageName,
                onLocalKindleRequested = {
                    launchKindleReadingMode(dev)
                }
            )
        }
    }

    fun updateKindleContrast(preset: String) {
        _kindleSettings.value = _kindleSettings.value.copy(contrastPreset = preset)
    }

    fun updateKindleFontSize(scale: Float) {
        _kindleSettings.value = _kindleSettings.value.copy(fontSizeScale = scale)
    }
}
