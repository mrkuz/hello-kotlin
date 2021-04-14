package net.bnb1.commons.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk

class EnvironmentTests : FunSpec({

    context("Environment.getActiveProfile") {

        test("Return default profile") {
            Environment.getActiveProfile() shouldBe "default"
        }

        test("Return profile from environment variable") {
            val mock = spyk(Environment)
            every { mock.get("BNB1_PROFILE") } returns "test"
            mock.getActiveProfile() shouldBe "test"
        }
    }

    context("Environment.getCurrentWorkingDirectory") {

        test("Return default working directory") {
            Environment.getCurrentWorkingDirectory() shouldBe System.getProperty("user.dir")
        }

        test("Return working directory from environment variable") {
            val mock = spyk(Environment)
            every { mock.get("BNB1_WORKDIR") } returns "/tmp"
            mock.getCurrentWorkingDirectory() shouldBe "/tmp"
        }
    }
})