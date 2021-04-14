package net.bnb1.gradle

import java.nio.file.Path
import java.util.*

object ProjectProperties {

    private val properties = Properties()

    fun load(rootDir: String) {
        // Use a file called project.properties to configure the build
        val path = Path.of(rootDir, "project.properties")
        val file = path.toFile()
        if (!file.exists()) {
            throw IllegalArgumentException("File '${file.absolutePath}' not found")
        }
        file.reader().use { properties.load(it) }
    }

    operator fun get(name: String): String {
        if (!properties.containsKey(name)) {
            throw IllegalArgumentException("Property '$name' not found")
        }
        return properties[name].toString()
    }
}

fun projectProperty(name: String): String = ProjectProperties[name]