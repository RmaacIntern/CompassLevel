package com.aivigil.compasslevel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.aivigil.compasslevel.sensor.CompassSensorManager
import com.aivigil.compasslevel.ui.*
import com.aivigil.compasslevel.ui.theme.CompassLevelTheme
import com.aivigil.compasslevel.ui.theme.PureBlack

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: CompassSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = CompassSensorManager(this)

        setContent {
            CompassLevelTheme {
                val sensorState by sensorManager.compassState.collectAsState()
                var currentMode by remember { mutableStateOf("Compass") }
                var showCalibrationModal by remember { mutableStateOf(false) }
                var showSettingsModal by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PureBlack)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    TopActionBar(
                        isReliable = sensorState.isReliable,
                        onCalibrateClick = { showCalibrationModal = true },
                        onSettingsClick = { showSettingsModal = true }
                    )

                    SegmentedModeSelector(
                        selectedMode = currentMode,
                        onModeSelected = { currentMode = it }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (showSettingsModal) {
                            ScreenSettingsView(
                                isTrueNorth = sensorState.isTrueNorth,
                                declination = sensorState.declination,
                                usePercentGrade = sensorState.usePercentGrade,
                                onTrueNorthToggle = { enabled ->
                                    sensorManager.setTrueNorth(enabled, sensorState.declination)
                                },
                                onDeclinationChange = { newDecl ->
                                    sensorManager.setTrueNorth(sensorState.isTrueNorth, newDecl)
                                },
                                onPercentGradeToggle = { enabled ->
                                    sensorManager.setUsePercentGrade(enabled)
                                },
                                onClose = { showSettingsModal = false }
                            )
                        } else if (showCalibrationModal) {
                            ScreenErrorView(onDismiss = { showCalibrationModal = false })
                        } else {
                            when (currentMode) {
                                "Level" -> {
                                    ScreenContentView(
                                        pitch = sensorState.pitch,
                                        roll = sensorState.roll,
                                        isLevel = sensorState.isLevel,
                                        usePercentGrade = sensorState.usePercentGrade,
                                        onTareClick = { sensorManager.tare() }
                                    )
                                }
                                else -> {
                                    if (!sensorManager.hasMagnetometer) {
                                        ScreenContentView(
                                            pitch = sensorState.pitch,
                                            roll = sensorState.roll,
                                            isLevel = sensorState.isLevel,
                                            usePercentGrade = sensorState.usePercentGrade,
                                            onTareClick = { sensorManager.tare() }
                                        )
                                    } else {
                                        ScreenLiveCompassView(
                                            heading = sensorState.heading,
                                            pitch = sensorState.pitch,
                                            roll = sensorState.roll,
                                            isLevel = sensorState.isLevel
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AdBannerBottom()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stopListening()
    }
}
