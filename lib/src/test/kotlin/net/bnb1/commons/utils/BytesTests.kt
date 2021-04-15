package net.bnb1.commons.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.math.pow

class BytesTests : FunSpec({

    context("Bytes.prettyPrint") {

        test("Print B") {
            Bytes.prettyPrint(1000) shouldBe "1000 B"
        }

        test("Print KiB") {
            Bytes.prettyPrint(1024) shouldBe "1.0 KiB"
        }

        test("Print MiB") {
            Bytes.prettyPrint(1024.0.pow(2.0)) shouldBe "1.0 MiB"
        }

        test("Print GiB") {
            Bytes.prettyPrint(1024.0.pow(3.0)) shouldBe "1.0 GiB"
        }

        test("Print TiB") {
            Bytes.prettyPrint(1024.0.pow(4.0)) shouldBe "1.0 TiB"
        }

        test("Print PiB") {
            Bytes.prettyPrint(1024.0.pow(5.0)) shouldBe "1.0 PiB"
        }

        test("Print EiB") {
            Bytes.prettyPrint(1024.0.pow(6.0)) shouldBe "1.0 EiB"
        }
    }
})
