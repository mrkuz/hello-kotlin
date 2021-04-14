package net.bnb1.commons.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import net.bnb1.commons.test.Person
import java.io.FileNotFoundException

class ResourcesTests : FunSpec({

    context("Resources.loadYaml") {

        test("Load and deserialize person") {
            val person = Resources.loadYaml<Root>("/person.yaml").person
            with(person) {
                firstName shouldBe "First"
                middleName shouldBe null
                lastName shouldBe "Last"
                age shouldBe 33
                weight shouldBe 70.4
                married shouldBe true
            }
        }

        test("File not found") {
            shouldThrow<FileNotFoundException> {
                Resources.loadYaml<Root>("/does-not-exist.yaml")
            }
        }
    }

    context("Resources.loadConf") {

        test("Load and deserialize person") {
            val person = Resources.loadConf<Root>("/person.conf").person
            with(person) {
                firstName shouldBe "First"
                middleName shouldBe null
                lastName shouldBe "Last"
                age shouldBe 33
                weight shouldBe 70.4
                married shouldBe true
            }
        }

        test("File not found") {
            shouldThrow<FileNotFoundException> {
                Resources.loadConf<Root>("/does-not-exist.conf")
            }
        }
    }

    context("Resources.loadProperties") {

        test("Load and deserialize person") {
            val person = Resources.loadProperties<Root>("/person.properties").person
            with(person) {
                firstName shouldBe "First"
                middleName shouldBe null
                lastName shouldBe "Last"
                age shouldBe 33
                weight shouldBe 70.4
                married shouldBe true
            }
        }

        test("File not found") {
            shouldThrow<FileNotFoundException> {
                Resources.loadProperties<Root>("/does-not-exist.properties")
            }
        }
    }
})

@Serializable
data class Root(val person: Person)