package net.bnb1.commons.utils

import java.nio.charset.Charset

/**
 * Provides UTF-8 related utility functions.
 */
object UTF8 {

    /**
     * Returns the UTF-8 [charset][Charset].
     */
    fun charset(): Charset = Charset.forName("UTF-8")
}