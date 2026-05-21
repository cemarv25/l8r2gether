package com.latertogether.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.latertogether.domain.session.SessionLibraryEntry
import com.latertogether.domain.session.sortByLastActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.sessionLibraryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_library",
)

class SessionLibraryStore(context: Context) {
    private val dataStore = context.sessionLibraryDataStore
    private val json = Json { ignoreUnknownKeys = true }

    val entries: Flow<List<SessionLibraryEntry>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_DATA] ?: return@map emptyList()
        val dto = runCatching { json.decodeFromString<SessionLibraryDto>(raw) }.getOrElse {
            SessionLibraryDto()
        }
        sortByLastActivity(dto.entries.values.map { it.toDomain() })
    }

    suspend fun upsert(entry: SessionLibraryEntry) {
        dataStore.edit { prefs ->
            val current = decode(prefs[KEY_DATA])
            val updated = current.entries.toMutableMap()
            updated[entry.contentKey] = entry.toStored()
            prefs[KEY_DATA] = json.encodeToString(SessionLibraryDto(updated))
        }
    }

    suspend fun remove(contentKey: String) {
        dataStore.edit { prefs ->
            val current = decode(prefs[KEY_DATA])
            val updated = current.entries.toMutableMap()
            updated.remove(contentKey)
            prefs[KEY_DATA] = json.encodeToString(SessionLibraryDto(updated))
        }
    }

    private fun decode(raw: String?): SessionLibraryDto =
        raw?.let { runCatching { json.decodeFromString<SessionLibraryDto>(it) }.getOrNull() }
            ?: SessionLibraryDto()

    companion object {
        private val KEY_DATA = stringPreferencesKey("session_library_json")
    }
}
