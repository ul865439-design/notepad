package com.example.notepad.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notes")

@kotlinx.serialization.Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NoteRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val notesKey = stringPreferencesKey("notes_list")

    val notes: Flow<List<Note>> = context.dataStore.data.map { preferences ->
        val notesJson = preferences[notesKey] ?: "[]"
        try {
            json.decodeFromString(notesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveNote(note: Note) {
        context.dataStore.edit { preferences ->
            val currentNotes = try {
                json.decodeFromString<List<Note>>(preferences[notesKey] ?: "[]")
            } catch (e: Exception) {
                emptyList()
            }
            val updatedNotes = currentNotes.filter { it.id != note.id } + note
            preferences[notesKey] = json.encodeToString(updatedNotes.sortedByDescending { it.timestamp })
        }
    }

    suspend fun deleteNote(noteId: String) {
        context.dataStore.edit { preferences ->
            val currentNotes = try {
                json.decodeFromString<List<Note>>(preferences[notesKey] ?: "[]")
            } catch (e: Exception) {
                emptyList()
            }
            preferences[notesKey] = json.encodeToString(currentNotes.filter { it.id != noteId })
        }
    }
}