package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CastDao {
    @Query("SELECT * FROM discovered_devices ORDER BY isFavorite DESC, lastConnected DESC, name ASC")
    fun getAllDevices(): Flow<List<DiscoveredDevice>>

    @Query("SELECT * FROM discovered_devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: String): DiscoveredDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: DiscoveredDevice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DiscoveredDevice>)

    @Update
    suspend fun updateDevice(device: DiscoveredDevice)

    @Query("DELETE FROM discovered_devices WHERE id = :id")
    suspend fun deleteDevice(id: String)

    @Query("UPDATE discovered_devices SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE discovered_devices SET lastConnected = :timestamp WHERE id = :id")
    suspend fun updateLastConnected(id: String, timestamp: Long)

    @Query("UPDATE discovered_devices SET pairingStatus = :status, authToken = :token WHERE id = :id")
    suspend fun updatePairingAuth(id: String, status: PairingStatus, token: String?)

    // Cast Sessions History
    @Query("SELECT * FROM cast_sessions ORDER BY timestamp DESC LIMIT 30")
    fun getRecentSessions(): Flow<List<CastSessionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CastSessionRecord)

    @Query("DELETE FROM cast_sessions")
    suspend fun clearHistory()

    // TV Apps
    @Query("SELECT * FROM tv_apps ORDER BY orderIndex ASC")
    fun getAllTvApps(): Flow<List<TvAppItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTvApps(apps: List<TvAppItem>)

    @Update
    suspend fun updateTvApp(app: TvAppItem)
}
