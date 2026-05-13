package com.latertogether.app

import android.app.Application
import com.latertogether.data.MessageRepositoryFactory
import com.latertogether.domain.repository.MessageRepository

class LaterTogetherApp : Application() {

    lateinit var messageRepository: MessageRepository
        private set

    override fun onCreate() {
        super.onCreate()
        messageRepository = MessageRepositoryFactory.create(
            BuildConfig.SUPABASE_URL,
            BuildConfig.SUPABASE_ANON_KEY,
        )
    }
}
