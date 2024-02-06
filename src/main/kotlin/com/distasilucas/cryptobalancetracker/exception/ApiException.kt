package com.distasilucas.cryptobalancetracker.exception

import com.distasilucas.cryptobalancetracker.constants.UNKNOWN_ERROR
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

class ApiException(
    val httpStatusCode: HttpStatusCode,
    override val message: String
) : RuntimeException(message) {

    constructor() : this(UNKNOWN_ERROR)

    constructor(message: String) : this(HttpStatus.INTERNAL_SERVER_ERROR, message)

}