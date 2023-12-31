package com.distasilucas.cryptobalancetracker.model.response.coingecko

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.math.BigDecimal

data class CoingeckoCryptoInfo(
    val id: String,
    val symbol: String,
    val name: String,
    val image: Image,

    @JsonProperty("market_data")
    val marketData: MarketData
): Serializable

data class Image(
    val large: String
): Serializable

data class MarketData(
    @JsonProperty("current_price")
    val currentPrice: CurrentPrice,

    @JsonProperty("circulating_supply")
    val circulatingSupply: BigDecimal,

    @JsonProperty("max_supply")
    val maxSupply: BigDecimal?
): Serializable

data class CurrentPrice(
    val usd: BigDecimal,
    val eur: BigDecimal,
    val btc: BigDecimal
): Serializable