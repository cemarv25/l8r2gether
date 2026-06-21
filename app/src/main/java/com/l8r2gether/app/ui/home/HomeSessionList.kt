package com.l8r2gether.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.outlined.MovieCreation
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.l8r2gether.app.R
import com.l8r2gether.app.ui.theme.LtCanvas
import com.l8r2gether.app.ui.theme.LtContainerLow
import com.l8r2gether.app.ui.theme.LtControlSurface
import com.l8r2gether.app.ui.theme.LtInk
import com.l8r2gether.app.ui.theme.LtOnPrimary
import com.l8r2gether.app.ui.theme.LtOutline
import com.l8r2gether.app.ui.theme.LtPrimary
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
                    colors = listOf(Color(0xFFFFEDE8).copy(alpha = 0.55f), LtCanvas),
                    radius = 980f,
                ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            item {
                SelectNewMediaBar(onClick = onSelectNewMedia)
            }
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 22.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_pick_up_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = LtInk,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_pick_up_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF514641),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 420.dp),
                    )
                }
            }
            items(sessions, key = { it.contentKey }) { item ->
                SessionCard(
                    item = item,
                    onResume = { onResumeSession(item.contentKey) },
                    onAdjustTimestamp = { onSyncNow(item.contentKey) },
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .widthIn(max = 804.dp),
                )
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
        FloatingActionButton(
            onClick = onFavoritesClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 38.dp, bottom = 38.dp)
                .size(80.dp),
            shape = CircleShape,
            containerColor = Color(0xFFA97B7D),
            contentColor = LtOnPrimary,
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = stringResource(R.string.cd_favorites_fab),
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
private fun SelectNewMediaBar(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(top = 4.dp)
            .heightIn(min = 66.dp)
            .widthIn(min = 364.dp),
        shape = PillShape,
        color = LtControlSurface,
        border = BorderStroke(1.dp, LtOutline),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.MovieCreation,
                    contentDescription = null,
                    tint = LtPrimary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = stringResource(R.string.home_select_new_media),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp),
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 22.dp, end = 22.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.EventNote,
                    contentDescription = stringResource(R.string.cd_session_notes),
                    tint = LtPrimary.copy(alpha = 0.88f),
                    modifier = Modifier.size(22.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 24.dp, end = 72.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
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
                            fontWeight = FontWeight.SemiBold,
                            color = LtInk,
                        )
                        Text(
                            text = item.pauseSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = LtPrimary,
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
                                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.home_resume_session),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        } else {
                            Button(
                                onClick = onResume,
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LtContainerLow.copy(alpha = 0.72f),
                                    contentColor = LtPrimary,
                                ),
                                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.home_resume), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        OutlinedButton(
                            onClick = onAdjustTimestamp,
                            shape = PillShape,
                            border = BorderStroke(1.dp, LtOutline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C514D)),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text(
                                stringResource(R.string.home_adjust_timestamp),
                                fontWeight = FontWeight.SemiBold,
                            )
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
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF34333A), tint, Color(0xFF1F2227)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 28.dp, top = 10.dp)
                .size(width = 42.dp, height = 54.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.15f)),
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.82f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.cd_session_thumbnail, title),
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun thumbnailTintFor(contentKey: String): Color {
    val palette = listOf(
        Color(0xFF9B7C62),
        Color(0xFF578291),
        Color(0xFF7B6A92),
        Color(0xFF6E7F74),
    )
    return palette[kotlin.math.abs(contentKey.hashCode()) % palette.size]
}
