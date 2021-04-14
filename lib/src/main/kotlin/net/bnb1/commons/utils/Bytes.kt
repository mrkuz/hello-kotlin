package net.bnb1.commons.utils

import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.abs

/**
 * Provides utility functions for working with bytes.
 */
@Suppress("SpellCheckingInspection")
object Bytes {

    /**
     * Pretty prints [input] bytes in human-readable format (base 2, 1024).
     */
    fun <T : Number> prettyPrint(input: T): String {
        val bytes = input.toLong()
        val absoluteBytes = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else abs(bytes)
        if (absoluteBytes < 1024) {
            return "$bytes B"
        }
        var value = absoluteBytes
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && absoluteBytes > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= java.lang.Long.signum(bytes).toLong()
        return String.format("%.1f %ciB", value / 1024.0, ci.current())
    }
}
