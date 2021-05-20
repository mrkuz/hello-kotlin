package net.bnb1.commons.utils

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Returns duration of this number in milliseconds
 */
@OptIn(ExperimentalTime::class)
val Number.milliseconds
    get() = Duration.milliseconds(this.toLong())
