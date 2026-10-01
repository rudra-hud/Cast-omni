package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casting.CastQualityProfile
import com.example.data.local.DiscoveredDevice
import com.example.data.local.PairingStatus
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricSky
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

@Composable
fun CastSettingsScreen(
    currentProfile: CastQualityProfile,
    devices: List<DiscoveredDevice>,
    onSelectQualityProfile: () -> Unit,
    onOpenPermissionWizard: () -> Unit,
    onClearHistory: () -> Unit
) {
    var thermalGuardEnabled by remember { mutableStateOf(true) }
    var audioSyncDelayMs by remember { mutableFloatStateOf(0f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("cast_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Streaming Engine & Latency Configuration
        item {
            Text(
                text = "STREAMING PIPELINE & LATENCY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMutedDark,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectQualityProfile),
                shape = RoundedCornerShape(16.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Active Quality Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text(currentProfile.title, fontSize = 12.sp, color = ElectricSky)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(SurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                }
            }
        }

        // 2. Thermal Guardrail Setting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SunsetAmber.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Thermostat, contentDescription = null, tint = SunsetAmber, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Thermal & Battery Guardrail", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("Keeps 1080p60 for screen mirror to prevent device overheating", fontSize = 11.sp, color = TextSecondaryDark)
                        }
                    }

                    Switch(
                        checked = thermalGuardEnabled,
                        onCheckedChange = { thermalGuardEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = SurfaceElevated
                        )
                    )
                }
            }
        }

        // 3. Audio / Video Sync Delay
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("A/V Bluetooth Lip-Sync Offset", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text("${audioSyncDelayMs.toInt()} ms", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                    Text("Calibrates TV speaker or soundbar delay", fontSize = 11.sp, color = TextMutedDark)

                    Slider(
                        value = audioSyncDelayMs,
                        onValueChange = { audioSyncDelayMs = it },
                        valueRange = -150f..150f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = SurfaceBorder
                        )
                    )
                }
            }
        }

        // 4. Permissions & Onboarding Checklist
        item {
            Text(
                text = "SYSTEM ACCESS & SECURITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMutedDark,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PermissionStatusRow(title = "Local Network & Multicast Discovery", granted = true)
                    PermissionStatusRow(title = "Ongoing Casting Foreground Notification", granted = true)
                    PermissionStatusRow(title = "Android 14+ Screen Capture Consent (Per-Session)", granted = true)

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenPermissionWizard,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = NeonCyan),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Permission Setup Guide", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Saved TV Authentication Tokens (Room Database)
        item {
            Text(
                text = "SAVED SMART TV AUTHENTICATION TOKENS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMutedDark,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val pairedDevices = devices.filter { it.pairingStatus == PairingStatus.AUTHENTICATED }
            if (pairedDevices.isEmpty()) {
                Text("No authenticated TV tokens saved in Room.", fontSize = 12.sp, color = TextMutedDark)
            } else {
                pairedDevices.forEach { dev ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(dev.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                    Text(
                                        "Token: ${dev.authToken?.take(14) ?: "Session Granted"}...",
                                        fontSize = 10.sp,
                                        color = TextMutedDark
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("SECURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }

        // 6. Reset History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClearHistory,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCoral)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear Casting History", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(title: String, granted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 12.sp, color = TextSecondaryDark)
        }

        Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
    }
}
