package net.bnb1.commons.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Extension function to serialize the receiver object to JSON.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
inline fun <reified T : Any?> T.toJson(): String = Json { encodeDefaults = true }.encodeToString(this)
