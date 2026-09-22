package com.aivigil.compasslevel

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.aivigil.compasslevel.data.MeasurementNotesManager
import com.aivigil.compasslevel.sensor.CompassLocationManager
import com.aivigil.compasslevel.sensor.CompassSensorManager
import com.aivigil.compasslevel.sensor.FlashlightManager
import com.aivigil.compasslevel.ui.*
import com.aivigil.compasslevel.ui.theme.AppSkin
import com.aivigil.compasslevel.ui.theme.CompassLevelTheme
import com.aivigil.compasslevel.ui.theme.PureBlack
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: CompassSensorManager
    private lateinit var locationManager: CompassLocationManager
    private lateinit var notesManager: MeasurementNotesManager
    private lateinit var flashlightManager: FlashlightManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = CompassSensorManager(this)
        locationManager = CompassLocationManager(this)
        notesManager = MeasurementNotesManager(this)
        flashlightManager = FlashlightManager(this)

        setContent {
            CompassLevelTheme {
                val sensorState by sensorManager.compassState.collectAsState()
                val locationState by locationManager.locationState.collectAsState()
                val isFlashlightOn by flashlightManager.isTorchOn.collectAsState()

                val systemDark = isSystemInDarkTheme()
                var isDarkMode by remember { mutableStateOf(systemDark) }

                var currentMode by remember { mutableStateOf("Compass") }
                var currentSkin by remember { mutableStateOf(AppSkin.CLASSIC_EMERALD) }
                val activePalette = remember(currentSkin, isDarkMode) { currentSkin.palette(isDarkMode) }

                var showCalibrationModal by remember { mutableStateOf(false) }
                var showSettingsModal by remember { mutableStateOf(false) }
                var showNotesModal by remember { mutableStateOf(false) }
                var showSkinsModal by remember { mutableStateOf(false) }

                var hasLocationPermission by remember {
                    mutableStateOf(locationManager.hasLocationPermission())
                }

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { perms ->
                    val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    hasLocationPermission = granted
                    if (granted) {
                        locationManager.startLocationUpdates()
                    }
                }

                LaunchedEffect(Unit) {
                    if (!locationManager.hasLocationPermission()) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        locationManager.startLocationUpdates()
                    }
                }

                LaunchedEffect(currentMode, hasLocationPermission) {
                    if (hasLocationPermission) {
                        locationManager.startLocationUpdates()
                    }
                }

                val notesList by notesManager.notes.collectAsState()

                Scaffold(
                    topBar = {
                        GoogleTopAppBar(
                            title = currentMode.uppercase(),
                            isReliable = sensorState.isReliable,
                            notesCount = notesList.size,
                            skin = activePalette,
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode },
                            onNotesClick = { showNotesModal = true },
                            onSkinsClick = { showSkinsModal = true },
                            onCalibrateClick = { showCalibrationModal = true },
                            onSettingsClick = { showSettingsModal = true }
                        )
                    },
                    bottomBar = {
                        GoogleNavigationBar(
                            selectedMode = currentMode,
                            skin = activePalette,
                            onModeSelected = { mode ->
                                currentMode = mode
                                if (mode == "Location" && locationManager.hasLocationPermission()) {
                                    locationManager.startLocationUpdates()
                                }
                            }
                        )
                    },
                    containerColor = activePalette.appBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentMode,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "ScreenTransition"
                        ) { mode ->
                            when (mode) {
                                "Level" -> {
                                    ScreenContentView(
                                        pitch = if (sensorState.isAngleLocked) sensorState.lockedPitch else sensorState.pitch,
                                        roll = if (sensorState.isAngleLocked) sensorState.lockedRoll else sensorState.roll,
                                        isLevel = sensorState.isLevel,
                                        isAngleLocked = sensorState.isAngleLocked,
                                        usePercentGrade = sensorState.usePercentGrade,
                                        onTareClick = { sensorManager.tare() },
                                        onAngleLockToggle = { sensorManager.toggleAngleLock() },
                                        onFlashlightToggle = { flashlightManager.toggleFlashlight() },
                                        isFlashlightOn = isFlashlightOn,
                                        onSaveNoteClick = { p, r ->
                                            notesManager.saveNote(
                                                type = "Level",
                                                title = "Dual-Axis Level Alignment",
                                                primaryValue = "X: ${String.format(Locale.US, "%.1f", r)}°, Y: ${String.format(Locale.US, "%.1f", p)}°",
                                                secondaryDetails = if (sensorState.isLevel) "PERFECT LEVEL (0.0° Tolerance)" else "Inclination Detected"
                                            )
                                        },
                                        skin = activePalette
                                    )
                                }
                                "Clinometer" -> {
                                    ScreenClinometerView(
                                        pitch = if (sensorState.isAngleLocked) sensorState.lockedElevation else sensorState.elevation,
                                        roll = if (sensorState.isAngleLocked) sensorState.lockedCameraRoll else sensorState.cameraRoll,
                                        isLocked = sensorState.isAngleLocked,
                                        onLockToggle = { sensorManager.toggleAngleLock() },
                                        onSaveNoteClick = { p, slope ->
                                            notesManager.saveNote(
                                                type = "Clinometer",
                                                title = "AR Clinometer Sight",
                                                primaryValue = "Elevation: ${String.format(Locale.US, "%+.1f", p)}°",
                                                secondaryDetails = "Grade/Slope: ${String.format(Locale.US, "%.1f", slope)}%"
                                            )
                                        },
                                        onFlashlightToggle = { flashlightManager.toggleFlashlight() },
                                        isFlashlightOn = isFlashlightOn,
                                        skin = activePalette
                                    )
                                }
                                "Location" -> {
                                    ScreenLocationView(
                                        locationData = locationState,
                                        compassHeading = sensorState.heading,
                                        skin = activePalette,
                                        hasLocationPermission = hasLocationPermission,
                                        onRequestPermission = {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        },
                                        onOpenSettings = {
                                            locationManager.openLocationSettings()
                                        },
                                        onForceRefresh = {
                                            locationManager.forceRefresh()
                                        },
                                        onSaveToNotes = { summary, details ->
                                            notesManager.saveNote(
                                                type = "Location",
                                                title = "GPS Waypoint",
                                                primaryValue = summary,
                                                secondaryDetails = details
                                            )
                                        }
                                    )
                                }
                                else -> {
                                    if (!sensorManager.hasMagnetometer) {
                                        ScreenContentView(
                                            pitch = if (sensorState.isAngleLocked) sensorState.lockedPitch else sensorState.pitch,
                                            roll = if (sensorState.isAngleLocked) sensorState.lockedRoll else sensorState.roll,
                                            isLevel = sensorState.isLevel,
                                            isAngleLocked = sensorState.isAngleLocked,
                                            usePercentGrade = sensorState.usePercentGrade,
                                            onTareClick = { sensorManager.tare() },
                                            onAngleLockToggle = { sensorManager.toggleAngleLock() },
                                            onFlashlightToggle = { flashlightManager.toggleFlashlight() },
                                            isFlashlightOn = isFlashlightOn,
                                            onSaveNoteClick = { p, r ->
                                                notesManager.saveNote(
                                                    type = "Level",
                                                    title = "Dual-Axis Level Alignment",
                                                    primaryValue = "X: ${String.format(Locale.US, "%.1f", r)}°, Y: ${String.format(Locale.US, "%.1f", p)}°",
                                                    secondaryDetails = if (sensorState.isLevel) "PERFECT LEVEL" else "Inclination Detected"
                                                )
                                            },
                                            skin = activePalette
                                        )
                                    } else {
                                        ScreenLiveCompassView(
                                            heading = if (sensorState.isBearingLocked) sensorState.lockedHeading else sensorState.heading,
                                            pitch = sensorState.pitch,
                                            roll = sensorState.roll,
                                            isLevel = sensorState.isLevel,
                                            isBearingLocked = sensorState.isBearingLocked,
                                            lockedHeading = sensorState.lockedHeading,
                                            onBearingLockToggle = { sensorManager.toggleBearingLock() },
                                            onSaveNoteClick = { h, card ->
                                                notesManager.saveNote(
                                                    type = "Compass",
                                                    title = "Compass Bearing $card",
                                                    primaryValue = "${h.toInt()}° $card",
                                                    secondaryDetails = "Pitch: ${sensorState.pitch.toInt()}°, Roll: ${sensorState.roll.toInt()}°"
                                                )
                                            },
                                            skin = activePalette,
                                            locationData = locationState
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Modals & Bottom Sheets
                if (showNotesModal) {
                    val currentMeasurementSummary = when (currentMode) {
                        "Compass" -> Pair(
                            "${sensorState.heading.toInt()}°",
                            "Pitch: ${sensorState.pitch.toInt()}°, Roll: ${sensorState.roll.toInt()}°"
                        )
                        "Level" -> Pair(
                            "X: ${String.format(Locale.US, "%.1f", sensorState.roll)}° / Y: ${String.format(Locale.US, "%.1f", sensorState.pitch)}°",
                            if (sensorState.isLevel) "SURFACE LEVEL" else "INCLINED"
                        )
                        "Clinometer" -> Pair(
                            "Elevation: ${String.format(Locale.US, "%+.1f", sensorState.elevation)}°",
                            "Roll: ${String.format(Locale.US, "%+.1f", sensorState.cameraRoll)}°"
                        )
                        "Location" -> Pair(
                            "${locationState.latitudeDms}, ${locationState.longitudeDms}",
                            locationState.address
                        )
                        else -> Pair("N/A", "")
                    }
                    NotesModal(
                        notesManager = notesManager,
                        currentMode = currentMode,
                        currentMeasurementSummary = currentMeasurementSummary,
                        skin = activePalette,
                        onClose = { showNotesModal = false }
                    )
                }

                if (showSkinsModal) {
                    SkinsModal(
                        currentSkin = currentSkin,
                        isDarkMode = isDarkMode,
                        onSkinSelected = { skin ->
                            currentSkin = skin
                            showSkinsModal = false
                        },
                        onClose = { showSkinsModal = false }
                    )
                }

                if (showSettingsModal) {
                    SettingsBottomSheet(
                        isTrueNorth = sensorState.isTrueNorth,
                        declination = sensorState.declination,
                        usePercentGrade = sensorState.usePercentGrade,
                        skin = activePalette,
                        isDarkMode = isDarkMode,
                        onThemeToggle = { isDarkMode = it },
                        onTrueNorthToggle = { enabled ->
                            sensorManager.setTrueNorth(enabled, sensorState.declination)
                        },
                        onDeclinationChange = { newDecl ->
                            sensorManager.setTrueNorth(sensorState.isTrueNorth, newDecl)
                        },
                        onPercentGradeToggle = { enabled ->
                            sensorManager.setUsePercentGrade(enabled)
                        },
                        onCalibrateClick = {
                            showSettingsModal = false
                            showCalibrationModal = true
                        },
                        onResetTare = {
                            sensorManager.resetTare()
                        },
                        onClose = { showSettingsModal = false }
                    )
                }

                if (showCalibrationModal) {
                    CalibrationDialog(
                        skin = activePalette,
                        onDismiss = { showCalibrationModal = false }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.startListening()
        if (::locationManager.isInitialized && locationManager.hasLocationPermission()) {
            locationManager.startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stopListening()
        if (::locationManager.isInitialized) {
            locationManager.stopLocationUpdates()
        }
        if (::flashlightManager.isInitialized) {
            flashlightManager.turnOff()
        }
    }
}
