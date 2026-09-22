package com.aivigil.compasslevel.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class MeasurementNote(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "Compass", "Level", "Clinometer", "Location"
    val title: String,
    val primaryValue: String,
    val secondaryDetails: String,
    val userNotes: String = ""
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("timestamp", timestamp)
            put("type", type)
            put("title", title)
            put("primaryValue", primaryValue)
            put("secondaryDetails", secondaryDetails)
            put("userNotes", userNotes)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): MeasurementNote {
            return MeasurementNote(
                id = json.optString("id", UUID.randomUUID().toString()),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                type = json.optString("type", "General"),
                title = json.optString("title", "Saved Measurement"),
                primaryValue = json.optString("primaryValue", ""),
                secondaryDetails = json.optString("secondaryDetails", ""),
                userNotes = json.optString("userNotes", "")
            )
        }
    }
}

class MeasurementNotesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("compass_level_notes_prefs", Context.MODE_PRIVATE)

    private val _notes = MutableStateFlow<List<MeasurementNote>>(emptyList())
    val notes: StateFlow<List<MeasurementNote>> = _notes.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        val rawJson = prefs.getString(KEY_NOTES, "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<MeasurementNote>()
            for (i in 0 until jsonArray.length()) {
                list.add(MeasurementNote.fromJson(jsonArray.getJSONObject(i)))
            }
            _notes.value = list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            _notes.value = emptyList()
        }
    }

    fun saveNote(
        type: String,
        title: String,
        primaryValue: String,
        secondaryDetails: String,
        userNotes: String = ""
    ) {
        val newNote = MeasurementNote(
            type = type,
            title = if (title.isBlank()) "$type Measurement" else title,
            primaryValue = primaryValue,
            secondaryDetails = secondaryDetails,
            userNotes = userNotes
        )
        val currentList = _notes.value.toMutableList()
        currentList.add(0, newNote)
        persistList(currentList)
    }

    fun deleteNote(id: String) {
        val currentList = _notes.value.filter { it.id != id }
        persistList(currentList)
    }

    fun clearAllNotes() {
        persistList(emptyList())
    }

    private fun persistList(list: List<MeasurementNote>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it.toJson()) }
        prefs.edit().putString(KEY_NOTES, jsonArray.toString()).apply()
        _notes.value = list
    }

    companion object {
        private const val KEY_NOTES = "measurement_notes_data"
    }
}
