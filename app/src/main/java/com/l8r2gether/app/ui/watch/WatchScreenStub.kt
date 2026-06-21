package com.l8r2gether.app.ui.watch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.l8r2gether.app.R
import com.l8r2gether.app.ui.shell.AppShell
import com.l8r2gether.domain.session.prettifyContentKey
import androidx.compose.material3.SnackbarHostState

@Composable
fun WatchScreenStub(
    contentKey: String,
    snackbarHostState: SnackbarHostState,
    onComingSoon: () -> Unit,
    onBack: () -> Unit,
) {
    AppShell(onComingSoon = onComingSoon, snackbarHostState = snackbarHostState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.watch_stub_back),
                )
            }
            Text(
                text = stringResource(R.string.watch_stub_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = prettifyContentKey(contentKey),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = contentKey,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.watch_stub_message),
                style = MaterialTheme.typography.bodyLarge,
            )
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.watch_stub_back))
            }
        }
    }
}
