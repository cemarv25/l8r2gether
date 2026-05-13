package com.latertogether.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android

object SupabaseClientFactory {
    fun create(supabaseUrl: String, supabaseKey: String) =
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey,
        ) {
            httpEngine = Android.create()
            install(Auth)
            install(Postgrest)
        }
}
