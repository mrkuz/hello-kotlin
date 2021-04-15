package net.bnb1.commons.http

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.ByteBuffer

class HttpParserTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    var parser = HttpParser()

    fun request(request: String): ByteBuffer = ByteBuffer.wrap(
        request.trimIndent()
            .replace("\n", "\r\n")
            .replace("<END>", "\r\n")
            .toByteArray()
    )

    context("Broken requests") {

        test("Unsupported HTTP method") {
            val request = request(
                """
                HELLO /test HTTP/1.1
                <END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            parser.result.valid shouldBe false
        }

        test("Missing HTTP version") {
            val request = request(
                """
                GET /test
                <END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            parser.result.valid shouldBe false
        }

        test("Not a HTTP request at all") {
            val request = request(
                """
                Hello
                <END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            parser.result.valid shouldBe false
        }

        test("Missing line break") {
            val request = request("""GET /test HTTP/1.1""")
            parser.read(request)

            parser.done shouldBe false
            parser.result.valid shouldBe false
        }

        test("Missing content length header") {
            val request = request(
                """
                GET /test HTTP/1.1

                Hello<END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            parser.result.valid shouldBe true
        }

        test("Missing content length header (2)") {
            val request = request(
                """
                GET /test HTTP/1.1
                User-Agent: Test

                Hello<END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            parser.result.valid shouldBe true
        }

        test("Content length too small") {
            val request = request(
                """
                GET /test HTTP/1.1
                Content-Length: 2

                Hello<END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            with(parser.result) {
                valid shouldBe true
                body shouldBe "He".toByteArray()
            }
        }

        test("Content length too big") {
            val request = request(
                """
                GET /test HTTP/1.1
                Content-Length: 10

                Hello<END>
                """
            )
            parser.read(request)

            parser.done shouldBe false
            parser.result.valid shouldBe true
        }

        test("Invalid header line") {
            val request = request(
                """
                GET /test HTTP/1.1
                ABC
                <END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            parser.result.valid shouldBe false
        }
    }

    context("GET method") {

        test("Parse request without body") {
            val request = request(
                """
                GET /test HTTP/1.1
                User-Agent: Test
                <END>
                """
            )

            parser.read(request)

            parser.done shouldBe true
            with(parser.result) {
                valid shouldBe true
                method shouldBe HttpMethod.GET
                path shouldBe "/test"
                body.size shouldBe 0
            }
        }

        test("Parse request with body") {
            val request = request(
                """
                GET /test HTTP/1.1
                User-Agent: Test
                Content-Length: 5

                Hello<END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            with(parser.result) {
                valid shouldBe true
                method shouldBe HttpMethod.GET
                path shouldBe "/test"
                body shouldBe "Hello".toByteArray()
            }
        }
    }

    context("POST method") {

        test("Parse request without body") {
            val request = request(
                """
                POST /test HTTP/1.1
                User-Agent: Test
                <END>
                """
            )

            parser.read(request)

            parser.done shouldBe true
            with(parser.result) {
                valid shouldBe true
                method shouldBe HttpMethod.POST
                path shouldBe "/test"
                body.size shouldBe 0
            }
        }

        test("Parse request with body") {
            val request = request(
                """
                POST /test HTTP/1.1
                User-Agent: Test
                Content-Length: 5

                Hello<END>
                """
            )
            parser.read(request)

            parser.done shouldBe true
            with(parser.result) {
                valid shouldBe true
                method shouldBe HttpMethod.POST
                path shouldBe "/test"
                body shouldBe "Hello".toByteArray()
            }
        }
    }

    test("Parse request with payload bigger than buffer size") {
        val request = request(
            """
                POST /test HTTP/1.1
                User-Agent: Test
                Content-Length: 5

                Hello<END>
                """
        )
        parser = HttpParser(4)
        parser.read(request)

        parser.done shouldBe true
        with(parser.result) {
            valid shouldBe true
            method shouldBe HttpMethod.POST
            path shouldBe "/test"
            body shouldBe "Hello".toByteArray()
        }
    }

    test("Parse fragmented request") {
        parser.read("GET /test ".toByteArray())
        parser.done shouldBe false
        parser.read("HTTP/1.1\r\n".toByteArray())
        parser.done shouldBe false
        parser.read("\r\n".toByteArray())

        parser.done shouldBe true
        with(parser.result) {
            valid shouldBe true
            method shouldBe HttpMethod.GET
            path shouldBe "/test"
        }
    }
})
