package com.distasilucas.cryptobalancetracker.model.response.insights

import java.math.BigDecimal

data class MarketData(
    val circulatingSupply: String,
    val maxSupply: String,
    val currentPrice: CurrentPrice,
    val marketCap: String,
    val priceChange: PriceChange
)

data class CurrentPrice(
    val usd: String,
    val eur: String,
    val btc: String
)

data class PriceChange(
    val changePercentageIn24h: BigDecimal,
    val changePercentageIn7d: BigDecimal,
    val changePercentageIn30d: BigDecimal
)