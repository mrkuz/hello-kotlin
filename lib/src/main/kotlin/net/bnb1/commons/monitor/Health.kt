package net.bnb1.commons.monitor

import kotlinx.serialization.Serializable

/**
 * Used to represent health status of a component.
 */
@Serializable
data class Health(val status: HealthStatus)
