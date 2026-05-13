package com.latertogether.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.latertogether.app.ui.MainScreen
import com.latertogether.app.ui.theme.LtTheme
import com.latertogether.app.viewmodel.WatchViewModel
import com.latertogether.app.viewmodel.WatchViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LtTheme {
                val app = application as LtApplication
                val vm: WatchViewModel = viewModel(factory = WatchViewModelFactory(app))
                MainScreen(vm)
            }
        }
    }
}
