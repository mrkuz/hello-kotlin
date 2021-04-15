package net.bnb1.commons.test

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val age: Int,
    val weight: Double,
    val married: Boolean
)
