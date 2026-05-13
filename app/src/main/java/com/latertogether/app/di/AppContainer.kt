package com.latertogether.app.di

import android.app.Application
import com.latertogether.app.BuildConfig
import com.latertogether.data.MessageRepositoryImpl
import com.latertogether.data.SupabaseClientFactory
import com.latertogether.app.data.InMemoryMessageRepository
import com.latertogether.domain.repository.MessageRepository
import io.github.jan.supabase.SupabaseClient

class AppContainer(app: Application) {
    val supabaseClient: SupabaseClient? =
        if (BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            SupabaseClientFactory.create(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
            )
        } else {
            null
        }

    val messageRepository: MessageRepository =
        if (supabaseClient != null) {
            MessageRepositoryImpl(supabaseClient)
        } else {
            InMemoryMessageRepository()
        }
}
