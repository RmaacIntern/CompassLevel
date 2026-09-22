package com.aivigil.compasslevel.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.sensor.LocationData
import com.aivigil.compasslevel.ui.theme.*
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScreenLocationView(
    locationData: LocationData,
    compassHeading: Float,
    skin: SkinPalette,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onForceRefresh: () -> Unit = {},
    onSaveToNotes: (summary: String, details: String) -> Unit
) {
    val cardinal = when (compassHeading) {
        in 22.5f..67.5f   -> "NE"
        in 67.5f..112.5f  -> "E"
        in 112.5f..157.5f -> "SE"
        in 157.5f..202.5f -> "S"
        in 202.5f..247.5f -> "SW"
        in 247.5f..292.5f -> "W"
        in 292.5f..337.5f -> "NW"
        else              -> "N"
    }

    if (!hasLocationPermission) {
        // Permission Request View
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(skin.cardBackground)
                        .border(1.dp, skin.cardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = skin.primaryAccent,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "GPS LOCATION PERMISSION",
                    color = skin.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Location access is needed to compute your real-time Latitude, Longitude, Altitude, Speed, and Geocoded Address.",
                    color = skin.textSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Default,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = skin.primaryAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ENABLE GPS SENSORS",
                        color = PureBlack,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    } else {
        // Location Dashboard View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Disabled GPS Alert Banner
            if (!locationData.isGpsEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenSettings() },
                    colors = CardDefaults.cardColors(containerColor = GlowAmberSoft),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberWarning)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LOCATION IS TURNED OFF",
                                color = AmberWarning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap here to turn on Location in Android settings to acquire GPS coordinates.",
                                color = skin.textPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                }
            }

            // Address & Fix Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = skin.surfaceBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.ringBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (locationData.hasFix) NeonEmerald else AmberWarning)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (locationData.hasFix) "GPS LOCKED" else "ACQUIRING SATELLITES",
                                color = if (locationData.hasFix) NeonEmerald else AmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (locationData.accuracyMeters > 0) {
                                Text(
                                    text = "±${locationData.accuracyMeters.toInt()}m",
                                    color = skin.textSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Default
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            IconButton(
                                onClick = onForceRefresh,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh GPS",
                                    tint = skin.primaryAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "CURRENT LOCATION",
                        color = skin.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = locationData.address,
                        color = skin.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            // Coordinates Card (Lat / Lon)
            val hasCoords = locationData.hasFix || locationData.latitude != 0.0 || locationData.longitude != 0.0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = skin.surfaceBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.ringBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LATITUDE",
                            color = skin.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (hasCoords) String.format(Locale.US, "%.5f°", locationData.latitude) else "--.-----°",
                            color = skin.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                        Text(
                            text = if (hasCoords) locationData.latitudeDms else "Acquiring...",
                            color = skin.primaryAccent,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(skin.cardBorder)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = "LONGITUDE",
                            color = skin.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (hasCoords) String.format(Locale.US, "%.5f°", locationData.longitude) else "--.-----°",
                            color = skin.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                        Text(
                            text = if (hasCoords) locationData.longitudeDms else "Acquiring...",
                            color = skin.primaryAccent,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Altitude & Speed Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Altitude
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = skin.surfaceBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.ringBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terrain,
                                contentDescription = null,
                                tint = skin.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ALTITUDE",
                                color = skin.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${locationData.altitudeMeters.toInt()} m",
                            color = skin.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                        Text(
                            text = "${locationData.altitudeFeet.toInt()} ft MSL",
                            color = skin.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }

                // Speed
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = skin.surfaceBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.ringBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = skin.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SPEED",
                                color = skin.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", locationData.speedKmh),
                            color = skin.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                        Text(
                            text = "km/h (${String.format(Locale.US, "%.1f", locationData.speedMph)} mph)",
                            color = skin.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            }

            // Compass Direction & Azimuth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = skin.surfaceBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.ringBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMPASS DIRECTION",
                            color = skin.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${compassHeading.toInt()}°",
                                color = skin.textPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(skin.primaryAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = cardinal,
                                    color = skin.primaryAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default
                                )
                            }
                        }
                        Text(
                            text = "Real-time Magnetic Orientation",
                            color = skin.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Default
                        )
                    }

                    // Miniature rotating compass dial
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(skin.dialBackground)
                            .border(1.5.dp, skin.ringBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationZ = -compassHeading }
                        ) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val needleLen = size.width * 0.38f

                            // North Needle (Red)
                            val pathN = Path().apply {
                                moveTo(cx, cy - needleLen)
                                lineTo(cx + 6.dp.toPx(), cy)
                                lineTo(cx - 6.dp.toPx(), cy)
                                close()
                            }
                            drawPath(pathN, color = LaserRed)

                            // South Needle (White)
                            val pathS = Path().apply {
                                moveTo(cx, cy + needleLen)
                                lineTo(cx + 6.dp.toPx(), cy)
                                lineTo(cx - 6.dp.toPx(), cy)
                                close()
                            }
                            drawPath(pathS, color = Color.White.copy(alpha = 0.7f))

                            // Pivot dot
                            drawCircle(color = PureBlack, radius = 3.dp.toPx())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Location to Notes Button
            Button(
                onClick = {
                    val summary = "${locationData.latitudeDms}, ${locationData.longitudeDms}"
                    val details = "Alt: ${locationData.altitudeMeters.toInt()}m | Spd: ${String.format(Locale.US, "%.1f", locationData.speedKmh)}km/h | Heading: ${compassHeading.toInt()}° $cardinal\nAddress: ${locationData.address}"
                    onSaveToNotes(summary, details)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = skin.primaryAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    tint = PureBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECORD LOCATION & BEARING",
                    color = PureBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

@Composable
fun ScreenLocationView(
    locationData: LocationData,
    compassHeading: Float,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onForceRefresh: () -> Unit = {},
    onSaveToNotes: (summary: String, details: String) -> Unit
) {
    ScreenLocationView(
        locationData = locationData,
        compassHeading = compassHeading,
        skin = skin.palette(true),
        hasLocationPermission = hasLocationPermission,
        onRequestPermission = onRequestPermission,
        onOpenSettings = onOpenSettings,
        onForceRefresh = onForceRefresh,
        onSaveToNotes = onSaveToNotes
    )
}
