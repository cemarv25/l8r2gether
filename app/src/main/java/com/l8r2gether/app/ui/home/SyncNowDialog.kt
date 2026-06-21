package com.l8r2gether.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.l8r2gether.app.R
import com.l8r2gether.domain.time.MediaTimeParser

@Composable
fun SyncNowDialog(
    timeInput: String,
    errorMessage: String?,
    onTimeInputChange: (String) -> Unit,
    onNudge: (Double) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sync_now_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = timeInput,
                    onValueChange = onTimeInputChange,
                    label = { Text(stringResource(R.string.sync_now_label)) },
                    placeholder = { Text(stringResource(R.string.sync_now_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = false,
                        onClick = { onNudge(-MediaTimeParser.NUDGE_HALF_MINUTE_SEC) },
                        label = { Text(stringResource(R.string.sync_nudge_minus_30s)) },
                    )
                    FilterChip(
                        selected = false,
                        onClick = { onNudge(MediaTimeParser.NUDGE_HALF_MINUTE_SEC) },
                        label = { Text(stringResource(R.string.sync_nudge_30s)) },
                    )
                    FilterChip(
                        selected = false,
                        onClick = { onNudge(-MediaTimeParser.NUDGE_TWO_MINUTES_SEC) },
                        label = { Text(stringResource(R.string.sync_nudge_minus_2m)) },
                    )
                    FilterChip(
                        selected = false,
                        onClick = { onNudge(MediaTimeParser.NUDGE_TWO_MINUTES_SEC) },
                        label = { Text(stringResource(R.string.sync_nudge_2m)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.sync_now_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.sync_now_cancel))
            }
        },
        shape = MaterialTheme.shapes.medium,
    )
}
