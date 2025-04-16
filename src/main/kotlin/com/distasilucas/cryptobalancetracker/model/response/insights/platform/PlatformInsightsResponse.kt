package com.distasilucas.cryptobalancetracker.model.response.insights.platform

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import java.io.Serializable

data class PlatformInsightsResponse(
  val platformName: String,
  val balances: BalancesResponse,
  val cryptos: List<CryptoInsights>,
): Serializable

data class CryptoInsights(
  val id: String,
  val cryptoInfo: CryptoInfo,
  val quantity: String,
  val percentage: Float,
  val balances: BalancesResponse,
): Serializable
