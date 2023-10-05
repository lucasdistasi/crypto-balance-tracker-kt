package com.distasilucas.cryptobalancetracker.model.response.crypto

import java.math.BigDecimal

data class UserCryptoResponse(
    val id: String,
    val cryptoName: String,
    val quantity: BigDecimal, // TODO - should be string
    val platform: String
)