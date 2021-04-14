package net.bnb1.commons.http

/**
 * List of available HTTP headers.
 */
enum class HttpHeader(val headerName: String) {

    CONNECTION("Connection"),
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-Length")
}