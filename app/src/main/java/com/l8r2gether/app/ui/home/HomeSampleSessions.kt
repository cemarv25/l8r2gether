package com.l8r2gether.app.ui.home

import com.l8r2gether.app.viewmodel.SessionListItemUi

/** Placeholder sessions for debug builds and Compose previews (matches home mockup). */
object HomeSampleSessions {
    val items: List<SessionListItemUi> = listOf(
        SessionListItemUi(
            contentKey = "sample:grand-budapest-hotel",
            displayTitle = "The Grand Budapest Hotel",
            pauseSubtitle = "Paused at 47:31",
            isMostRecent = true,
        ),
        SessionListItemUi(
            contentKey = "sample:past-lives",
            displayTitle = "Past Lives",
            pauseSubtitle = "Paused at 39:18",
            isMostRecent = false,
        ),
    )
}
