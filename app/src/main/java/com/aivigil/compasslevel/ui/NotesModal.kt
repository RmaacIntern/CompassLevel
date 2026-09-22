package com.aivigil.compasslevel.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivigil.compasslevel.data.MeasurementNote
import com.aivigil.compasslevel.data.MeasurementNotesManager
import com.aivigil.compasslevel.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesModal(
    notesManager: MeasurementNotesManager,
    currentMode: String,
    currentMeasurementSummary: Pair<String, String>, // (primaryValue, secondaryDetails)
    skin: SkinPalette,
    onClose: () -> Unit
) {
    val notes by notesManager.notes.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filters = listOf("All", "Compass", "Level", "Clinometer", "Location")

    val filteredNotes = remember(notes, selectedFilter) {
        if (selectedFilter == "All") notes
        else notes.filter { it.type.equals(selectedFilter, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = skin.surfaceBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = skin.cardBorder) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SAVED MEASUREMENTS",
                        color = skin.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${notes.size} total entries recorded",
                        color = skin.textSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Default
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(skin.cardBackground)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = skin.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Save Current Reading Button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = skin.primaryAccent),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PostAdd,
                    contentDescription = null,
                    tint = PureBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECORD CURRENT ${currentMode.uppercase()}",
                    color = PureBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) skin.primaryAccent.copy(alpha = 0.15f) else skin.cardBackground)
                            .border(
                                1.dp,
                                if (isSelected) skin.primaryAccent else skin.cardBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) skin.primaryAccent else skin.textSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes List
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No saved measurements in $selectedFilter",
                            color = skin.textSecondary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Default
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap 'Record Current' above to save readings",
                            color = skin.textSecondary.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        MeasurementNoteCard(
                            note = note,
                            skin = skin,
                            onDelete = { notesManager.deleteNote(note.id) },
                            onCopy = {
                                val text = "${note.title}\n${note.primaryValue}\n${note.secondaryDetails}\nNotes: ${note.userNotes}\nDate: ${note.formattedDate}"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("Measurement Note", text)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAddDialog) {
        AddNoteDialog(
            type = currentMode,
            primaryValue = currentMeasurementSummary.first,
            secondaryDetails = currentMeasurementSummary.second,
            skin = skin,
            onDismiss = { showAddDialog = false },
            onConfirm = { customTitle, customNotes ->
                notesManager.saveNote(
                    type = currentMode,
                    title = customTitle,
                    primaryValue = currentMeasurementSummary.first,
                    secondaryDetails = currentMeasurementSummary.second,
                    userNotes = customNotes
                )
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesModal(
    notesManager: MeasurementNotesManager,
    currentMode: String,
    currentMeasurementSummary: Pair<String, String>,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onClose: () -> Unit
) {
    NotesModal(
        notesManager = notesManager,
        currentMode = currentMode,
        currentMeasurementSummary = currentMeasurementSummary,
        skin = skin.palette(true),
        onClose = onClose
    )
}

@Composable
fun MeasurementNoteCard(
    note: MeasurementNote,
    skin: SkinPalette,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = skin.surfaceBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.cardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (note.type) {
                                    "Compass" -> skin.secondaryAccent.copy(alpha = 0.2f)
                                    "Level" -> skin.primaryAccent.copy(alpha = 0.2f)
                                    "Clinometer" -> Color(0xFFFF9100).copy(alpha = 0.2f)
                                    else -> skin.primaryAccent.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = note.type.uppercase(),
                            color = when (note.type) {
                                "Compass" -> skin.secondaryAccent
                                "Level" -> skin.primaryAccent
                                "Clinometer" -> Color(0xFFFF9100)
                                else -> skin.primaryAccent
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.title,
                        color = skin.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Default
                    )
                }

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = skin.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = RedAccent.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.primaryValue,
                color = skin.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            )

            if (note.secondaryDetails.isNotBlank()) {
                Text(
                    text = note.secondaryDetails,
                    color = skin.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default
                )
            }

            if (note.userNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${note.userNotes}",
                    color = skin.textPrimary.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.formattedDate,
                color = skin.textSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Default
            )
        }
    }
}

@Composable
fun MeasurementNoteCard(
    note: MeasurementNote,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    MeasurementNoteCard(
        note = note,
        skin = skin.palette(true),
        onDelete = onDelete,
        onCopy = onCopy
    )
}

@Composable
fun AddNoteDialog(
    type: String,
    primaryValue: String,
    secondaryDetails: String,
    skin: SkinPalette,
    onDismiss: () -> Unit,
    onConfirm: (title: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "RECORD ${type.uppercase()}",
                color = skin.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            )
        },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = skin.surfaceBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skin.cardBorder))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = primaryValue,
                            color = skin.primaryAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default
                        )
                        Text(
                            text = secondaryDetails,
                            color = skin.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Location (e.g. Wall A)") },
                    placeholder = { Text("Optional title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Comments") },
                    placeholder = { Text("Details about measurement") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, notesText) },
                colors = ButtonDefaults.buttonColors(containerColor = skin.primaryAccent)
            ) {
                Text("SAVE", color = PureBlack, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = skin.textSecondary, fontFamily = FontFamily.Default)
            }
        },
        containerColor = skin.cardBackground,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AddNoteDialog(
    type: String,
    primaryValue: String,
    secondaryDetails: String,
    skin: AppSkin = AppSkin.CLASSIC_EMERALD,
    onDismiss: () -> Unit,
    onConfirm: (title: String, notes: String) -> Unit
) {
    AddNoteDialog(
        type = type,
        primaryValue = primaryValue,
        secondaryDetails = secondaryDetails,
        skin = skin.palette(true),
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
