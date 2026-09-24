package com.aivigil.compasslevel.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.ui.theme.*
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// GOOGLE MATERIAL 3 TOP APP BAR
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleTopAppBar(
    title: String,
    isReliable: Boolean = true,
    notesCount: Int = 0,
    skin: SkinPalette,
    isDarkMode: Boolean = true,
    onThemeToggle: () -> Unit = {},
    onNotesClick: () -> Unit = {},
    onSkinsClick: () -> Unit = {},
    onCalibrateClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHomeClick: (() -> Unit)? = null
) {
    TopAppBar(
        navigationIcon = {
            if (onHomeClick != null) {
                IconButton(
                    onClick = onHomeClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Return to Intro Screen",
                        tint = skin.primaryAccent
                    )
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = skin.textPrimary,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                // Calm hardware diagnostic status indicator (Leica / Nothing style active LED)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(skin.primaryAccent.copy(alpha = 0.18f))
                        .clickable { onCalibrateClick() }
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(skin.primaryAccent)
                    )
                }
            }
        },
        actions = {
            // Light / Dark Mode Toggle Button
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = skin.textPrimary
                )
            }

            // Themes / Skins Button
            IconButton(
                onClick = onSkinsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = "Instrument Skins",
                    tint = skin.primaryAccent
                )
            }

            // Notes Button with Badge
            IconButton(
                onClick = onNotesClick,
                modifier = Modifier.size(40.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (notesCount > 0) {
                            Badge(
                                containerColor = skin.primaryAccent,
                                contentColor = PureBlack
                            ) {
                                Text("$notesCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Measurement Notes",
                        tint = skin.textPrimary
                    )
                }
            }

            // Settings Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = skin.textSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = skin.surfaceBackground,
            titleContentColor = skin.textPrimary
        )
    )
}

@Composable
fun GoogleTopAppBar(
    title: String,
    isReliable: Boolean = true,
    notesCount: Int = 0,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onNotesClick: () -> Unit = {},
    onSkinsClick: () -> Unit = {},
    onCalibrateClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    GoogleTopAppBar(
        title = title,
        isReliable = isReliable,
        notesCount = notesCount,
        skin = skin.palette(true),
        isDarkMode = true,
        onThemeToggle = {},
        onNotesClick = onNotesClick,
        onSkinsClick = onSkinsClick,
        onCalibrateClick = onCalibrateClick,
        onSettingsClick = onSettingsClick
    )
}

