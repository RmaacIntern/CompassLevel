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
                var selectedTab by remember { mutableStateOf("Live") }
                val heading by sensorManager.headingFlow.collectAsState()
                val pitch by sensorManager.pitchFlow.collectAsState()
                val roll by sensorManager.rollFlow.collectAsState()
                val isReliable by sensorManager.isReliable.collectAsState()
                val hasMagnetometer = sensorManager.hasMagnetometer

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PureBlack)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    TopActionBar()
                    StateSelectorBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (selectedTab) {
                            "Loading" -> ScreenLoadingView()
                            "Content" -> ScreenContentView(pitch = -2f, roll = -1f, isStaticMock = true)
                            "Empty" -> ScreenEmptyView()
                            "Error" -> ScreenErrorView()
                            else -> {
                                when {
                                    !hasMagnetometer -> ScreenContentView(pitch = pitch, roll = roll, isStaticMock = false)
                                    !isReliable -> ScreenErrorView()
                                    else -> ScreenLiveCompassView(heading = heading, pitch = pitch, roll = roll)
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
