package net.bnb1.commons.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.bnb1.commons.test.Person

class JsonTests : FunSpec({

    context("Json.toJson") {

        test("Serialize null receiver") {
            val person: Person? = null
            person.toJson() shouldBe "null"
        }

        test("Serialize simple data class") {
            val person = Person("First", null, "Last", 33, 70.4, true)
            person.toJson() shouldBe """
                {
                    "firstName":"First",
                    "middleName":null,
                    "lastName":"Last",
                    "age":33,
                    "weight":70.4,
                    "married":true
                }
                """
                .filterNot { it == ' ' }
                .filterNot { it == '\n' }
        }
    }
});

