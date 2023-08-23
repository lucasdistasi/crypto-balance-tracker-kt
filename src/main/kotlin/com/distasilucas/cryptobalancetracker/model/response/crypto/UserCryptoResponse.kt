package com.distasilucas.cryptobalancetracker.model.response.crypto

import java.math.BigDecimal

data class UserCryptoResponse(
    val id: String,
    val cryptoName: String,
    val quantity: BigDecimal,
    val platform: String
)