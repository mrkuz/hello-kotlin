package net.bnb1.commons.utils

/**
 * Returns identity hash code of an object.
 */
fun Any?.identityHashCode(): Int = this?.let { System.identityHashCode(this) } ?: 0

/**
 * Returns the reference ID of an object.
 */
@Suppress("MagicNumber")
fun Any?.identity(): String? {
    if (this == null) {
        return null
    }
    return this::class.java.name + "@" + this.identityHashCode().toString(16)
}
