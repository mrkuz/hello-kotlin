package net.bnb1.commons.properties

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Used to provide build-related information.
 *
 * - [project.name]: Project group
 * - [project.group]: Project name
 * - [project.version]: Version number
 * - [build.timestamp]: Build timestamp
 * - [git.commitId]: Git commit id
 */
@Serializable
data class BuildProperties(val project: Project, val build: Build, val git: Git) {

    @Serializable
    data class Project(val name: String, val group: String, val version: String)

    @Serializable
    data class Build(val timestamp: Long)

    @Serializable
    data class Git(@SerialName("commit-id") val commitId: String)
}
