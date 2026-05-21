package com.latertogether.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.latertogether.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionSheet(
    contentKey: String,
    errorMessage: String?,
    onContentKeyChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.new_session_title),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = contentKey,
                onValueChange = onContentKeyChange,
                label = { Text(stringResource(R.string.new_session_content_key_label)) },
                placeholder = { Text(stringResource(R.string.new_session_content_key_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it) } },
            )
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = contentKey.isNotBlank(),
            ) {
                Text(stringResource(R.string.new_session_confirm))
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.new_session_cancel))
            }
        }
    }
}
