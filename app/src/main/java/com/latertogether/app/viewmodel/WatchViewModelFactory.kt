package com.latertogether.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.latertogether.app.LtApplication

class WatchViewModelFactory(
    private val application: LtApplication,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val prefs = application.getSharedPreferences("lt_prefs", android.content.Context.MODE_PRIVATE)
        val container = application.container
        return WatchViewModel(
            application = application,
            repository = container.messageRepository,
            supabaseClient = container.supabaseClient,
            prefs = prefs,
        ) as T
    }
}
