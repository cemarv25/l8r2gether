package com.l8r2gether.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.l8r2gether.app.R
import com.l8r2gether.app.ui.theme.LtOnBackground
import com.l8r2gether.app.ui.theme.LtPrimary
import com.l8r2gether.app.ui.theme.LtRailSelected

@Composable
fun LtTopBar(
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 40.dp, end = 34.dp, top = 14.dp, bottom = 16.dp),
    ) {
        val showTopNavigation = maxWidth >= 900.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = if (showTopNavigation) {
                    MaterialTheme.typography.displaySmall
                } else {
                    MaterialTheme.typography.headlineLarge
                },
                color = LtOnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showTopNavigation) {
                    TextButton(onClick = {}) {
                        Text(
                            stringResource(R.string.nav_cinema),
                            fontWeight = FontWeight.SemiBold,
                            color = LtOnBackground,
                        )
                    }
                    TextButton(onClick = onComingSoon) {
                        Text(stringResource(R.string.nav_moments), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onComingSoon) {
                        Text(stringResource(R.string.nav_lounge), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(18.dp))
                    VerticalDivider(modifier = Modifier.height(40.dp), color = LtRailSelected)
                    Spacer(Modifier.width(16.dp))
                }
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.nav_favorites),
                        tint = LtPrimary,
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = LtPrimary,
                    )
                }
                AvatarPlaceholder()
            }
        }
    }
}

@Composable
private fun AvatarPlaceholder() {
    Box(
        modifier = Modifier
            .padding(start = 2.dp)
            .size(42.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFE3B09A), Color(0xFF8D554B), Color(0xFF2F2930)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.White.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = stringResource(R.string.cd_profile_avatar),
                tint = Color.White.copy(alpha = 0.88f),
            )
        }
    }
}
