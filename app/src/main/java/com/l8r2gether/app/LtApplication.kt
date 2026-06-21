package com.l8r2gether.app

import android.app.Application
import com.l8r2gether.app.data.SessionLibraryRepository
import com.l8r2gether.app.data.SessionLibraryRepositoryImpl
import com.l8r2gether.app.data.SessionLibraryStore

class LtApplication : Application() {
    lateinit var sessionLibraryRepository: SessionLibraryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionLibraryRepository = SessionLibraryRepositoryImpl(
            SessionLibraryStore(applicationContext),
        )
    }
}
