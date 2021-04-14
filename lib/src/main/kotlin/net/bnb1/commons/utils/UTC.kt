package net.bnb1.commons.utils

import java.time.ZoneId

/**
 * Provides UTC related utility functions.
 */
object UTC {

    /**
     * Returns the UTC [zone id][ZoneId].
     */
    fun zone(): ZoneId = ZoneId.of("UTC")
}