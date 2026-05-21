package com.latertogether.app.data

import com.latertogether.domain.session.SessionLibraryEntry
import kotlinx.coroutines.flow.Flow

interface SessionLibraryRepository {
    val sessions: Flow<List<SessionLibraryEntry>>
    suspend fun upsert(entry: SessionLibraryEntry)
    suspend fun remove(contentKey: String)
}

class SessionLibraryRepositoryImpl(
    private val store: SessionLibraryStore,
) : SessionLibraryRepository {
    override val sessions: Flow<List<SessionLibraryEntry>> = store.entries

    override suspend fun upsert(entry: SessionLibraryEntry) {
        store.upsert(entry)
    }

    override suspend fun remove(contentKey: String) {
        store.remove(contentKey)
    }
}
