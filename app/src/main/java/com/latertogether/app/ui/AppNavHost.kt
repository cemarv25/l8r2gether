package com.latertogether.app.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.latertogether.app.R
import com.latertogether.app.data.SessionLibraryRepository
import com.latertogether.app.ui.home.HomeScreen
import com.latertogether.app.ui.watch.WatchScreenStub
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val WATCH = "watch/{contentKey}"

    fun watch(contentKey: String): String =
        "watch/${java.net.URLEncoder.encode(contentKey, Charsets.UTF_8.name())}"
}

@Composable
fun AppNavHost(sessionLibraryRepository: SessionLibraryRepository) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val onComingSoon: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(context.getString(R.string.nav_coming_soon))
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                repository = sessionLibraryRepository,
                snackbarHostState = snackbarHostState,
                onNavigateToWatch = { key ->
                    navController.navigate(Routes.watch(key))
                },
                onComingSoon = onComingSoon,
            )
        }
        composable(
            route = Routes.WATCH,
            arguments = listOf(navArgument("contentKey") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("contentKey").orEmpty()
            val contentKey = java.net.URLDecoder.decode(encoded, Charsets.UTF_8.name())
            WatchScreenStub(
                contentKey = contentKey,
                snackbarHostState = snackbarHostState,
                onComingSoon = onComingSoon,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
