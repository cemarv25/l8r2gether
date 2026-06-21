package com.l8r2gether.app.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l8r2gether.app.data.SessionLibraryRepository
import com.l8r2gether.app.ui.shell.AppShell
import com.l8r2gether.app.viewmodel.HomeEvent
import com.l8r2gether.app.viewmodel.HomeViewModel
import com.l8r2gether.app.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    repository: SessionLibraryRepository,
    snackbarHostState: SnackbarHostState,
    onNavigateToWatch: (String) -> Unit,
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
    val uiState by vm.uiState.collectAsState()
    LaunchedEffect(vm) {
        vm.events.collectLatest { event ->
            when (event) {
                is HomeEvent.NavigateToWatch -> onNavigateToWatch(event.contentKey)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.clearError()
        }
    }

    AppShell(onComingSoon = onComingSoon, snackbarHostState = snackbarHostState) {
        if (uiState.isEmpty) {
            HomeEmptyState(
                onStartNewSession = vm::onStartNewSessionClick,
                onBrowseLibrary = vm::onBrowseLibraryClick,
                modifier = modifier,
            )
        } else {
            HomeSessionList(
                sessions = uiState.sessions,
                onSelectNewMedia = vm::onStartNewSessionClick,
                onResumeSession = vm::onResumeSession,
                onSyncNow = vm::onOpenSyncNow,
                onFavoritesClick = onComingSoon,
                modifier = modifier,
            )
        }
    }

    if (uiState.showNewSessionSheet) {
        NewSessionSheet(
            contentKey = uiState.newSessionContentKey,
            errorMessage = uiState.errorMessage,
            onContentKeyChange = vm::onNewSessionContentKeyChange,
            onConfirm = vm::onConfirmNewSession,
            onDismiss = vm::onDismissNewSessionSheet,
        )
    }

    if (uiState.syncTargetContentKey != null) {
        SyncNowDialog(
            timeInput = uiState.syncTimeInput,
            errorMessage = uiState.errorMessage,
            onTimeInputChange = vm::onSyncTimeInputChange,
            onNudge = vm::onNudgeSyncTime,
            onConfirm = vm::onApplySyncNow,
            onDismiss = vm::onDismissSyncNow,
        )
    }
}
