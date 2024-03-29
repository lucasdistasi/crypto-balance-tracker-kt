package com.distasilucas.cryptobalancetracker.model.response.insights

data class UserCryptosInsights(
  val cryptoInfo: CryptoInfo,
  val quantity: String,
  val percentage: Float,
  val balances: BalancesResponse,
  val marketCapRank: Int,
  val marketData: MarketData,
  val platforms: List<String>
)
