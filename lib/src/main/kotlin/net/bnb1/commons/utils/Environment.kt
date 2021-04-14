package net.bnb1.commons.utils

/**
 * Provides access to the environment.
 */
object Environment {

    /**
     * Returns the currently active profile. If no profile is set, "default" is returned.
     *
     * Environment variable: BNB1_PROFILE
     */
    fun getActiveProfile(): String = get("BNB1_PROFILE") ?: "default"

    /**
     * Returns the current working directory of the process (system property 'user.dir').
     * Can be overridden with environment variable.
     *
     * Environment variable: BNB1_WORKDIR
     */
    fun getCurrentWorkingDirectory(): String = get("BNB1_WORKDIR") ?: System.getProperty("user.dir")

    internal fun get(name: String) = System.getenv(name)
}