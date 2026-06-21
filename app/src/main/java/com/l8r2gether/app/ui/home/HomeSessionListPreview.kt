package com.l8r2gether.app.ui.home

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.l8r2gether.app.ui.theme.LtTheme

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun HomeSessionListPreview() {
    LtTheme {
        Surface {
            HomeSessionList(
                sessions = HomeSampleSessions.items,
                onSelectNewMedia = {},
                onResumeSession = {},
                onSyncNow = {},
                onFavoritesClick = {},
            )
        }
    }
}
