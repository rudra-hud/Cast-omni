package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casting.CastQualityProfile
import com.example.casting.CastState
import com.example.casting.CastType
import com.example.data.local.CastSessionRecord
import com.example.data.local.DiscoveredDevice
import com.example.discovery.NetworkDiagnosticInfo
import com.example.ui.components.AddDeviceDialog
import com.example.ui.theme.CastGreen
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.FireOrange
import com.example.ui.theme.KindleGold
import com.example.ui.theme.LgRed
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RokuPurple
import com.example.ui.theme.SamsungBlue
import com.example.ui.theme.SonySilver
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun CastHomeScreen(
    devices: List<DiscoveredDevice>,
    castState: CastState,
    selectedProfile: CastQualityProfile,
    networkInfo: NetworkDiagnosticInfo,
    isScanning: Boolean,
    scanProgress: Float,
    history: List<CastSessionRecord>,
    onScanRequested: () -> Unit,
    onStartCast: (DiscoveredDevice, CastType) -> Unit,
    onStopCast: () -> Unit,
    onTogglePause: () -> Unit,
    onToggleMute: () -> Unit,
    onSelectQualityProfile: () -> Unit,
    onToggleFavorite: (DiscoveredDevice) -> Unit,
    onNavigateToRemote: (DiscoveredDevice) -> Unit,
    onLaunchKindleMode: (DiscoveredDevice?) -> Unit,
    onNavigateToMedia: () -> Unit,
    onAddManualDevice: (name: String, brand: String, ip: String, port: Int) -> Unit,
    onDeleteDevice: (DiscoveredDevice) -> Unit
) {
    var showAddDeviceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("cast_home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Active Cast HUD or Ready Hero
        item {
            when (castState) {
                is CastState.Active -> {
                    ActiveCastHudCard(
                        castState = castState,
                        onStopCast = onStopCast,
                        onTogglePause = onTogglePause,
                        onToggleMute = onToggleMute,
                        onSelectQuality = onSelectQualityProfile
                    )
                }
                is CastState.Connecting -> {
                    ConnectingCard(device = castState.device)
                }
                else -> {
                    HeroReadyToCastCard(
                        selectedProfile = selectedProfile,
                        onSelectQuality = onSelectQualityProfile,
                        onQuickMirror = {
                            devices.firstOrNull()?.let { onStartCast(it, CastType.SCREEN_MIRROR) }
                        }
                    )
                }
            }
        }

        // 2. Quick Action Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip(
                    title = "Mirror Screen",
                    subtitle = "Low-Lag 60fps",
                    icon = Icons.Default.Cast,
                    tint = NeonCyan,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val dev = devices.firstOrNull { it.isFavorite } ?: devices.firstOrNull()
                        dev?.let { onStartCast(it, CastType.SCREEN_MIRROR) }
                    }
                )

                QuickActionChip(
                    title = "Kindle Reader",
                    subtitle = "TV Mirror Mode",
                    icon = Icons.Default.MenuBook,
                    tint = KindleGold,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val dev = devices.firstOrNull { it.isFavorite } ?: devices.firstOrNull()
                        onLaunchKindleMode(dev)
                    }
                )

                QuickActionChip(
                    title = "4K Media",
                    subtitle = "Direct Stream",
                    icon = Icons.Default.HighQuality,
                    tint = ElectricSky,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMedia
                )
            }
        }

        // 3. Wi-Fi & Discovery Radar Bar
        item {
            WifiDiscoveryRadarCard(
                networkInfo = networkInfo,
                isScanning = isScanning,
                scanProgress = scanProgress,
                onRescan = onScanRequested
            )
        }

        // 4. Discovered TV Screens Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Discovered Screens (${devices.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showAddDeviceDialog = true }
                        .background(SurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add by IP", tint = NeonCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add by IP", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. Discovered TV Device Cards
        if (devices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Scanning local network for Smart TVs...", color = TextSecondaryDark, fontSize = 13.sp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SurfaceElevated, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tv, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No Smart TVs detected yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text(
                                "Make sure your Smart TV is powered on and connected to this Wi-Fi. You can also connect directly by entering its IP address.",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = onScanRequested,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rescan", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { showAddDeviceDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepObsidian)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add TV by IP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            items(devices, key = { it.id }) { device ->
                DiscoveredDeviceCard(
                    device = device,
                    isCurrentlyCasting = (castState as? CastState.Active)?.device?.id == device.id,
                    onStartCast = { onStartCast(device, CastType.SCREEN_MIRROR) },
                    onOpenRemote = { onNavigateToRemote(device) },
                    onToggleFavorite = { onToggleFavorite(device) },
                    onDelete = { onDeleteDevice(device) }
                )
            }
        }

        // 6. Recent Cast History
        if (history.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recent Sessions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                }
            }

            items(history.take(4)) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(session.deviceName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text(
                                "${session.resolutionMode} • ${session.avgLatencyMs}ms avg",
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }
                        Text(
                            "${session.durationSeconds / 60}m ${session.durationSeconds % 60}s",
                            fontSize = 12.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showAddDeviceDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDeviceDialog = false },
            onAddDevice = onAddManualDevice
        )
    }
}

