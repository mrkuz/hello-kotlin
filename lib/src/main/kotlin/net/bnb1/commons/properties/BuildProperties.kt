package net.bnb1.commons.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Used to provide build-related information.
 *
 * - [version]: Version number
 * - [timestamp]: Build timestamp
 * - [git.commitId]: Git commit id
 */
@Serializable
data class BuildProperties(val version: String, val timestamp: Long, val git: Git) {

    @Serializable
    data class Git(@SerialName("commit-id") val commitId: String)
}