package com.distasilucas.cryptobalancetracker.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.time.LocalDateTime

@Document("Cryptos")
data class Crypto(
    @Id
    val id: String,
    val name: String,
    val ticker: String,

    @Field("last_known_price")
    val lastKnownPrice: BigDecimal,

    @Field("last_known_price_in_eur")
    val lastKnownPriceInEUR: BigDecimal,

    @Field("last_known_price_in_btc")
    val lastKnownPriceInBTC: BigDecimal,

    @Field("circulating_supply")
    val circulatingSupply: BigDecimal,

    @Field("max_supply")
    val maxSupply: BigDecimal,

    @Field("last_updated_at")
    val lastUpdatedAt: LocalDateTime
)
