package com.l8r2gether.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.l8r2gether.app.R
import com.l8r2gether.app.ui.theme.LtAccent
import com.l8r2gether.app.ui.theme.LtContainerLow
import com.l8r2gether.app.ui.theme.LtOnBackground
import com.l8r2gether.app.ui.theme.LtOnPrimary
import com.l8r2gether.app.ui.theme.LtPrimary
import com.l8r2gether.app.ui.theme.LtRailSelected
import com.l8r2gether.app.ui.theme.LtSurface
import com.l8r2gether.app.viewmodel.SessionListItemUi

private val PillShape = RoundedCornerShape(percent = 50)

@Composable
fun HomeSessionList(
    sessions: List<SessionListItemUi>,
    onSelectNewMedia: () -> Unit,
    onResumeSession: (String) -> Unit,
    onSyncNow: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.5f), LtSurface),
                    radius = 900f,
                ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                SelectNewMediaBar(onClick = onSelectNewMedia)
            }
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(0.85f),
                ) {
                    Text(
                        text = stringResource(R.string.home_pick_up_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = LtOnBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_pick_up_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(sessions, key = { it.contentKey }) { item ->
                SessionCard(
                    item = item,
                    onResume = { onResumeSession(item.contentKey) },
                    onAdjustTimestamp = { onSyncNow(item.contentKey) },
                    modifier = Modifier.fillMaxWidth(0.85f),
                )
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
        FloatingActionButton(
            onClick = onFavoritesClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 32.dp, bottom = 32.dp),
            shape = CircleShape,
            containerColor = LtPrimary,
            contentColor = LtOnPrimary,
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = stringResource(R.string.cd_favorites_fab),
            )
        }
    }
}

@Composable
private fun SelectNewMediaBar(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.85f),
        shape = PillShape,
        color = LtRailSelected,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.PlayCircle,
                    contentDescription = null,
                    tint = LtPrimary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(R.string.home_select_new_media),
                    style = MaterialTheme.typography.labelLarge,
                    color = LtPrimary,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LtPrimary,
            )
        }
    }
}

@Composable
private fun SessionCard(
    item: SessionListItemUi,
    onResume: () -> Unit,
    onAdjustTimestamp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.EventNote,
                    contentDescription = stringResource(R.string.cd_session_notes),
                    tint = LtAccent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SessionThumbnail(
                    title = item.displayTitle,
                    contentKey = item.contentKey,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = item.displayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = LtOnBackground,
                        )
                        Text(
                            text = item.pauseSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (item.isMostRecent) {
                            Button(
                                onClick = onResume,
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LtPrimary,
                                    contentColor = LtOnPrimary,
                                ),
                                contentPadding = ButtonDefaults.ContentPadding,
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.home_resume_session))
                            }
                        } else {
                            Button(
                                onClick = onResume,
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LtContainerLow,
                                    contentColor = LtPrimary,
                                ),
                                contentPadding = ButtonDefaults.ContentPadding,
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.home_resume))
                            }
                        }
                        OutlinedButton(
                            onClick = onAdjustTimestamp,
                            shape = PillShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LtPrimary),
                        ) {
                            Text(stringResource(R.string.home_adjust_timestamp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionThumbnail(
    title: String,
    contentKey: String,
    modifier: Modifier = Modifier,
) {
    val tint = thumbnailTintFor(contentKey)
    Box(
        modifier = modifier
            .width(120.dp)
            .height(80.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                Brush.linearGradient(
                    colors = listOf(tint, tint.copy(alpha = 0.7f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.cd_session_thumbnail, title),
                tint = LtPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private fun thumbnailTintFor(contentKey: String): Color {
    val palette = listOf(
        Color(0xFFC9A88E),
        Color(0xFF8BA89A),
        Color(0xFFB8A0C4),
        Color(0xFF9AABB8),
    )
    return palette[kotlin.math.abs(contentKey.hashCode()) % palette.size]
}
