package com.example.data.local

import kotlinx.coroutines.flow.Flow

class CastRepository(private val dao: CastDao) {

    val allDevices: Flow<List<DiscoveredDevice>> = dao.getAllDevices()
    val recentSessions: Flow<List<CastSessionRecord>> = dao.getRecentSessions()
    val tvApps: Flow<List<TvAppItem>> = dao.getAllTvApps()

    suspend fun saveDevice(device: DiscoveredDevice) = dao.insertOrUpdateDevice(device)
    suspend fun saveDevices(devices: List<DiscoveredDevice>) = dao.insertDevices(devices)
    suspend fun toggleFavorite(id: String, isFav: Boolean) = dao.toggleFavorite(id, isFav)
    suspend fun markLastConnected(id: String) = dao.updateLastConnected(id, System.currentTimeMillis())
    suspend fun updatePairing(id: String, status: PairingStatus, token: String?) = dao.updatePairingAuth(id, status, token)
    suspend fun deleteDevice(id: String) = dao.deleteDevice(id)

    suspend fun recordSession(session: CastSessionRecord) = dao.insertSession(session)
    suspend fun clearHistory() = dao.clearHistory()

    suspend fun seedDefaultAppsIfEmpty() {
        val defaults = listOf(
            TvAppItem(
                id = "kindle",
                name = "Kindle (Reading Mode)",
                category = "E-Books & Documents",
                iconKey = "kindle",
                isNativeTvApp = false, // Triggers Local-App-to-Mirror flow
                androidPackageName = "com.amazon.kindle",
                isPinned = true,
                orderIndex = 0
            ),
            TvAppItem(
                id = "youtube",
                name = "YouTube",
                category = "Video & Live",
                iconKey = "youtube",
                isNativeTvApp = true,
                tizenAppId = "org.tizen.youtube",
                webOsAppId = "youtube.leanback.v4",
                rokuAppId = "800",
                androidPackageName = "com.google.android.youtube.tv",
                isPinned = true,
                orderIndex = 1
            ),
            TvAppItem(
                id = "netflix",
                name = "Netflix",
                category = "Movies & TV",
                iconKey = "netflix",
                isNativeTvApp = true,
                tizenAppId = "org.tizen.netflix-app",
                webOsAppId = "netflix",
                rokuAppId = "12",
                androidPackageName = "com.netflix.ninja",
                isPinned = true,
                orderIndex = 2
            ),
            TvAppItem(
                id = "prime",
                name = "Prime Video",
                category = "Movies & TV",
                iconKey = "prime",
                isNativeTvApp = true,
                tizenAppId = "org.tizen.prime-video",
                webOsAppId = "amazon",
                rokuAppId = "13",
                androidPackageName = "com.amazon.amazonvideo.livingroom",
                isPinned = true,
                orderIndex = 3
            ),
            TvAppItem(
                id = "disney",
                name = "Disney+",
                category = "Family & Movies",
                iconKey = "disney",
                isNativeTvApp = true,
                tizenAppId = "org.tizen.disney-plus",
                webOsAppId = "com.disney.disneyplus-prod",
                rokuAppId = "291097",
                androidPackageName = "com.disney.disneyplus",
                isPinned = true,
                orderIndex = 4
            ),
            TvAppItem(
                id = "spotify",
                name = "Spotify",
                category = "Music & Audio",
                iconKey = "spotify",
                isNativeTvApp = true,
                tizenAppId = "org.tizen.spotify",
                webOsAppId = "spotify-beehive",
                rokuAppId = "19977",
                androidPackageName = "com.spotify.tv.android",
                isPinned = true,
                orderIndex = 5
            ),
            TvAppItem(
                id = "browser",
                name = "TV Web Browser",
                category = "Utility & Web",
                iconKey = "browser",
                isNativeTvApp = true,
                tizenAppId = "org.tizen.browser",
                webOsAppId = "com.webos.app.browser",
                rokuAppId = "58066",
                androidPackageName = "com.opera.tv.browser",
                isPinned = true,
                orderIndex = 6
            ),
            TvAppItem(
                id = "twitch",
                name = "Twitch",
                category = "Gaming & Streams",
                iconKey = "twitch",
                isNativeTvApp = true,
                tizenAppId = "org.tizen.twitch",
                webOsAppId = "tv.twitch.tv",
                rokuAppId = "40040",
                androidPackageName = "tv.twitch.android.app",
                isPinned = false,
                orderIndex = 7
            ),
            TvAppItem(
                id = "plex",
                name = "Plex Media Server",
                category = "Home Media",
                iconKey = "plex",
                isNativeTvApp = true,
                tizenAppId = "org.tizen.plex",
                webOsAppId = "cdp-30",
                rokuAppId = "13535",
                androidPackageName = "com.plexapp.android",
                isPinned = false,
                orderIndex = 8
            )
        )
        dao.insertTvApps(defaults)
    }
}
