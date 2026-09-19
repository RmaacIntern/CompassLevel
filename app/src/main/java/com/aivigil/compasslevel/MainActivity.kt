package com.aivigil.compasslevel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aivigil.compasslevel.sensor.CompassSensorManager
import com.aivigil.compasslevel.sensor.ScreenState
import com.aivigil.compasslevel.ui.*

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: CompassSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = CompassSensorManager(this)

        setContent {
            val uiState by sensorManager.uiState.collectAsState()

            when (uiState.screenState) {
                ScreenState.LOADING -> ScreenLoading()
                ScreenState.CONTENT -> ScreenContent(
                    state = uiState,
                    onMenuClick = { /* Settings menu */ }
                )
                ScreenState.LEVEL_ONLY -> ScreenFallbackLevel(
                    state = uiState,
                    onTapToZero = { sensorManager.tapToZero() }
                )
                ScreenState.UNRELIABLE -> ScreenUnreliable(
                    state = uiState,
                    onStartCalibration = { /* Rewarded Video / Figure-8 calibration */ }
                )
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
