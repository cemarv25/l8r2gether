package com.latertogether.domain.model

enum class ClockMode {
    /** Preferred for elapsed math when available. */
    Monotonic,
    /** Fallback when monotonic clock is unavailable. */
    Wall,
}
