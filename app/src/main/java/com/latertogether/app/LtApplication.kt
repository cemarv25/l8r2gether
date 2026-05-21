package com.latertogether.app

import android.app.Application
import com.latertogether.app.data.SessionLibraryRepository
import com.latertogether.app.data.SessionLibraryRepositoryImpl
import com.latertogether.app.data.SessionLibraryStore

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
