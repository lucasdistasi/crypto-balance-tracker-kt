package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable

data class HomeInsightsResponse(
  val balances: Balances,
  val stablecoins: String,
  val top24hGainer: CryptoInfo,
): Serializable
