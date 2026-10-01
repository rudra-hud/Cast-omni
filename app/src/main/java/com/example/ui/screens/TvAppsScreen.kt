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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DiscoveredDevice
import com.example.data.local.TvAppItem
import com.example.ui.KindleReadingSettings
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.KindleGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun TvAppsScreen(
    tvApps: List<TvAppItem>,
    currentDevice: DiscoveredDevice?,
    kindleSettings: KindleReadingSettings,
    onLaunchApp: (TvAppItem) -> Unit,
    onLaunchKindleMode: () -> Unit,
    onUpdateKindleContrast: (String) -> Unit,
    onUpdateKindleFontSize: (Float) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Movies & TV", "Video & Live", "Music & Audio", "Utility & Web")

    val filteredApps = if (selectedCategory == "All") {
        tvApps.filter { it.id != "kindle" }
    } else {
        tvApps.filter { it.id != "kindle" && (it.category == selectedCategory || it.category.contains(selectedCategory)) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("tv_apps_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Target TV Status Pill
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Target TV: ${currentDevice?.name ?: "All Smart TVs"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryDark
                        )
                    }

                    Text(
                        text = currentDevice?.brand ?: "Multi-Brand",
                        fontSize = 11.sp,
                        color = ElectricSky,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. FEATURED: Kindle / E-Book TV Mirroring Showcase (Specialized User Feature)
        item {
            KindleHeroCard(
                settings = kindleSettings,
                onLaunch = onLaunchKindleMode,
                onSelectContrast = onUpdateKindleContrast,
                onFontSizeChange = onUpdateKindleFontSize
            )
        }

        // 3. Category Filter Chips
        item {
            Column {
                Text(
                    text = "SMART TV APPLICATION STORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = DeepObsidian,
                                containerColor = SurfaceDark,
                                labelColor = TextSecondaryDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NeonCyan else SurfaceBorder
                            )
                        )
                    }
                }
            }
        }

        // 4. Native TV Apps Grid
        items(filteredApps.chunked(2)) { appPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (app in appPair) {
                    Box(modifier = Modifier.weight(1f)) {
                        TvAppCard(app = app, onLaunch = { onLaunchApp(app) })
                    }
                }
                if (appPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KindleHeroCard(
    settings: KindleReadingSettings,
    onLaunch: () -> Unit,
    onSelectContrast: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kindle_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.5.dp, KindleGold)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(KindleGold.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(KindleGold.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = KindleGold, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Kindle on Smart TV", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimaryDark)
                            Text("Dedicated Reading Screen Mirror", fontSize = 11.sp, color = KindleGold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(SurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("4K OLED TEXT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Kindle does not offer native apps on webOS or Tizen. OmniCast automatically mirrors your Kindle library to your TV with crisp anti-glare typography and comfortable reading contrast.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Reading Contrast Presets
                Text("TV DISPLAY CONTRAST PRESETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContrastPresetChip(
                        name = "Paper White",
                        isSelected = settings.contrastPreset == "Paper White",
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectContrast("Paper White") }
                    )
                    ContrastPresetChip(
                        name = "Sepia Warm",
                        isSelected = settings.contrastPreset == "Sepia Warm",
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectContrast("Sepia Warm") }
                    )
                    ContrastPresetChip(
                        name = "OLED Night",
                        isSelected = settings.contrastPreset == "OLED Night",
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectContrast("OLED Night") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLaunch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("launch_kindle_mirror_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KindleGold,
                        contentColor = DeepObsidian
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Launch Kindle & Cast to TV", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContrastPresetChip(
    name: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(if (isSelected) SurfaceElevated else SurfaceDark, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) KindleGold else TextSecondaryDark
        )
    }
}

@Composable
private fun TvAppCard(app: TvAppItem, onLaunch: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLaunch)
            .testTag("tv_app_card_${app.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SurfaceElevated, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.name.take(2).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Icon(
                    imageVector = Icons.Default.Launch,
                    contentDescription = "Launch",
                    tint = TextMutedDark,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(app.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(app.category, fontSize = 11.sp, color = TextMutedDark)

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevated, RoundedCornerShape(6.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Launch on TV", fontSize = 10.sp, color = ElectricSky, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