@Composable
fun TopActionBar(
    isReliable: Boolean = true,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onNotesClick: () -> Unit = {},
    onSkinsClick: () -> Unit = {},
    onCalibrateClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    GoogleTopAppBar(
        title = "Compass & Level",
        isReliable = isReliable,
        notesCount = 0,
        skin = skin,
        onNotesClick = onNotesClick,
        onSkinsClick = onSkinsClick,
        onCalibrateClick = onCalibrateClick,
        onSettingsClick = onSettingsClick
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// GOOGLE MATERIAL 3 NAVIGATION BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GoogleNavigationBar(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    skin: SkinPalette
) {
    val haptic = LocalHapticFeedback.current
    val navItems = listOf(
        Triple("Compass", Icons.Filled.Explore, Icons.Outlined.Explore),
        Triple("Level", Icons.Filled.Straighten, Icons.Outlined.Straighten),
        Triple("Clinometer", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
        Triple("Location", Icons.Filled.NearMe, Icons.Outlined.NearMe)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = skin.surfaceBackground,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Fine hairline divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.75.dp)
                    .background(skin.cardBorder.copy(alpha = 0.6f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                navItems.forEach { (mode, activeIcon, inactiveIcon) ->
                    val isSelected = selectedMode == mode

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) skin.primaryAccent.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .clickable {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onModeSelected(mode)
                                }
                            }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) activeIcon else inactiveIcon,
                                contentDescription = mode,
                                tint = if (isSelected) skin.primaryAccent else skin.textSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = mode,
                                color = if (isSelected) skin.primaryAccent else skin.textSecondary.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Default,
                                letterSpacing = 0.2.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleNavigationBar(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD
) {
    GoogleNavigationBar(
        selectedMode = selectedMode,
        onModeSelected = onModeSelected,
        skin = skin.palette(true)
    )
}

// Legacy Segmented Switcher for backwards compatibility
@Composable
fun SegmentedModeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    skin: SkinPalette
) {
    GoogleNavigationBar(
        selectedMode = selectedMode,
        onModeSelected = onModeSelected,
        skin = skin
    )
}

@Composable
fun SegmentedModeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD
) {
    GoogleNavigationBar(
        selectedMode = selectedMode,
        onModeSelected = onModeSelected,
        skin = skin.palette(true)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// MONETIZATION-READY AD CONTAINER (Fully Skinned)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdBannerBottom(skin: SkinPalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(skin.surfaceBackground)
            .border(width = 0.5.dp, color = skin.cardBorder, shape = RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(0.5.dp, skin.textSecondary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AD",
                    color = skin.textSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Sponsored Placement (320x50)",
                color = skin.textSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Default
            )
        }
    }
}

@Composable
fun AdBannerBottom(skin: AppSkin = AppSkin.CLASSIC_EMERALD) {
    AdBannerBottom(skin = skin.palette(true))
}

// ─────────────────────────────────────────────────────────────────────────────
// PRECISION PILL CARD (Pitch & Roll Decimal Readouts - Fully Skinned)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PrecisionPillCard(
    label: String,
    valueDegrees: Float,
    skin: SkinPalette
) {
    val absVal = abs(valueDegrees)
    val valueColor = when {
        absVal <= 0.5f -> skin.primaryAccent
        absVal <= 5.0f -> AmberWarning
        else           -> skin.textPrimary
    }

    val formattedValue = String.format(Locale.US, "%+.1f°", valueDegrees)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(skin.cardBackground)
            .border(1.dp, skin.cardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = skin.textSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = formattedValue,
            color = valueColor,
            fontSize = 15.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PrecisionPillCard(
    label: String,
    valueDegrees: Float,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD
) {
    PrecisionPillCard(
        label = label,
        valueDegrees = valueDegrees,
        skin = skin.palette(true)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// COCKPIT TELEMETRY DECK (Balanced Telemetry Pod + Distinct Squircle Action Controls)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CockpitTelemetryDeck(
    pitch: Float,
    roll: Float,
    isBearingLocked: Boolean,
    onBearingLockToggle: () -> Unit,
    onSaveNoteClick: () -> Unit,
    skin: SkinPalette,
    modifier: Modifier = Modifier,
    isSoundEnabled: Boolean = true,
    onSoundToggle: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 1. Telemetry Pod (Pitch & Roll Readouts - Rock Solid Fixed Height) ──
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            color = skin.cardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, skin.cardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Pitch
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "PITCH",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = skin.textSecondary,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = String.format(Locale.US, "%+.1f°", pitch),
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (abs(pitch) <= 0.6f) skin.primaryAccent else skin.textPrimary,
                        fontFamily = FontFamily.Default,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Vertical hairline divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(skin.cardBorder.copy(alpha = 0.8f))
                )

                // Roll
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ROLL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = skin.textSecondary,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = String.format(Locale.US, "%+.1f°", roll),
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (abs(roll) <= 0.6f) skin.primaryAccent else skin.textPrimary,
                        fontFamily = FontFamily.Default,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // ── 2. Distinct Bearing Lock Squircle Control ──
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    if (isBearingLocked) skin.primaryAccent.copy(alpha = 0.22f)
                    else skin.cardBackground
                )
                .border(
                    width = 1.dp,
                    color = if (isBearingLocked) skin.primaryAccent else skin.cardBorder,
                    shape = RoundedCornerShape(13.dp)
                )
                .clickable { onBearingLockToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBearingLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isBearingLocked) "Unlock Heading" else "Lock Heading Bearing",
                tint = if (isBearingLocked) skin.primaryAccent else skin.textPrimary,
                modifier = Modifier.size(19.dp)
            )
        }

        // ── 3. Rotary Dial Sound Toggle Squircle Control ──
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    if (isSoundEnabled) skin.primaryAccent.copy(alpha = 0.20f)
                    else skin.cardBackground
                )
                .border(
                    width = 1.dp,
                    color = if (isSoundEnabled) skin.primaryAccent else skin.cardBorder,
                    shape = RoundedCornerShape(13.dp)
                )
                .clickable { onSoundToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                contentDescription = if (isSoundEnabled) "Mute Rotating Click Sound" else "Enable Rotating Click Sound",
                tint = if (isSoundEnabled) skin.primaryAccent else skin.textSecondary,
                modifier = Modifier.size(19.dp)
            )
        }

        // ── 4. Distinct Save Note Squircle Control ──
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(skin.cardBackground)
                .border(1.dp, skin.cardBorder, RoundedCornerShape(13.dp))
                .clickable { onSaveNoteClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkAdd,
                contentDescription = "Save Compass Bearing Note",
                tint = skin.primaryAccent,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPASS ROSE DIAL (Porsche-Inspired Luxury Automotive Instrument)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CompassRoseDial(
    heading: Float,
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier,
    skin: SkinPalette
) {
    val haptic = LocalHapticFeedback.current

    // Trigger haptic tick on exact level snap
    LaunchedEffect(isLevel) {
        if (isLevel) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Pre-allocate text paints once outside draw loop to eliminate GC churn
    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val dialLabels = remember {
        listOf(
            Triple("N", 0.0, true),
            Triple("NE", 45.0, false),
            Triple("E", 90.0, true),
            Triple("SE", 135.0, false),
            Triple("S", 180.0, true),
            Triple("SW", 225.0, false),
            Triple("W", 270.0, true),
            Triple("NW", 315.0, false)
        )
    }

    val lubberPath = remember { Path() }

    Box(
        modifier = modifier.size(284.dp),
        contentAlignment = Alignment.Center
    ) {
        // ── 1. Outer Machined Bezel Frame (Subtle Luxury Bezel) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = 140.dp.toPx()
            val bezelRadius = 135.dp.toPx()

            // Outer metallic rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(skin.dialOuterBezel, skin.surfaceBackground),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            // Accent bevel ring
            drawCircle(
                color = skin.ringBorder.copy(alpha = if (skin.isDark) 0.5f else 0.35f),
                radius = outerRadius,
                center = center,
                style = Stroke(1.5.dp.toPx())
            )
            drawCircle(
                color = skin.cardBorder,
                radius = bezelRadius,
                center = center,
                style = Stroke(1.dp.toPx())
            )
        }

        // ── 2. Rotating Dial Track (GPU Rotated via graphicsLayer Lambda) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = -heading
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val center = Offset(cx, cy)
                val trackRadius = 132.dp.toPx()
                val innerTrackRadius = 110.dp.toPx()
                val labelRadius = 92.dp.toPx()

                // Dial face fill
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            skin.dialBackground,
                            skin.dialBackground.copy(alpha = 0.95f)
                        ),
                        center = center,
                        radius = trackRadius
                    ),
                    radius = trackRadius,
                    center = center
                )

                // Concentric inner boundary rings
                drawCircle(
                    color = skin.ringBorder.copy(alpha = 0.25f),
                    radius = trackRadius,
                    center = center,
                    style = Stroke(1.dp.toPx())
                )
                drawCircle(
                    color = skin.cardBorder.copy(alpha = 0.5f),
                    radius = innerTrackRadius,
                    center = center,
                    style = Stroke(0.8.dp.toPx())
                )

                // Laser crosshair lines (North-South & East-West)
                val crossInner = 36.dp.toPx()
                val crossOuter = innerTrackRadius - 2.dp.toPx()
                val crossColor = skin.cardBorder.copy(alpha = 0.35f)
                val crossStroke = 0.75.dp.toPx()
                drawLine(crossColor, Offset(cx, cy - crossInner), Offset(cx, cy - crossOuter), crossStroke)
                drawLine(crossColor, Offset(cx, cy + crossInner), Offset(cx, cy + crossOuter), crossStroke)
                drawLine(crossColor, Offset(cx - crossInner, cy), Offset(cx - crossOuter, cy), crossStroke)
                drawLine(crossColor, Offset(cx + crossInner, cy), Offset(cx + crossOuter, cy), crossStroke)

                // 360° Precision Ticks (Major 30°, Medium 15°, Micro 3°)
                for (i in 0 until 120) {
                    val deg = i * 3.0
                    val rad = Math.toRadians(deg - 90.0)
                    val isMajor = (i % 10 == 0)      // every 30°
                    val isMedium = (i % 5 == 0)     // every 15°

                    val tickLen = when {
                        isMajor -> 11.dp.toPx()
                        isMedium -> 7.dp.toPx()
                        else -> 3.5.dp.toPx()
                    }
                    val tickColor = when {
                        isMajor -> skin.primaryAccent
                        isMedium -> skin.secondaryAccent
                        else -> skin.cardBorder.copy(alpha = 0.6f)
                    }
                    val strokeW = when {
                        isMajor -> 1.8.dp.toPx()
                        isMedium -> 1.1.dp.toPx()
                        else -> 0.75.dp.toPx()
                    }

                    val startX = (cx + (trackRadius - tickLen) * cos(rad)).toFloat()
                    val startY = (cy + (trackRadius - tickLen) * sin(rad)).toFloat()
                    val endX = (cx + trackRadius * cos(rad)).toFloat()
                    val endY = (cy + trackRadius * sin(rad)).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW
                    )
                }

                // Typography for 8 Cardinals & Intercardinals (Google Sans / Default Roboto) - ZERO OVERLAPPING
                dialLabels.forEach { (label, deg, isCardinal) ->
                    val rad = Math.toRadians(deg - 90.0)
                    val x = (cx + labelRadius * cos(rad)).toFloat()
                    val y = (cy + labelRadius * sin(rad)).toFloat()

                    textPaint.textSize = when (label) {
                        "N" -> 18.sp.toPx()
                        "E", "S", "W" -> 14.sp.toPx()
                        else -> 10.sp.toPx()
                    }
                    textPaint.typeface = Typeface.create(
                        Typeface.DEFAULT,
                        if (isCardinal) Typeface.BOLD else Typeface.NORMAL
                    )
                    textPaint.color = when (label) {
                        "N" -> skin.needleNorth.toArgb()
                        "E", "S", "W" -> skin.textPrimary.toArgb()
                        else -> skin.textSecondary.toArgb()
                    }

                    val textY = y - ((textPaint.descent() + textPaint.ascent()) / 2f)
                    drawContext.canvas.nativeCanvas.drawText(label, x, textY, textPaint)
                }
            }
        }

        // ── 3. Static 12 O'Clock Precision Lubber Line (Heading Chevron) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            lubberPath.reset()
            lubberPath.moveTo(cx, 6.dp.toPx())
            lubberPath.lineTo(cx - 7.dp.toPx(), 19.dp.toPx())
            lubberPath.lineTo(cx, 16.dp.toPx())
            lubberPath.lineTo(cx + 7.dp.toPx(), 19.dp.toPx())
            lubberPath.close()

            drawPath(path = lubberPath, color = skin.needleNorth)
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(cx, 7.dp.toPx()),
                end = Offset(cx, 15.5.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }

        // ── 4. Center Machined Hub & High-Visibility Fluid Leveling Bubble ──
        Canvas(modifier = Modifier.size(76.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = 35.dp.toPx()
            val innerRadius = 28.5.dp.toPx()
            val snapRadius = 14.dp.toPx()
            val bubbleRadius = 8.5.dp.toPx()

            // Outer machined hub bezel
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(skin.cardElevated, skin.dialOuterBezel),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = skin.cardBorder,
                radius = outerRadius,
                center = center,
                style = Stroke(1.2.dp.toPx())
            )

            // Inner fluid recess
            drawCircle(
                color = skin.dialBackground,
                radius = innerRadius,
                center = center
            )

            // Precision Level Target Reticle (Outer Ring + 4 Quadrant Crosshairs)
            val targetColor = if (isLevel) skin.primaryAccent else skin.cardBorder.copy(alpha = 0.7f)
            drawCircle(
                color = targetColor,
                radius = snapRadius,
                center = center,
                style = Stroke(1.2.dp.toPx())
            )

            // 4 Quadrant Precision Reticle Ticks
            val tickLen = 4.dp.toPx()
            val reticleColor = if (isLevel) skin.primaryAccent else skin.cardBorder.copy(alpha = 0.85f)
            // Left tick
            drawLine(
                color = reticleColor,
                start = Offset(center.x - snapRadius, center.y),
                end = Offset(center.x - snapRadius + tickLen, center.y),
                strokeWidth = 1.2.dp.toPx()
            )
            // Right tick
            drawLine(
                color = reticleColor,
                start = Offset(center.x + snapRadius - tickLen, center.y),
                end = Offset(center.x + snapRadius, center.y),
                strokeWidth = 1.2.dp.toPx()
            )
            // Top tick
            drawLine(
                color = reticleColor,
                start = Offset(center.x, center.y - snapRadius),
                end = Offset(center.x, center.y - snapRadius + tickLen),
                strokeWidth = 1.2.dp.toPx()
            )
            // Bottom tick
            drawLine(
                color = reticleColor,
                start = Offset(center.x, center.y + snapRadius - tickLen),
                end = Offset(center.x, center.y + snapRadius),
                strokeWidth = 1.2.dp.toPx()
            )

            // Center target micro-dot
            drawCircle(
                color = if (isLevel) skin.primaryAccent else skin.cardBorder.copy(alpha = 0.6f),
                radius = 1.5.dp.toPx(),
                center = center
            )

            // Level snap radiant halo bloom
            if (isLevel) {
                drawCircle(
                    color = skin.bubbleGlow,
                    radius = snapRadius + 5.dp.toPx(),
                    center = center,
                    style = Stroke(3.5.dp.toPx())
                )
            }

            // Fluid Leveling Bubble (Physics clamp to circular hub interior)
            val maxTravel = innerRadius - bubbleRadius - 2.dp.toPx()
            val rawOffsetX = (roll.coerceIn(-20f, 20f) / 14f * maxTravel)
            val rawOffsetY = (pitch.coerceIn(-20f, 20f) / 14f * maxTravel)
            val dist = Math.hypot(rawOffsetX.toDouble(), rawOffsetY.toDouble()).toFloat()
            val scale = if (dist > maxTravel) maxTravel / dist else 1f
            val dotCenter = Offset(center.x + rawOffsetX * scale, center.y - rawOffsetY * scale)

            // 1. Soft fluid luminous aura
            drawCircle(
                color = skin.bubbleGlow,
                radius = bubbleRadius + 4.dp.toPx(),
                center = dotCenter
            )
            // 2. High-visibility fluid bubble body
            drawCircle(
                color = if (isLevel) skin.primaryAccent else skin.bubbleColor,
                radius = bubbleRadius,
                center = dotCenter
            )
            // 3. Specular gloss highlight (Organic 3D fluid reflection)
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = bubbleRadius * 0.42f,
                center = Offset(dotCenter.x - 2.2.dp.toPx(), dotCenter.y - 2.2.dp.toPx())
            )
            // 4. Glass refraction contour rim
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = bubbleRadius,
                center = dotCenter,
                style = Stroke(1.2.dp.toPx())
            )
        }
    }
}