@Composable
private fun ActiveCastHudCard(
    castState: CastState.Active,
    onStopCast: () -> Unit,
    onTogglePause: () -> Unit,
    onToggleMute: () -> Unit,
    onSelectQuality: () -> Unit
) {
    val telemetry = castState.telemetry
    val brandColor = getBrandColor(castState.device.brand)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_cast_hud_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.5.dp, NeonCyan)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(SuccessGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE CASTING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clickable(onClick = onSelectQuality)
                        .background(brandColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = castState.profile.resolutionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = castState.device.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "${castState.device.model} • ${castState.profile.transportType}",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Telemetry Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidian, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TelemetryItem(label = "LATENCY", value = if (telemetry.latencyMs > 0) "${telemetry.latencyMs} ms" else "--", tint = NeonCyan)
                TelemetryItem(label = "BITRATE", value = "${telemetry.currentBitrateMbps} Mbps", tint = ElectricSky)
                TelemetryItem(label = "FRAMERATE", value = "${telemetry.currentFps} FPS", tint = SunsetAmber)
                TelemetryItem(label = "TEMP", value = "${telemetry.phoneTempCelsius}°C", tint = TextSecondaryDark)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cast Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTogglePause,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = TextPrimaryDark
                    )
                ) {
                    Icon(
                        imageVector = if (telemetry.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (telemetry.isPaused) "Resume" else "Pause", fontSize = 12.sp)
                }

                Button(
                    onClick = onToggleMute,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = TextPrimaryDark
                    )
                ) {
                    Icon(
                        imageVector = if (telemetry.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (telemetry.isMuted) "Unmute" else "Mute", fontSize = 12.sp)
                }

                Button(
                    onClick = onStopCast,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("stop_cast_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCoral,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Disconnect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TelemetryItem(label: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = TextMutedDark, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable
private fun ConnectingCard(device: DiscoveredDevice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, NeonCyan)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text("Establishing Protocol Handshake...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text("Negotiating low-latency buffer with ${device.name}", fontSize = 12.sp, color = TextSecondaryDark)
        }
    }
}

@Composable
private fun HeroReadyToCastCard(
    selectedProfile: CastQualityProfile,
    onSelectQuality: () -> Unit,
    onQuickMirror: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ready_to_cast_hero"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent),
                        radius = 450f
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(NeonCyan.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("4K LOW-LATENCY READY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }

                    Box(
                        modifier = Modifier
                            .clickable(onClick = onSelectQuality)
                            .background(SurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = SunsetAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedProfile.resolutionLabel, fontSize = 11.sp, color = TextPrimaryDark, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Cast to Any Smart TV",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryDark
                )

                Text(
                    text = "Seamless connection for Samsung Tizen, LG webOS, Google TV, Roku, Fire TV & Apple TV receivers.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Button(
                    onClick = onQuickMirror,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("one_tap_mirror_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DeepObsidian
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CastConnected, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1-Tap Mirror to Nearest TV", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("quick_action_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(subtitle, fontSize = 10.sp, color = TextMutedDark)
        }
    }
}

@Composable
private fun WifiDiscoveryRadarCard(
    networkInfo: NetworkDiagnosticInfo,
    isScanning: Boolean,
    scanProgress: Float,
    onRescan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(networkInfo.ssid.ifEmpty { "Connected Network" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(
                            "${networkInfo.linkSpeedMbps} Mbps • ${networkInfo.localIp}",
                            fontSize = 11.sp,
                            color = TextMutedDark
                        )
                    }
                }

                IconButton(
                    onClick = onRescan,
                    modifier = Modifier.size(32.dp).testTag("rescan_network_button")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Rescan", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (isScanning) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = NeonCyan,
                    trackColor = SurfaceBorder
                )
            }
        }
    }
}

@Composable
private fun DiscoveredDeviceCard(
    device: DiscoveredDevice,
    isCurrentlyCasting: Boolean,
    onStartCast: () -> Unit,
    onOpenRemote: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val brandColor = getBrandColor(device.brand)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyCasting) SurfaceElevated else SurfaceDark
        ),
        border = BorderStroke(
            width = if (isCurrentlyCasting) 1.5.dp else 1.dp,
            color = if (isCurrentlyCasting) NeonCyan else SurfaceBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(brandColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = device.brand.take(2).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = device.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }

                        Text(
                            text = "${device.brand} • ${device.protocol}",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (device.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (device.isFavorite) NeonCoral else TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove",
                            tint = TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecBadge(text = if (device.avgLatencyMs > 0) "${device.avgLatencyMs}ms ping" else "Direct IP", tint = NeonCyan)
                if (device.supports4K) {
                    SpecBadge(text = "4K UHD", tint = SunsetAmber)
                }
                SpecBadge(text = device.ipAddress, tint = TextMutedDark)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartCast,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("cast_btn_${device.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlyCasting) NeonCoral else NeonCyan,
                        contentColor = if (isCurrentlyCasting) Color.White else DeepObsidian
                    )
                ) {
                    Icon(Icons.Default.Cast, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isCurrentlyCasting) "Active Cast" else "Mirror Cast",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenRemote,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("remote_btn_${device.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryDark)
                ) {
                    Icon(Icons.Default.SettingsRemote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TV Remote", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(text: String, tint: Color) {
    Box(
        modifier = Modifier
            .background(SurfaceElevated, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}

fun getBrandColor(brand: String): Color {
    return when {
        brand.contains("Samsung", ignoreCase = true) -> SamsungBlue
        brand.contains("LG", ignoreCase = true) -> LgRed
        brand.contains("Roku", ignoreCase = true) -> RokuPurple
        brand.contains("Sony", ignoreCase = true) -> SonySilver
        brand.contains("Fire", ignoreCase = true) -> FireOrange
        brand.contains("Google", ignoreCase = true) || brand.contains("Cast", ignoreCase = true) -> CastGreen
        else -> NeonCyan
    }
}
