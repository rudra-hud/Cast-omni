package com.example.casting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class MediaProjectionStatus {
    data object Idle : MediaProjectionStatus()
    data object AwaitingUserConsent : MediaProjectionStatus()
    data class Active(val width: Int, val height: Int, val densityDpi: Int) : MediaProjectionStatus()
    data class Stopped(val reason: String) : MediaProjectionStatus()
}

/**
 * Manages the Android 14+ / 15 / 16 MediaProjection capture lifecycle:
 * - Fresh createScreenCaptureIntent() per session (explicit user consent requirement).
 * - Registration of MediaProjection.Callback for safe handling of orientation changes,
 *   system status bar terminations, and surface lifecycle without crashing.
 */
class MediaProjectionLifecycleHelper(private val context: Context) {

    private val mediaProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    private var currentMediaProjection: MediaProjection? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _status = MutableStateFlow<MediaProjectionStatus>(MediaProjectionStatus.Idle)
    val status: StateFlow<MediaProjectionStatus> = _status.asStateFlow()

    fun createCaptureIntent(): Intent {
        _status.value = MediaProjectionStatus.AwaitingUserConsent
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    fun onCaptureConsentResult(resultCode: Int, data: Intent?, onStopped: () -> Unit): Boolean {
        if (resultCode != Activity.RESULT_OK || data == null) {
            _status.value = MediaProjectionStatus.Stopped("Screen capture consent denied by user")
            onStopped()
            return false
        }

        try {
            val projection = mediaProjectionManager.getMediaProjection(resultCode, data) ?: run {
                _status.value = MediaProjectionStatus.Stopped("MediaProjection session returned null")
                onStopped()
                return false
            }
            currentMediaProjection = projection

            // Strict Android 14+ requirement: MediaProjection.Callback must be registered immediately
            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    currentMediaProjection = null
                    _status.value = MediaProjectionStatus.Stopped("Screen projection stopped by system or user")
                    onStopped()
                }

                override fun onCapturedContentResize(width: Int, height: Int) {
                    super.onCapturedContentResize(width, height)
                    // Handle dynamic orientation change or window resize gracefully
                    val metrics = context.resources.displayMetrics
                    _status.value = MediaProjectionStatus.Active(width, height, metrics.densityDpi)
                }

                override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
                    super.onCapturedContentVisibilityChanged(isVisible)
                }
            }, mainHandler)

            val metrics = context.resources.displayMetrics
            _status.value = MediaProjectionStatus.Active(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
            return true
        } catch (e: Exception) {
            _status.value = MediaProjectionStatus.Stopped("Failed to initialize projection: ${e.message}")
            onStopped()
            return false
        }
    }

    fun stopProjection() {
        try {
            currentMediaProjection?.stop()
        } catch (_: Exception) {}
        currentMediaProjection = null
        _status.value = MediaProjectionStatus.Idle
    }
}