@Composable
fun CompassRoseDial(
    heading: Float,
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD
) {
    CompassRoseDial(
        heading = heading,
        pitch = pitch,
        roll = roll,
        isLevel = isLevel,
        modifier = modifier,
        skin = skin.palette(true)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// FULL-SCREEN RETICLE SPIRIT LEVEL (Level Mode)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ReticleSpiritLevel(
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isLevel) {
        if (isLevel) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Canvas(modifier = modifier.size(330.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = 155.dp.toPx()
        val midRadius = 100.dp.toPx()
        val snapRadius = 40.dp.toPx()
        val bubbleRadius = 22.dp.toPx()

        val snapColor = if (isLevel) NeonEmerald else BorderStrong

        // Concentric precision rings
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF161A22), Color(0xFF090A0E)),
                center = center,
                radius = outerRadius
            ),
            radius = outerRadius,
            center = center
        )
        drawCircle(color = BorderSubtle, radius = outerRadius, center = center, style = Stroke(1.2.dp.toPx()))
        drawCircle(color = BorderSubtle.copy(alpha = 0.7f), radius = midRadius, center = center, style = Stroke(1.dp.toPx()))
        drawCircle(color = snapColor, radius = snapRadius, center = center, style = Stroke(2.dp.toPx()))

        // Full-span precision crosshairs
        drawLine(
            color = BorderStrong,
            start = Offset(center.x - outerRadius, center.y),
            end = Offset(center.x + outerRadius, center.y),
            strokeWidth = 0.75.dp.toPx()
        )
        drawLine(
            color = BorderStrong,
            start = Offset(center.x, center.y - outerRadius),
            end = Offset(center.x, center.y + outerRadius),
            strokeWidth = 0.75.dp.toPx()
        )

        // Center crosshairs
        drawLine(
            color = snapColor,
            start = Offset(center.x - snapRadius, center.y),
            end = Offset(center.x + snapRadius, center.y),
            strokeWidth = 1.2.dp.toPx()
        )
        drawLine(
            color = snapColor,
            start = Offset(center.x, center.y - snapRadius),
            end = Offset(center.x, center.y + snapRadius),
            strokeWidth = 1.2.dp.toPx()
        )

        // Emerald glow halo when leveled
        if (isLevel) {
            drawCircle(
                color = GlowGreenSoft,
                radius = snapRadius + 10.dp.toPx(),
                center = center,
                style = Stroke(14.dp.toPx())
            )
        }

        // Angle ticks on outer circumference
        for (i in 0 until 72) {
            val deg = i * 5.0
            val rad = Math.toRadians(deg)
            val isMajor = (i % 9 == 0) // every 45°
            val tickLen = if (isMajor) 15.dp.toPx() else 8.dp.toPx()
            val start = Offset(
                (center.x + (outerRadius - tickLen) * cos(rad)).toFloat(),
                (center.y + (outerRadius - tickLen) * sin(rad)).toFloat()
            )
            val end = Offset(
                (center.x + outerRadius * cos(rad)).toFloat(),
                (center.y + outerRadius * sin(rad)).toFloat()
            )
            drawLine(
                color = if (isMajor) TextWhite else BorderStrong,
                start = start,
                end = end,
                strokeWidth = if (isMajor) 1.5.dp.toPx() else 0.75.dp.toPx()
            )
        }

        // Fluid bubble position
        val maxTravel = outerRadius - bubbleRadius - 4.dp.toPx()
        val offsetX = (roll.coerceIn(-25f, 25f) / 25f * maxTravel)
        val offsetY = (pitch.coerceIn(-25f, 25f) / 25f * maxTravel)
        val bubbleCenter = Offset(center.x + offsetX, center.y + offsetY)

        // Modern 3D fluid bubble highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xF5FFFFFF),
                    if (isLevel) Color(0x8800E676) else Color(0x35FFFFFF),
                    Color(0x05FFFFFF)
                ),
                center = Offset(bubbleCenter.x - 5.dp.toPx(), bubbleCenter.y - 5.dp.toPx()),
                radius = bubbleRadius
            ),
            radius = bubbleRadius,
            center = bubbleCenter
        )

        drawCircle(
            color = if (isLevel) NeonEmerald else Color(0xD0FFFFFF),
            radius = bubbleRadius,
            center = bubbleCenter,
            style = Stroke(2.5.dp.toPx())
        )
    }
}
