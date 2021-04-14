package net.bnb1.commons.utils

import com.charleskorn.kaml.Yaml
import com.typesafe.config.ConfigFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import java.io.FileNotFoundException
import java.net.URL

/**
 * Provides access to classpath resources.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object Resources {

    /**
     * Loads and deserialize YAML file from classpath.
     */
    inline fun <reified T> loadYaml(classpathResource: String): T {
        val yaml = getResource(classpathResource).readText()
        return Yaml.default.decodeFromString(yaml)
    }

    /**
     * Loads and deserialize HOCON file from classpath.
     */
    inline fun <reified T> loadConf(classpathResource: String): T {
        getResource(classpathResource)
        val resourceBasename = classpathResource
            .replace(Regex("^/"), "")
            .replace(Regex("\\.conf$"), "")
        val conf = ConfigFactory.load(Resources::class.java.classLoader, resourceBasename)
        return Hocon.decodeFromConfig(conf)
    }

    /**
     * Load and deserialize properties file from classpath.
     */
    inline fun <reified T> loadProperties(classpathResource: String): T {
        val map = mutableMapOf<String, String>()
        getResource(classpathResource).openStream().use {
            val properties = java.util.Properties().apply { load(it) }
            properties.forEach { k, v -> map[k.toString()] = v.toString() }
        }
        return Properties.decodeFromStringMap(map)
    }

    /**
     * Returns URL to classpath resource.
     *
     * @throws FileNotFoundException if resource is not found
     */
    fun getResource(classpathResource: String): URL {
        return Resources::class.java.getResource(classpathResource)
            ?: throw FileNotFoundException("$classpathResource not found")
    }
}