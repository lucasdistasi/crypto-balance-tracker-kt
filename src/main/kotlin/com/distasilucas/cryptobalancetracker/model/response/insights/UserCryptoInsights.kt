package com.distasilucas.cryptobalancetracker.model.response.insights

data class UserCryptoInsights(
  val cryptoInfo: CryptoInfo,
  val quantity: String,
  val percentage: Float,
  val balances: BalancesResponse,
)
