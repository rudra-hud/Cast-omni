package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casting.CastQualityProfile
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatencyQualitySheet(
    currentProfile: CastQualityProfile,
    onSelectProfile: (CastQualityProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .background(SurfaceBorder, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("latency_quality_sheet")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Streaming Quality & Latency Engine",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Protocol-calibrated transport profiles",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            ProfileItem(
                profile = CastQualityProfile.LOW_LATENCY_1080P60,
                icon = Icons.Default.Bolt,
                isSelected = currentProfile == CastQualityProfile.LOW_LATENCY_1080P60,
                badge = "RECOMMENDED FOR MIRRORING",
                onSelect = { onSelectProfile(CastQualityProfile.LOW_LATENCY_1080P60) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileItem(
                profile = CastQualityProfile.DIRECT_4K_MEDIA,
                icon = Icons.Default.HighQuality,
                isSelected = currentProfile == CastQualityProfile.DIRECT_4K_MEDIA,
                badge = "PRISTINE 4K (PHOTOS & VIDEOS)",
                onSelect = { onSelectProfile(CastQualityProfile.DIRECT_4K_MEDIA) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileItem(
                profile = CastQualityProfile.KINDLE_READING_MODE,
                icon = Icons.Default.MenuBook,
                isSelected = currentProfile == CastQualityProfile.KINDLE_READING_MODE,
                badge = "SHARP TEXT & LOW POWER",
                onSelect = { onSelectProfile(CastQualityProfile.KINDLE_READING_MODE) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileItem(
                profile = CastQualityProfile.SMART_ADAPTIVE,
                icon = Icons.Default.Speed,
                isSelected = currentProfile == CastQualityProfile.SMART_ADAPTIVE,
                badge = "DYNAMIC WI-FI TUNING",
                onSelect = { onSelectProfile(CastQualityProfile.SMART_ADAPTIVE) }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ProfileItem(
    profile: CastQualityProfile,
    icon: ImageVector,
    isSelected: Boolean,
    badge: String,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) NeonCyan else SurfaceBorder
    val bgColor = if (isSelected) SurfaceElevated else SurfaceDark

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("profile_item_${profile.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            if (isSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceElevated,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) NeonCyan else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    Text(
                        text = "${profile.resolutionLabel} • ${profile.transportType}",
                        fontSize = 12.sp,
                        color = ElectricSky
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidian.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = profile.latencyLabel,
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = profile.thermalImpact,
                    fontSize = 11.sp,
                    color = TextMutedDark
                )
            }
        }
    }
}
