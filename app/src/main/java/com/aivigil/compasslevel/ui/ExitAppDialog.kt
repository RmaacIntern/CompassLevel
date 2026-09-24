package com.aivigil.compasslevel.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aivigil.compasslevel.ui.theme.PureBlack
import com.aivigil.compasslevel.ui.theme.RedAccent
import com.aivigil.compasslevel.ui.theme.SkinPalette

@Composable
fun ExitAppDialog(
    skin: SkinPalette,
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedRating by remember { mutableIntStateOf(5) }
    var ratingSubmitted by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, skin.cardBorder, RoundedCornerShape(24.dp)),
            color = skin.surfaceBackground,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(skin.primaryAccent.copy(alpha = 0.15f))
                        .border(1.5.dp, skin.primaryAccent.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RateReview,
                        contentDescription = null,
                        tint = skin.primaryAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Are you sure you want to exit?",
                        color = skin.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Before you leave, how would you rate your experience with Compass & Clinometer?",
                        color = skin.textSecondary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Default,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                // Interactive 5-Star Rating Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = skin.cardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.cardBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (star in 1..5) {
                                val isSelected = star <= selectedRating
                                val scale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.15f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "star_scale_$star"
                                )
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedRating = star
                                        ratingSubmitted = false
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .scale(scale)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "$star Stars",
                                        tint = if (isSelected) Color(0xFFFFC107) else skin.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                        }

                        // Rating Feedback Caption
                        val feedbackText = when (selectedRating) {
                            5 -> "⭐⭐⭐⭐⭐ Outstanding! Thank you!"
                            4 -> "⭐⭐⭐⭐ Great experience! Thank you!"
                            3 -> "⭐⭐⭐ Good, we are continuously improving!"
                            2 -> "⭐⭐ Needs improvement, let us know how!"
                            else -> "⭐ We appreciate your honest feedback!"
                        }

                        Text(
                            text = feedbackText,
                            color = if (selectedRating >= 4) Color(0xFFFFC107) else skin.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Default,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Rate on Google Play or Send Feedback Action
                AnimatedVisibility(visible = selectedRating >= 4) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            openPlayStoreRating(context)
                            ratingSubmitted = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = PureBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = PureBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (ratingSubmitted) "Thank You for Rating!" else "Rate 5 Stars on Google Play",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                AnimatedVisibility(visible = selectedRating in 1..3) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            sendFeedbackEmail(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = skin.primaryAccent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, skin.primaryAccent)
                    ) {
                        Text(
                            text = "Send Feedback / Bug Report",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons: Cancel vs Exit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stay Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = skin.primaryAccent,
                            contentColor = PureBlack
                        )
                    ) {
                        Text(
                            text = "Stay",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Confirm Exit Button
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConfirmExit()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RedAccent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExitToApp,
                            contentDescription = null,
                            tint = RedAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = RedAccent
                        )
                    }
                }
            }
        }
    }
}

private fun openPlayStoreRating(context: Context) {
    val packageName = context.packageName
    val uri = Uri.parse("market://details?id=$packageName")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
    }
    try {
        context.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        try {
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Could not open Google Play Store", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun sendFeedbackEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@aivigil.com"))
        putExtra(Intent.EXTRA_SUBJECT, "Compass & Clinometer Feedback")
        putExtra(Intent.EXTRA_TEXT, "Hello Team,\n\nI have the following feedback for the app:\n\n")
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Send Feedback"))
    } catch (e: Exception) {
        Toast.makeText(context, "No email client installed", Toast.LENGTH_SHORT).show()
    }
}
