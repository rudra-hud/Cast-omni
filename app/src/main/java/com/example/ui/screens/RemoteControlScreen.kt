package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DiscoveredDevice
import com.example.data.local.PairingStatus
import com.example.data.local.TvAppItem
import com.example.remote.RemoteKey
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.KindleGold
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

enum class RemoteInputMode {
    DPAD_BUTTONS,
    TOUCHPAD_SURFACE
}

@Composable
fun RemoteControlScreen(
    devices: List<DiscoveredDevice>,
    currentDevice: DiscoveredDevice?,
    tvApps: List<TvAppItem>,
    onSelectDevice: (DiscoveredDevice) -> Unit,
    onSendKey: (RemoteKey) -> Unit,
    onLaunchTvApp: (TvAppItem) -> Unit,
    onLaunchKindleMode: () -> Unit
) {
    var inputMode by remember { mutableStateOf(RemoteInputMode.DPAD_BUTTONS) }
    val activeDevice = currentDevice ?: devices.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("remote_control_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Device Picker Carousel
        item {
            Column {
                Text(
                    text = "TARGET TELEVISION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(devices) { dev ->
                        val isSelected = activeDevice?.id == dev.id
                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SurfaceElevated else SurfaceDark)
                                .testTag("select_device_${dev.id}"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else SurfaceBorder),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) SurfaceElevated else SurfaceDark),
                            onClick = { onSelectDevice(dev) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (dev.pairingStatus == PairingStatus.AUTHENTICATED) SuccessGreen else SunsetAmber,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dev.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonCyan else TextPrimaryDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. TV Header & Pairing Status Banner
        item {
            if (activeDevice != null) {
                TvHeaderCard(device = activeDevice, onPower = { onSendKey(RemoteKey.POWER) }, onInput = { onSendKey(RemoteKey.INPUT_SOURCE) })
            }
        }

        // 3. Mode Toggle (D-Pad vs Touchpad)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevated, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeSwitchButton(
                    title = "Directional D-Pad",
                    icon = Icons.Default.Tv,
                    isSelected = inputMode == RemoteInputMode.DPAD_BUTTONS,
                    modifier = Modifier.weight(1f),
                    onClick = { inputMode = RemoteInputMode.DPAD_BUTTONS }
                )

                ModeSwitchButton(
                    title = "Gesture Touchpad",
                    icon = Icons.Default.TouchApp,
                    isSelected = inputMode == RemoteInputMode.TOUCHPAD_SURFACE,
                    modifier = Modifier.weight(1f),
                    onClick = { inputMode = RemoteInputMode.TOUCHPAD_SURFACE }
                )
            }
        }

        // 4. Primary Input Controller (Touchpad or D-Pad)
        item {
            when (inputMode) {
                RemoteInputMode.DPAD_BUTTONS -> {
                    DPadController(onSendKey = onSendKey)
                }
                RemoteInputMode.TOUCHPAD_SURFACE -> {
                    TouchpadController(
                        onTap = { onSendKey(RemoteKey.OK) },
                        onSwipeUp = { onSendKey(RemoteKey.UP) },
                        onSwipeDown = { onSendKey(RemoteKey.DOWN) },
                        onSwipeLeft = { onSendKey(RemoteKey.LEFT) },
                        onSwipeRight = { onSendKey(RemoteKey.RIGHT) }
                    )
                }
            }
        }

        // 5. Navigation Row: Back, Home, Menu
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RemoteNavButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    label = "Back",
                    modifier = Modifier.weight(1f),
                    onClick = { onSendKey(RemoteKey.BACK) }
                )
                RemoteNavButton(
                    icon = Icons.Default.Home,
                    label = "Home",
                    modifier = Modifier.weight(1f),
                    onClick = { onSendKey(RemoteKey.HOME) }
                )
                RemoteNavButton(
                    icon = Icons.Default.Menu,
                    label = "Menu",
                    modifier = Modifier.weight(1f),
                    onClick = { onSendKey(RemoteKey.MENU) }
                )
            }
        }

        // 6. Audio Volume & Channel Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Volume Column
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("VOLUME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(
                            onClick = { onSendKey(RemoteKey.VOL_UP) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceElevated, CircleShape)
                                .testTag("vol_up_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Vol +", tint = NeonCyan)
                        }

                        IconButton(
                            onClick = { onSendKey(RemoteKey.MUTE) },
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .size(36.dp)
                                .background(SurfaceElevated, CircleShape)
                        ) {
                            Icon(Icons.Default.VolumeMute, contentDescription = "Mute", tint = SunsetAmber, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { onSendKey(RemoteKey.VOL_DOWN) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceElevated, CircleShape)
                                .testTag("vol_down_button")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Vol -", tint = NeonCyan)
                        }
                    }
                }

                // Media Play/Pause Center
                Card(
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("PLAYBACK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                        Spacer(modifier = Modifier.height(24.dp))
                        IconButton(
                            onClick = { onSendKey(RemoteKey.PLAY_PAUSE) },
                            modifier = Modifier
                                .size(56.dp)
                                .background(NeonCyan, CircleShape)
                                .testTag("play_pause_remote_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = DeepObsidian, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Channel Switcher Column
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CHANNEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(
                            onClick = { onSendKey(RemoteKey.CHANNEL_UP) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceElevated, CircleShape)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "CH +", tint = ElectricSky)
                        }

                        Spacer(modifier = Modifier.height(44.dp))

                        IconButton(
                            onClick = { onSendKey(RemoteKey.CHANNEL_DOWN) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceElevated, CircleShape)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "CH -", tint = ElectricSky)
                        }
                    }
                }
            }
        }

        // 7. TV App Shortcuts Quick Launch
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1-TAP TV APP SHORTCUTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedDark,
                        letterSpacing = 1.sp
                    )
                    Text("Direct TV Launch", fontSize = 11.sp, color = NeonCyan)
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Kindle Mode Card (Highlighted Local-to-Mirror action)
                    item {
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .testTag("remote_shortcut_kindle"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, KindleGold),
                            onClick = onLaunchKindleMode
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(KindleGold.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = "Kindle", tint = KindleGold, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Kindle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                Text("Reading Mirror", fontSize = 9.sp, color = KindleGold)
                            }
                        }
                    }

                    items(tvApps.filter { it.id != "kindle" }) { app ->
                        Card(
                            modifier = Modifier
                                .width(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .testTag("remote_shortcut_${app.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, SurfaceBorder),
                            onClick = { onLaunchTvApp(app) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SurfaceElevated, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.name.take(2).uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(app.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark, maxLines = 1)
                                Text("TV App", fontSize = 9.sp, color = TextMutedDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvHeaderCard(
    device: DiscoveredDevice,
    onPower: () -> Unit,
    onInput: () -> Unit
) {
    val brandColor = getBrandColor(device.brand)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(brandColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Tv, contentDescription = null, tint = brandColor, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(device.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (device.pairingStatus == PairingStatus.AUTHENTICATED) SuccessGreen else SunsetAmber,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (device.pairingStatus == PairingStatus.AUTHENTICATED) "Authenticated" else "Pairing Required",
                            fontSize = 11.sp,
                            color = if (device.pairingStatus == PairingStatus.AUTHENTICATED) SuccessGreen else SunsetAmber
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onInput,
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceElevated, CircleShape)
                ) {
                    Icon(Icons.Default.Input, contentDescription = "Input Source", tint = ElectricSky, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onPower,
                    modifier = Modifier
                        .size(40.dp)
                        .background(NeonCoral.copy(alpha = 0.15f), CircleShape)
                        .testTag("power_button")
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = NeonCoral, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ModeSwitchButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonCyan else Color.Transparent)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) DeepObsidian else TextSecondaryDark, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DeepObsidian else TextSecondaryDark
            )
        }
    }
}

@Composable
private fun DPadController(onSendKey: (RemoteKey) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dpad_controller"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up Button
            IconButton(
                onClick = { onSendKey(RemoteKey.UP) },
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceElevated, RoundedCornerShape(16.dp))
                    .testTag("dpad_up")
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = NeonCyan, modifier = Modifier.size(30.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle Row: Left, OK, Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onSendKey(RemoteKey.LEFT) },
                    modifier = Modifier
                        .size(56.dp)
                        .background(SurfaceElevated, RoundedCornerShape(16.dp))
                        .testTag("dpad_left")
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = NeonCyan, modifier = Modifier.size(30.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Center OK Button
                IconButton(
                    onClick = { onSendKey(RemoteKey.OK) },
                    modifier = Modifier
                        .size(68.dp)
                        .background(NeonCyan, CircleShape)
                        .testTag("dpad_ok")
                ) {
                    Text("OK", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DeepObsidian)
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { onSendKey(RemoteKey.RIGHT) },
                    modifier = Modifier
                        .size(56.dp)
                        .background(SurfaceElevated, RoundedCornerShape(16.dp))
                        .testTag("dpad_right")
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = NeonCyan, modifier = Modifier.size(30.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Down Button
            IconButton(
                onClick = { onSendKey(RemoteKey.DOWN) },
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceElevated, RoundedCornerShape(16.dp))
                    .testTag("dpad_down")
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = NeonCyan, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun TouchpadController(
    onTap: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .testTag("touchpad_controller"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(SurfaceElevated, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {},
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val (dx, dy) = dragAmount
                            if (Math.abs(dx) > Math.abs(dy)) {
                                if (dx > 25) onSwipeRight()
                                else if (dx < -25) onSwipeLeft()
                            } else {
                                if (dy > 25) onSwipeDown()
                                else if (dy < -25) onSwipeUp()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = NeonCyan.copy(alpha = 0.6f),
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Swipe to Navigate • Tap to Select", fontSize = 12.sp, color = TextSecondaryDark)
                Text("Smart TV Mouse / Cursor Trackpad", fontSize = 10.sp, color = TextMutedDark)
            }
        }
    }
}

@Composable
private fun RemoteNavButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("nav_btn_${label.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = TextPrimaryDark, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = TextSecondaryDark, fontWeight = FontWeight.Medium)
        }
    }
}
