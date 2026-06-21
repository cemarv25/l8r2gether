package com.l8r2gether.app.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.l8r2gether.app.R
import com.l8r2gether.app.ui.theme.LtOnBackground

@Composable
fun LtTopBar(
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = LtOnBackground,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {}) {
                Text(
                    stringResource(R.string.nav_cinema),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            TextButton(onClick = onComingSoon) {
                Text(stringResource(R.string.nav_moments))
            }
            TextButton(onClick = onComingSoon) {
                Text(stringResource(R.string.nav_lounge))
            }
            Spacer(Modifier.width(8.dp))
            VerticalDivider(modifier = Modifier.height(24.dp))
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {}) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = stringResource(R.string.nav_favorites))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
            }
        }
    }
}
