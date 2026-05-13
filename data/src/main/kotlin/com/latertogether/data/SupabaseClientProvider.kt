package com.latertogether.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {
    fun create(supabaseUrl: String, supabaseAnonKey: String): SupabaseClient =
        createSupabaseClient(supabaseUrl, supabaseAnonKey) {
            install(Auth)
            install(Postgrest)
        }
}
