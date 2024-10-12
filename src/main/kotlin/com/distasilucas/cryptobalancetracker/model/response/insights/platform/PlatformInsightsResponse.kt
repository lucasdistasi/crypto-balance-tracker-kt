package com.distasilucas.cryptobalancetracker.model.response.insights.platform

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights
import java.io.Serializable

data class PlatformInsightsResponse(
  val platformName: String?,
  val balances: BalancesResponse,
  val cryptos: List<CryptoInsights>,
) : Serializable
