package com.distasilucas.cryptobalancetracker.model.response.insights

data class MarketData(
    val circulatingSupply: String,
    val maxSupply: String,
    val currentPrice: CurrentPrice
)

data class CurrentPrice(
    val usd: String,
    val eur: String,
    val btc: String
)