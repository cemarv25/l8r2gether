package com.l8r2gether.app.data

import com.l8r2gether.domain.session.SessionLibraryEntry
import kotlinx.coroutines.flow.Flow

interface SessionLibraryRepository {
    val sessions: Flow<List<SessionLibraryEntry>>
    suspend fun upsert(entry: SessionLibraryEntry)
}

class SessionLibraryRepositoryImpl(
    private val store: SessionLibraryStore,
) : SessionLibraryRepository {
    override val sessions: Flow<List<SessionLibraryEntry>> = store.entries

    override suspend fun upsert(entry: SessionLibraryEntry) {
        store.upsert(entry)
    }
}
