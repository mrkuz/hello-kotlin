package net.bnb1.commons.http

/**
 * List of available HTTP status.
 */
enum class HttpStatus(val code: Int, val message: String) {

    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error")
}
