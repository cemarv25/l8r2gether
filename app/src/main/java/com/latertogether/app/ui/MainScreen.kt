@file:OptIn(ExperimentalMaterial3Api::class)

package com.latertogether.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.latertogether.app.BuildConfig
import com.latertogether.app.accessibility.LaterTogetherAccessibilityService
import com.latertogether.app.viewmodel.WatchUiState
import com.latertogether.app.viewmodel.WatchViewModel
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.time.MediaTimeParser

@Composable
fun MainScreen(vm: WatchViewModel) {
    val ui by vm.uiState.collectAsState()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showAuth by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LaterTogether") },
                actions = {
                    if (BuildConfig.SUPABASE_URL.isNotBlank()) {
                        TextButton(onClick = { showAuth = true }) { Text("Sign in") }
                    }
                },
            )
        },
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SessionPanel(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                ui = ui,
                vm = vm,
                onOpenAccessibilitySettings = {
                    context.startActivity(
                        LaterTogetherAccessibilityService.settingsIntent()
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                },
            )
            ChatPanel(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                ui = ui,
                vm = vm,
            )
        }
    }

    if (ui.confidencePrompt) {
        AlertDialog(
            onDismissRequest = vm::dismissConfidencePrompt,
            title = { Text("Still in sync?") },
            text = { Text("Confidence dropped or observations are stale. Confirm playback alignment.") },
            confirmButton = {
                TextButton(onClick = vm::acknowledgeStillSynced) { Text("Still synced") }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.dismissConfidencePrompt()
                }) { Text("Fix time") }
            },
        )
    }

    if (showAuth && BuildConfig.SUPABASE_URL.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showAuth = false },
            title = { Text("Supabase sign-in") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Uses Supabase email/password. Keys come from local.properties — never commit secrets.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.signIn(email, password)
                        showAuth = false
                    },
                ) { Text("Sign in") }
            },
            dismissButton = {
                TextButton(onClick = { showAuth = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SessionPanel(
    modifier: Modifier = Modifier,
    ui: WatchUiState,
    vm: WatchViewModel,
    onOpenAccessibilitySettings: () -> Unit,
) {
    val ctx = LocalContext.current
    val a11yRunning = LaterTogetherAccessibilityService.isEnabled(ctx)
    val rate = ui.session?.playbackRate?.toFloat()?.coerceIn(0.5f, 2f) ?: 1f

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Thread / session", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = ui.contentKey,
            onValueChange = vm::setContentKey,
            label = { Text("Content key (e.g. yt:dQw4w9WgXcQ)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !ui.sessionActive,
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::startWatchSession, enabled = !ui.sessionActive) {
                Text("Start watch session")
            }
            Button(onClick = vm::stopWatchSession, enabled = ui.sessionActive) {
                Text("Stop")
            }
            Button(onClick = vm::refreshMessages) { Text("Refresh thread") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = ui.playbackExplicit == PlaybackState.Playing,
                onClick = { vm.userSetPlayingExplicit(PlaybackState.Playing) },
                label = { Text("Playing") },
                enabled = ui.sessionActive,
            )
            FilterChip(
                selected = ui.playbackExplicit == PlaybackState.Paused,
                onClick = { vm.userSetPlayingExplicit(PlaybackState.Paused) },
                label = { Text("Paused") },
                enabled = ui.sessionActive,
            )
        }
        Text(
            "Playback rate (extrapolation)",
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = rate,
            onValueChange = { vm.setPlaybackRate(it.toDouble()) },
            valueRange = 0.5f..2f,
            enabled = ui.sessionActive,
        )
        Text("${"%.2f".format(rate)}×", style = MaterialTheme.typography.bodyMedium)

        HorizontalDivider()
        Text("Sync now", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = ui.syncTimeInput,
            onValueChange = vm::setSyncInput,
            label = { Text("Current media time (mm:ss or seconds)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = ui.sessionActive,
            singleLine = true,
        )
        Button(onClick = vm::manualSyncNow, enabled = ui.sessionActive) {
            Text("Apply checkpoint")
        }

        HorizontalDivider()
        Text(
            "t_est ≈ ${"%.1f".format(ui.tEstSec)} s   (${MediaTimeParser.formatMmSs(ui.tEstSec)})",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(ui.statusLine, style = MaterialTheme.typography.bodySmall)

        HorizontalDivider()
        Text("Accessibility fallback (optional)", style = MaterialTheme.typography.titleSmall)
        Text(
            "Parses node trees only — no screen capture. Enable the LaterTogether accessibility service after granting consent.",
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = ui.accessibilityFallbackEnabled,
                onClick = { vm.setAccessibilityFallback(!ui.accessibilityFallbackEnabled) },
                label = { Text("Use fallback lane") },
            )
            TextButton(onClick = onOpenAccessibilitySettings) { Text("Open accessibility settings") }
        }
        Text(
            if (a11yRunning) "Service enabled in OS settings." else "Service not enabled.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ChatPanel(
    modifier: Modifier = Modifier,
    ui: WatchUiState,
    vm: WatchViewModel,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Chat", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ui.visibleMessages, key = { it.id }) { m ->
                    Column {
                        Text(
                            "${MediaTimeParser.formatMmSs(m.mediaTimestamp)} — ${m.body}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "by ${m.authorId.take(8)}…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
        Text(
            "Composer anchors to current t_est (${"%.2f".format(ui.tEstSec)} s)",
            style = MaterialTheme.typography.labelMedium,
        )
        OutlinedTextField(
            value = ui.composerText,
            onValueChange = vm::setComposerText,
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Button(
            onClick = vm::sendCurrentMessage,
            enabled = ui.sessionActive && ui.composerText.isNotBlank(),
        ) {
            Text("Send")
        }
        Spacer(Modifier.height(8.dp))
    }
}
