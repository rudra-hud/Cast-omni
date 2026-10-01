package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PairingStatus {
    AUTHENTICATED,
    PAIRING_PROMPT_SENT,
    REQUIRES_PIN,
    UNPAIRED
}

@Entity(tableName = "discovered_devices")
data class DiscoveredDevice(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String, // Samsung, LG, Sony, Roku, Fire TV, Google Cast, DLNA
    val ipAddress: String,
    val port: Int,
    val protocol: String,
    val model: String = "Smart TV 4K",
    val isFavorite: Boolean = false,
    val lastConnected: Long = 0L,
    val supports4K: Boolean = true,
    val avgLatencyMs: Int = 24,
    val pairingStatus: PairingStatus = PairingStatus.UNPAIRED,
    val authToken: String? = null
)

@Entity(tableName = "cast_sessions")
data class CastSessionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceName: String,
    val deviceBrand: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val resolutionMode: String = "1080p60 Low-Latency",
    val avgLatencyMs: Int = 68,
    val sessionType: String = "Screen Mirror"
)

@Entity(tableName = "tv_apps")
data class TvAppItem(
    @PrimaryKey val id: String,
    val name: String,
    val category: String = "Streaming",
    val iconKey: String,
    val isNativeTvApp: Boolean = true, // True for Netflix/YouTube on TV OS, False for Kindle (Local-to-Mirror)
    val tizenAppId: String? = null,
    val webOsAppId: String? = null,
    val rokuAppId: String? = null,
    val androidPackageName: String? = null,
    val isPinned: Boolean = true,
    val orderIndex: Int = 0
)
