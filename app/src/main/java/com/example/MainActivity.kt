package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.casting.CastType
import com.example.remote.RemoteKey
import com.example.ui.AppNavDestination
import com.example.ui.CastViewModel
import com.example.ui.components.LatencyQualitySheet
import com.example.ui.components.PairingPinDialog
import com.example.ui.components.PermissionOnboardingDialog
import com.example.ui.components.TvPromptBanner
import com.example.ui.screens.CastHomeScreen
import com.example.ui.screens.CastSettingsScreen
import com.example.ui.screens.MediaCastScreen
import com.example.ui.screens.RemoteControlScreen
import com.example.ui.screens.TvAppsScreen
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: CastViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                // State from ViewModel
                val currentNav by viewModel.currentNavDestination.collectAsStateWithLifecycle()
                val devices by viewModel.devices.collectAsStateWithLifecycle()
                val castState by viewModel.castState.collectAsStateWithLifecycle()
                val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
                val networkInfo by viewModel.networkInfo.collectAsStateWithLifecycle()
                val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
                val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
                val castHistory by viewModel.castHistory.collectAsStateWithLifecycle()
                val tvApps by viewModel.tvApps.collectAsStateWithLifecycle()
                val remoteDevice by viewModel.remoteDevice.collectAsStateWithLifecycle()
                val kindleSettings by viewModel.kindleSettings.collectAsStateWithLifecycle()

                val showPermissionDialog by viewModel.showPermissionDialog.collectAsStateWithLifecycle()
                val activePinDevice by viewModel.activePinDevice.collectAsStateWithLifecycle()
                val pendingTvPromptDevice by viewModel.pendingTvPromptDevice.collectAsStateWithLifecycle()
                val showQualitySheet by viewModel.showQualitySheet.collectAsStateWithLifecycle()

                // Notification Permission launcher (Android 13+)
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.startScanning()
                    }
                }

                // Android 14+ / 15 Screen Projection Consent launcher (Per-session requirement)
                val mediaProjectionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    viewModel.handleCaptureConsentResult(result.resultCode, result.data)
                }

                // Listen for MediaProjection intent requests from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.mediaProjectionRequestIntent.collectLatest { intent ->
                        mediaProjectionLauncher.launch(intent)
                    }
                }

                // Listen for toast messages
                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collectLatest { msg ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }

                // Automated Permission Check upon launch as requested
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasNotification = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasNotification) {
                            viewModel.togglePermissionDialog(true)
                        } else {
                            viewModel.startScanning()
                        }
                    } else {
                        viewModel.startScanning()
                    }
                }

                // BackHandler for secondary screens
                BackHandler(enabled = currentNav != AppNavDestination.CAST_HOME) {
                    viewModel.setNavDestination(AppNavDestination.CAST_HOME)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DeepObsidian,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "OmniCast 4K",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimaryDark,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.toggleQualitySheet(true) },
                                    modifier = Modifier.testTag("quality_profile_header_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Quality Profile",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = DeepObsidian,
                                titleContentColor = TextPrimaryDark
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = SurfaceDark,
                            contentColor = TextPrimaryDark,
                            tonalElevation = 8.dp,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentNav == AppNavDestination.CAST_HOME,
                                onClick = { viewModel.setNavDestination(AppNavDestination.CAST_HOME) },
                                icon = { Icon(Icons.Default.Cast, contentDescription = "Cast") },
                                label = { Text("Cast", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextMutedDark,
                                    unselectedTextColor = TextMutedDark
                                ),
                                modifier = Modifier.testTag("nav_tab_cast")
                            )

                            NavigationBarItem(
                                selected = currentNav == AppNavDestination.REMOTE,
                                onClick = { viewModel.setNavDestination(AppNavDestination.REMOTE) },
                                icon = { Icon(Icons.Default.SettingsRemote, contentDescription = "Remote") },
                                label = { Text("Remote", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextMutedDark,
                                    unselectedTextColor = TextMutedDark
                                ),
                                modifier = Modifier.testTag("nav_tab_remote")
                            )

                            NavigationBarItem(
                                selected = currentNav == AppNavDestination.TV_APPS,
                                onClick = { viewModel.setNavDestination(AppNavDestination.TV_APPS) },
                                icon = { Icon(Icons.Default.Apps, contentDescription = "TV Apps") },
                                label = { Text("TV Apps", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextMutedDark,
                                    unselectedTextColor = TextMutedDark
                                ),
                                modifier = Modifier.testTag("nav_tab_apps")
                            )

                            NavigationBarItem(
                                selected = currentNav == AppNavDestination.MEDIA_4K,
                                onClick = { viewModel.setNavDestination(AppNavDestination.MEDIA_4K) },
                                icon = { Icon(Icons.Default.Movie, contentDescription = "4K Media") },
                                label = { Text("4K Media", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextMutedDark,
                                    unselectedTextColor = TextMutedDark
                                ),
                                modifier = Modifier.testTag("nav_tab_media")
                            )

                            NavigationBarItem(
                                selected = currentNav == AppNavDestination.SETTINGS,
                                onClick = { viewModel.setNavDestination(AppNavDestination.SETTINGS) },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan,
                                    unselectedIconColor = TextMutedDark,
                                    unselectedTextColor = TextMutedDark
                                ),
                                modifier = Modifier.testTag("nav_tab_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentNav) {
                            AppNavDestination.CAST_HOME -> {
                                CastHomeScreen(
                                    devices = devices,
                                    castState = castState,
                                    selectedProfile = selectedProfile,
                                    networkInfo = networkInfo,
                                    isScanning = isScanning,
                                    scanProgress = scanProgress,
                                    history = castHistory,
                                    onScanRequested = { viewModel.startScanning() },
                                    onStartCast = { dev, type -> viewModel.requestStartCasting(dev, type) },
                                    onStopCast = { viewModel.stopCasting() },
                                    onTogglePause = { viewModel.toggleCastPause() },
                                    onToggleMute = { viewModel.toggleCastMute() },
                                    onSelectQualityProfile = { viewModel.toggleQualitySheet(true) },
                                    onToggleFavorite = { dev -> viewModel.toggleDeviceFavorite(dev) },
                                    onNavigateToRemote = { dev ->
                                        viewModel.selectRemoteDevice(dev)
                                        viewModel.setNavDestination(AppNavDestination.REMOTE)
                                    },
                                    onLaunchKindleMode = { dev -> viewModel.launchKindleReadingMode(dev) },
                                    onNavigateToMedia = { viewModel.setNavDestination(AppNavDestination.MEDIA_4K) },
                                    onAddManualDevice = { name, brand, ip, port -> viewModel.addManualDevice(name, brand, ip, port) },
                                    onDeleteDevice = { dev -> viewModel.removeDevice(dev.id) }
                                )
                            }
                            AppNavDestination.REMOTE -> {
                                RemoteControlScreen(
                                    devices = devices,
                                    currentDevice = remoteDevice,
                                    tvApps = tvApps,
                                    onSelectDevice = { dev -> viewModel.selectRemoteDevice(dev) },
                                    onSendKey = { key -> viewModel.sendRemoteKey(key) },
                                    onLaunchTvApp = { app -> viewModel.launchTvApp(app) },
                                    onLaunchKindleMode = { viewModel.launchKindleReadingMode(remoteDevice) }
                                )
                            }
                            AppNavDestination.TV_APPS -> {
                                TvAppsScreen(
                                    tvApps = tvApps,
                                    currentDevice = remoteDevice,
                                    kindleSettings = kindleSettings,
                                    onLaunchApp = { app -> viewModel.launchTvApp(app) },
                                    onLaunchKindleMode = { viewModel.launchKindleReadingMode(remoteDevice) },
                                    onUpdateKindleContrast = { preset -> viewModel.updateKindleContrast(preset) },
                                    onUpdateKindleFontSize = { scale -> viewModel.updateKindleFontSize(scale) }
                                )
                            }
                            AppNavDestination.MEDIA_4K -> {
                                MediaCastScreen(
                                    currentDevice = remoteDevice ?: devices.firstOrNull(),
                                    castState = castState,
                                    onStartCast = { dev, type -> viewModel.requestStartCasting(dev, type) },
                                    onStopCast = { viewModel.stopCasting() }
                                )
                            }
                            AppNavDestination.SETTINGS -> {
                                CastSettingsScreen(
                                    currentProfile = selectedProfile,
                                    devices = devices,
                                    onSelectQualityProfile = { viewModel.toggleQualitySheet(true) },
                                    onOpenPermissionWizard = { viewModel.togglePermissionDialog(true) },
                                    onClearHistory = {
                                        coroutineScope.launch {
                                            viewModel.repository.clearHistory()
                                        }
                                    }
                                )
                            }
                        }

                        // TV Prompt on-screen Banner (webOS / Samsung Tizen)
                        pendingTvPromptDevice?.let { dev ->
                            TvPromptBanner(
                                device = dev,
                                onApproved = { viewModel.confirmUserApprovedOnTv() },
                                onDismiss = { viewModel.dismissTvPromptBanner() }
                            )
                        }
                    }

                    // Dialogs
                    if (showPermissionDialog) {
                        PermissionOnboardingDialog(
                            onDismiss = { viewModel.togglePermissionDialog(false) },
                            onGrantRequested = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.startScanning()
                                }
                            }
                        )
                    }

                    activePinDevice?.let { dev ->
                        PairingPinDialog(
                            device = dev,
                            onDismiss = { viewModel.dismissPinDialog() },
                            onSubmitPin = { pin -> viewModel.submitPin(pin) }
                        )
                    }

                    if (showQualitySheet) {
                        LatencyQualitySheet(
                            currentProfile = selectedProfile,
                            onSelectProfile = { profile -> viewModel.selectQualityProfile(profile) },
                            onDismiss = { viewModel.toggleQualitySheet(false) }
                        )
                    }
                }
            }
        }
    }
}
