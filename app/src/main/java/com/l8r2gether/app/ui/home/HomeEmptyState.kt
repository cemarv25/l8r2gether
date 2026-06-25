package com.l8r2gether.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.l8r2gether.app.ui.theme.LtAccent
import com.l8r2gether.app.ui.theme.LtCanvas
import com.l8r2gether.app.ui.theme.LtControlSurface
import com.l8r2gether.app.ui.theme.LtMuted
import com.l8r2gether.app.ui.theme.LtOnBackground
import com.l8r2gether.app.ui.theme.LtPrimary

@Composable
fun HomeEmptyState(
    onStartNewSession: () -> Unit,
    onBrowseLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFFE9E4).copy(alpha = 0.72f), LtCanvas),
                    radius = 760f,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.72f))
            EmptyHeroTile()
            Spacer(modifier = Modifier.height(84.dp))
            Text(
                text = stringResource(R.string.home_headline),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = LtOnBackground,
                modifier = Modifier.widthIn(max = 540.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.home_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 500.dp),
            )
            Spacer(modifier = Modifier.height(44.dp))
            Button(
                onClick = onStartNewSession,
                modifier = Modifier
                    .heightIn(min = 56.dp)
                    .widthIn(min = 244.dp),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LtPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(Icons.Outlined.PlayCircle, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    stringResource(R.string.home_start_session),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onBrowseLibrary) {
                Text(
                    stringResource(R.string.home_browse_library),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(72.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeatureChip(Icons.Default.Weekend, stringResource(R.string.feature_cozy_setup))
                FeatureChip(Icons.Default.Schedule, stringResource(R.string.feature_any_time))
                FeatureChip(Icons.Default.ChatBubbleOutline, stringResource(R.string.feature_live_reaction))
            }
            Spacer(modifier = Modifier.height(52.dp))
        }
    }
}

@Composable
private fun EmptyHeroTile() {
    Box(
        modifier = Modifier.size(260.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(252.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0f)),
                        radius = 260f,
                    ),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(196.dp)
                .clip(RoundedCornerShape(58.dp))
                .background(Color.White.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = stringResource(R.string.cd_clapperboard),
                    modifier = Modifier.size(70.dp),
                    tint = LtPrimary,
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = LtAccent.copy(alpha = 0.75f),
                    )
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = LtAccent.copy(alpha = 0.82f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(LtControlSurface.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = LtMuted, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = LtMuted)
    }
}
