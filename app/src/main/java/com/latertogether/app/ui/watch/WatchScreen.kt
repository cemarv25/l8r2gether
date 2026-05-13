@file:OptIn(ExperimentalMaterial3Api::class)

package com.latertogether.app.ui.watch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.latertogether.domain.model.PlaybackState
import com.latertogether.domain.time.MediaTimeParser

@Composable
fun WatchScreen(
    viewModel: WatchViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()

    if (ui.showConfidencePrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfidencePrompt() },
            title = { Text("Stay in sync?") },
            text = {
                Text(
                    "Confidence is ${ui.session.confidence}. Confirm you're still aligned or sync manually.",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.acknowledgeStillSynced() }) {
                    Text("Still synced")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfidencePrompt() }) {
                    Text("Dismiss")
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(ui.session.contentKey) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshMessages() },
                        enabled = !ui.loadingThread,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh thread")
                    }
                },
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            val wide = maxWidth >= 720.dp
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    SessionPanel(
                        ui = ui,
                        viewModel = viewModel,
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                            .padding(16.dp),
                    )
                    ChatPanel(
                        ui = ui,
                        viewModel = viewModel,
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                            .padding(16.dp),
                    )
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    SessionPanel(
                        ui = ui,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                    ChatPanel(
                        ui = ui,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionPanel(
    ui: WatchUiState,
    viewModel: WatchViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Estimated time", style = MaterialTheme.typography.titleMedium)
        Text(
            MediaTimeParser.formatMmSs(ui.tEstSec),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text("Confidence: ${ui.session.confidence}")
        Text("Observation: ${ui.session.observationSource}")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = ui.session.playbackState == PlaybackState.Playing,
                onClick = { viewModel.togglePlayback(toPlaying = true) },
                label = { Text("Playing") },
            )
            FilterChip(
                selected = ui.session.playbackState == PlaybackState.Paused,
                onClick = { viewModel.togglePlayback(toPlaying = false) },
                label = { Text("Paused") },
            )
        }

        Spacer(Modifier.height(8.dp))
        Text("Manual sync", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = ui.syncTimeInput,
            onValueChange = viewModel::setSyncInput,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Media time (mm:ss or seconds)") },
            singleLine = true,
            isError = ui.syncInputError != null,
            supportingText = {
                ui.syncInputError?.let { Text(it) }
            },
        )
        Button(onClick = { viewModel.applyManualSync() }) {
            Text("Apply checkpoint")
        }

        Text(
            "Enable the LaterTogether notification listener to read MediaSession positions from other apps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Optional accessibility probe parses on-screen time text when session metadata is missing.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChatPanel(
    ui: WatchUiState,
    viewModel: WatchViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Thread", style = MaterialTheme.typography.titleMedium)
        if (ui.loadingThread) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ui.visibleMessages, key = { it.id }) { m ->
                    val highlight = ui.newlyRevealed.any { it.id == m.id }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (highlight) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                MediaTimeParser.formatMmSs(m.mediaTimestamp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Text(m.body, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = ui.chatInput,
            onValueChange = viewModel::setChatInput,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Message at current t_est") },
            trailingIcon = {
                IconButton(
                    onClick = { viewModel.sendChat() },
                    enabled = ui.chatInput.isNotBlank(),
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            },
            isError = ui.sendError != null,
            supportingText = { ui.sendError?.let { Text(it) } },
        )
    }
}
