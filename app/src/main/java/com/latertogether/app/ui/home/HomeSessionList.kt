package com.latertogether.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.latertogether.app.R
import com.latertogether.app.ui.theme.LtOnBackground
import com.latertogether.app.viewmodel.SessionListItemUi

@Composable
fun HomeSessionList(
    sessions: List<SessionListItemUi>,
    onResumeSession: (String) -> Unit,
    onSyncNow: (String) -> Unit,
    onRemoveSession: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.home_pick_up_title),
            style = MaterialTheme.typography.headlineMedium,
            color = LtOnBackground,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(sessions, key = { it.contentKey }) { item ->
                SessionRowCard(
                    item = item,
                    onClick = { onResumeSession(item.contentKey) },
                    onSyncNow = { onSyncNow(item.contentKey) },
                    onRemove = { onRemoveSession(item.contentKey) },
                )
            }
        }
    }
}

@Composable
private fun SessionRowCard(
    item: SessionListItemUi,
    onClick: () -> Unit,
    onSyncNow: () -> Unit,
    onRemove: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = LtOnBackground,
                    )
                    Text(
                        text = item.pauseSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.session_menu),
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.session_sync_now)) },
                        onClick = {
                            menuExpanded = false
                            onSyncNow()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.session_remove)) },
                        onClick = {
                            menuExpanded = false
                            onRemove()
                        },
                    )
                }
            }
        }
    }
}
