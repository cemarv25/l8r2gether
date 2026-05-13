package com.latertogether.data

import com.latertogether.domain.repository.MessageRepository
import io.github.jan.supabase.SupabaseClient

object MessageRepositoryFactory {
    fun create(supabaseUrl: String, supabaseAnonKey: String): MessageRepository =
        when {
            supabaseUrl.isBlank() || supabaseAnonKey.isBlank() -> StubMessageRepository()
            else -> MessageRepositoryImpl(SupabaseClientProvider.create(supabaseUrl, supabaseAnonKey))
        }

    fun create(client: SupabaseClient): MessageRepository = MessageRepositoryImpl(client)
}
