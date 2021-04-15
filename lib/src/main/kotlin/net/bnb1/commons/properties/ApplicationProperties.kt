package net.bnb1.commons.properties

import kotlinx.serialization.Serializable

/**
 * Used to represent application configuration properties.
 *
 * - [application.name]: Name of the application
 */
@Serializable
data class ApplicationProperties(val application: Application) {

    @Serializable
    data class Application(val name: String)
}
