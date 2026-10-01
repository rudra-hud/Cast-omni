package com.example.casting

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.WindowManager
import com.example.data.local.DiscoveredDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

enum class CastQualityProfile(
    val title: String,
    val resolutionLabel: String,
    val transportType: String,
    val targetFps: Int,
    val targetBitrateMbps: Int,
    val latencyLabel: String,
    val isDirect4K: Boolean,
    val thermalImpact: String
) {
    LOW_LATENCY_1080P60(
        title = "Low-Latency Screen Mirroring (Default)",
        resolutionLabel = "1080p FHD (1920x1080)",
        transportType = "WebRTC / RTSP Pipeline",
        targetFps = 60,
        targetBitrateMbps = 16,
        latencyLabel = "~65–85ms Latency (Real-time)",
        isDirect4K = false,
        thermalImpact = "Optimal (Cool & Low Battery)"
    ),
    DIRECT_4K_MEDIA(
        title = "Cinema 4K UHD Direct Stream",
        resolutionLabel = "4K UHD (3840x2160)",
        transportType = "Direct HTTP / UPnP Stream",
        targetFps = 60,
        targetBitrateMbps = 40,
        latencyLabel = "~400–600ms Buffer (Pristine Video/Photos)",
        isDirect4K = true,
        thermalImpact = "Zero Phone Re-encoding (Hardware Offload)"
    ),
    KINDLE_READING_MODE(
        title = "E-Reader / Kindle Mirroring",
        resolutionLabel = "1080p Sharp-Text Contrast",
        transportType = "Static Low-Refresh Mirror",
        targetFps = 30,
        targetBitrateMbps = 8,
        latencyLabel = "Optimized for Books & Documents",
        isDirect4K = false,
        thermalImpact = "Ultra-Low Power"
    ),
    SMART_ADAPTIVE(
        title = "Smart Adaptive Profile",
        resolutionLabel = "Dynamic 720p - 1080p",
        transportType = "Auto-Scaling Transport",
        targetFps = 60,
        targetBitrateMbps = 14,
        latencyLabel = "Auto-tuned to Wi-Fi stability",
        isDirect4K = false,
        thermalImpact = "Balanced"
    )
}

enum class CastType {
    SCREEN_MIRROR,
    KINDLE_READING_MIRROR,
    VIDEO_4K_DIRECT,
    PHOTO_SLIDESHOW,
    AUDIO_DIRECT
}

data class CastTelemetry(
    val latencyMs: Int = 0,
    val currentFps: Int = 60,
    val currentBitrateMbps: Double = 16.0,
    val packetsDropped: Int = 0,
    val bufferFillPercent: Int = 18,
    val durationSeconds: Long = 0,
    val resolution: String = "1920x1080 (FHD 60fps)",
    val isMuted: Boolean = false,
    val isPaused: Boolean = false,
    val phoneTempCelsius: Int = 28
)

sealed class CastState {
    data object Idle : CastState()
    data class Connecting(val device: DiscoveredDevice, val targetType: CastType) : CastState()
    data class Active(
        val device: DiscoveredDevice,
        val type: CastType,
        val profile: CastQualityProfile,
        val telemetry: CastTelemetry = CastTelemetry()
    ) : CastState()
    data class Error(val message: String) : CastState()
}

class CastingEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private val _castState = MutableStateFlow<CastState>(CastState.Idle)
    val castState: StateFlow<CastState> = _castState.asStateFlow()

    private val _selectedProfile = MutableStateFlow(CastQualityProfile.LOW_LATENCY_1080P60)
    val selectedProfile: StateFlow<CastQualityProfile> = _selectedProfile.asStateFlow()

    private var telemetryJob: Job? = null
    private var sessionSeconds = 0L

    fun setQualityProfile(profile: CastQualityProfile) {
        _selectedProfile.value = profile
        val current = _castState.value
        if (current is CastState.Active) {
            _castState.value = current.copy(
                profile = profile,
                telemetry = current.telemetry.copy(
                    resolution = profile.resolutionLabel,
                    currentFps = profile.targetFps,
                    currentBitrateMbps = profile.targetBitrateMbps.toDouble()
                )
            )
        }
    }

    fun startCasting(device: DiscoveredDevice, type: CastType = CastType.SCREEN_MIRROR) {
        _castState.value = CastState.Connecting(device, type)

        val profile = when (type) {
            CastType.KINDLE_READING_MIRROR -> CastQualityProfile.KINDLE_READING_MODE
            CastType.VIDEO_4K_DIRECT, CastType.PHOTO_SLIDESHOW -> CastQualityProfile.DIRECT_4K_MEDIA
            CastType.SCREEN_MIRROR, CastType.AUDIO_DIRECT -> _selectedProfile.value
        }
        _selectedProfile.value = profile

        scope.launch(Dispatchers.IO) {
            val realPing = measureRealLatency(device.ipAddress, device.port)
            val realTemp = getDeviceTemperature()
            val realFps = getRealDisplayRefreshRate()

            val initialTelemetry = CastTelemetry(
                latencyMs = if (realPing > 0) realPing else device.avgLatencyMs,
                currentFps = realFps.coerceAtMost(profile.targetFps),
                currentBitrateMbps = profile.targetBitrateMbps.toDouble(),
                packetsDropped = 0,
                bufferFillPercent = if (profile == CastQualityProfile.DIRECT_4K_MEDIA) 70 else 20,
                durationSeconds = 0,
                resolution = profile.resolutionLabel,
                phoneTempCelsius = realTemp
            )

            _castState.value = CastState.Active(
                device = device,
                type = type,
                profile = profile,
                telemetry = initialTelemetry
            )

            startTelemetryLoop(device)
        }
    }

    private fun startTelemetryLoop(device: DiscoveredDevice) {
        telemetryJob?.cancel()
        sessionSeconds = 0L
        telemetryJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000)
                sessionSeconds++
                val current = _castState.value
                if (current is CastState.Active && !current.telemetry.isPaused) {
                    val realTemp = getDeviceTemperature()
                    val realPing = withContext(Dispatchers.IO) {
                        measureRealLatency(device.ipAddress, device.port)
                    }

                    _castState.value = current.copy(
                        telemetry = current.telemetry.copy(
                            latencyMs = if (realPing > 0) realPing else current.telemetry.latencyMs,
                            durationSeconds = sessionSeconds,
                            phoneTempCelsius = realTemp
                        )
                    )
                }
            }
        }
    }

    private fun measureRealLatency(ip: String, port: Int): Int {
        val start = System.currentTimeMillis()
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 300)
            }
            (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
        } catch (_: Exception) {
            -1
        }
    }

    private fun getDeviceTemperature(): Int {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val rawTemp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            if (rawTemp > 0) rawTemp / 10 else 28
        } catch (_: Exception) {
            28
        }
    }

    @Suppress("DEPRECATION")
    private fun getRealDisplayRefreshRate(): Int {
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display = wm?.defaultDisplay
            val rate = display?.refreshRate?.toInt() ?: 60
            if (rate > 0) rate else 60
        } catch (_: Exception) {
            60
        }
    }

    fun togglePause() {
        val current = _castState.value
        if (current is CastState.Active) {
            val paused = !current.telemetry.isPaused
            _castState.value = current.copy(
                telemetry = current.telemetry.copy(isPaused = paused)
            )
        }
    }

    fun toggleMute() {
        val current = _castState.value
        if (current is CastState.Active) {
            val muted = !current.telemetry.isMuted
            _castState.value = current.copy(
                telemetry = current.telemetry.copy(isMuted = muted)
            )
        }
    }

    fun stopCasting(): CastTelemetry? {
        telemetryJob?.cancel()
        telemetryJob = null
        val current = _castState.value
        val lastTelemetry = if (current is CastState.Active) current.telemetry else null
        _castState.value = CastState.Idle
        return lastTelemetry
    }
}
