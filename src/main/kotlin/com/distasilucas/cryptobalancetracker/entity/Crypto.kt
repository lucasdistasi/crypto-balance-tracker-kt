package com.distasilucas.cryptobalancetracker.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDateTime

@Document("Cryptos")
data class Crypto(
    @Id
    val id: String,
    val name: String,
    val ticker: String,
    val lastKnownPrice: BigDecimal,
    val lastKnownPriceInEUR: BigDecimal,
    val lastKnownPriceInBTC: BigDecimal,
    val circulatingSupply: BigDecimal,
    val maxSupply: BigDecimal,
    val lastUpdatedAt: LocalDateTime
)
