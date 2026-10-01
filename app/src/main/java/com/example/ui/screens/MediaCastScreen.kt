package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casting.CastState
import com.example.casting.CastType
import com.example.data.local.DiscoveredDevice
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

data class LocalMediaItem(
    val uri: Uri,
    val name: String,
    val type: CastType
)

@Composable
fun MediaCastScreen(
    currentDevice: DiscoveredDevice?,
    castState: CastState,
    onStartCast: (DiscoveredDevice, CastType) -> Unit,
    onStopCast: () -> Unit
) {
    val context = LocalContext.current
    val userSelectedMedia = remember { mutableStateListOf<LocalMediaItem>() }
    var activePlaybackTitle by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0.0f) }

    // Android Photo Picker for Videos & Photos (zero broad storage permission required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected Media"
            val isVideo = context.contentResolver.getType(uri)?.startsWith("video") == true
            val type = if (isVideo) CastType.VIDEO_4K_DIRECT else CastType.PHOTO_SLIDESHOW
            val item = LocalMediaItem(uri, fileName, type)
            userSelectedMedia.add(0, item)
            activePlaybackTitle = fileName
            isPlaying = true
            currentDevice?.let { onStartCast(it, type) }
        }
    }

    // Audio Document Picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Audio Track"
            val item = LocalMediaItem(uri, fileName, CastType.AUDIO_DIRECT)
            userSelectedMedia.add(0, item)
            activePlaybackTitle = fileName
            isPlaying = true
            currentDevice?.let { onStartCast(it, CastType.AUDIO_DIRECT) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("media_cast_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Direct Hardware Streaming Header
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HighQuality, contentDescription = null, tint = SunsetAmber, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Direct 4K UHD Media Streamer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }

                        Box(
                            modifier = Modifier
                                .background(SunsetAmber.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("TV HARDWARE DECODED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SunsetAmber)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Direct streaming transmits your original video and photo files directly to ${currentDevice?.name ?: "your Smart TV"}. The TV's native decoder handles the stream at full original resolution with zero re-encoding on your phone.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Media Picker Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("pick_video_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = SunsetAmber),
                    border = BorderStroke(1.dp, SunsetAmber.copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("pick_photo_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = NeonCyan),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Photos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        audioPickerLauncher.launch(arrayOf("audio/*"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("pick_audio_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = ElectricSky),
                    border = BorderStroke(1.dp, ElectricSky.copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Active Playback HUD (when media is selected)
        activePlaybackTitle?.let { title ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, NeonCyan)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NOW CASTING TO TV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyan, letterSpacing = 1.sp)
                            IconButton(onClick = { activePlaybackTitle = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text("Direct 4K Stream • Original Bitrate • Hardware Decoded", fontSize = 11.sp, color = TextSecondaryDark)

                        Spacer(modifier = Modifier.height(14.dp))

                        Slider(
                            value = playbackProgress,
                            onValueChange = { playbackProgress = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = SurfaceBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { playbackProgress = (playbackProgress - 0.05f).coerceAtLeast(0f) }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = TextPrimaryDark)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(NeonCyan, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = DeepObsidian,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(onClick = { playbackProgress = (playbackProgress + 0.05f).coerceAtMost(1f) }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = TextPrimaryDark)
                            }
                        }
                    }
                }
            }
        }

        // 4. Selected Media Queue or Empty State
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DEVICE MEDIA FOR CASTING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedDark,
                    letterSpacing = 1.sp
                )
                if (userSelectedMedia.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        fontSize = 11.sp,
                        color = NeonCoral,
                        modifier = Modifier.clickable { userSelectedMedia.clear() }
                    )
                }
            }
        }

        if (userSelectedMedia.isEmpty()) {
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(SurfaceElevated, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No media selected yet", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(
                            "Select any 4K video, gallery photo, or audio file from your device to stream directly to your TV.",
                            fontSize = 12.sp,
                            color = TextSecondaryDark,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepObsidian)
                        ) {
                            Text("Open Device Gallery", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(userSelectedMedia) { media ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            activePlaybackTitle = media.name
                            isPlaying = true
                            currentDevice?.let { onStartCast(it, media.type) }
                        }
                        .testTag("media_item_${media.name}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    .background(
                                        when (media.type) {
                                            CastType.VIDEO_4K_DIRECT -> SunsetAmber.copy(alpha = 0.15f)
                                            CastType.PHOTO_SLIDESHOW -> NeonCyan.copy(alpha = 0.15f)
                                            else -> ElectricSky.copy(alpha = 0.15f)
                                        },
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (media.type) {
                                        CastType.VIDEO_4K_DIRECT -> Icons.Default.Movie
                                        CastType.PHOTO_SLIDESHOW -> Icons.Default.Image
                                        else -> Icons.Default.Audiotrack
                                    },
                                    contentDescription = null,
                                    tint = when (media.type) {
                                        CastType.VIDEO_4K_DIRECT -> SunsetAmber
                                        CastType.PHOTO_SLIDESHOW -> NeonCyan
                                        else -> ElectricSky
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(media.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark, maxLines = 1)
                                Text(
                                    when (media.type) {
                                        CastType.VIDEO_4K_DIRECT -> "Video File • Direct 4K"
                                        CastType.PHOTO_SLIDESHOW -> "Photo File • Direct Display"
                                        else -> "Audio Track • Direct Stream"
                                    },
                                    fontSize = 11.sp,
                                    color = TextMutedDark
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(SurfaceElevated, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cast, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cast", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}
