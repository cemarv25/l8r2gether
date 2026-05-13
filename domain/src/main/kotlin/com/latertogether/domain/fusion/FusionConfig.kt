package com.latertogether.domain.fusion

data class FusionConfig(
    /** Within this delta (seconds), snap anchor to observed position. */
    val snapThresholdSec: Double = 2.0,
    /** Above this delta, treat as seek/jump — downgrade confidence until checkpoint. */
    val seekSuspectedThresholdSec: Double = 12.0,
)
