package com.distasilucas.cryptobalancetracker.exception

import com.distasilucas.cryptobalancetracker.constants.REQUEST_LIMIT_REACHED

class TooManyRequestsException(message: String) : RuntimeException(message) {

    constructor() : this(REQUEST_LIMIT_REACHED)
}