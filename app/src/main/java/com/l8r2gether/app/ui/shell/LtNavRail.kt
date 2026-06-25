package com.l8r2gether.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.l8r2gether.app.R
import com.l8r2gether.app.ui.theme.LtOnBackground
import com.l8r2gether.app.ui.theme.LtPrimary
import com.l8r2gether.app.ui.theme.LtRailSurface
import com.l8r2gether.app.ui.theme.LtRailSelected

@Composable
fun LtNavRail(
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(96.dp)
            .fillMaxHeight()
            .background(LtRailSurface)
            .padding(vertical = 22.dp)
            .semantics { contentDescription = "Navigation" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Us",
            style = MaterialTheme.typography.titleLarge,
            color = LtPrimary,
            modifier = Modifier.padding(bottom = 18.dp),
        )
        RailItem(
            icon = { Icon(Icons.Outlined.MovieCreation, contentDescription = stringResource(R.string.nav_cinema)) },
            label = stringResource(R.string.nav_cinema),
            selected = true,
            onClick = {},
        )
        RailItem(
            icon = { Icon(Icons.Outlined.PhotoLibrary, contentDescription = stringResource(R.string.nav_moments)) },
            label = stringResource(R.string.nav_moments),
            selected = false,
            onClick = onComingSoon,
        )
        RailItem(
            icon = { Icon(Icons.Outlined.GridView, contentDescription = stringResource(R.string.nav_lounge)) },
            label = stringResource(R.string.nav_lounge),
            selected = false,
            onClick = onComingSoon,
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {},
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LtPrimary),
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = stringResource(R.string.nav_favorites),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        RailItem(
            icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = stringResource(R.string.nav_help)) },
            label = stringResource(R.string.nav_help),
            selected = false,
            onClick = onComingSoon,
        )
    }
}

@Composable
private fun RailItem(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) LtRailSelected else MaterialTheme.colorScheme.surface
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                icon()
            }
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = LtOnBackground)
        }
    }
}
