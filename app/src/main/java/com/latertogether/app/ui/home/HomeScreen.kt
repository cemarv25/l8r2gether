package com.latertogether.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartSession: (contentKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val key = rememberSaveable { mutableStateOf("manual:demo") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("LaterTogether companion")
        OutlinedTextField(
            value = key.value,
            onValueChange = { key.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Content key") },
            singleLine = true,
        )
        Button(
            onClick = { onStartSession(key.value.trim()) },
            enabled = key.value.isNotBlank(),
        ) {
            Text("Start watch session")
        }
    }
}
