package com.distasilucas.cryptobalancetracker.model.response.coingecko

import java.io.Serializable

data class CoingeckoCrypto(
    val id: String,
    val symbol: String,
    val name: String
): Serializable