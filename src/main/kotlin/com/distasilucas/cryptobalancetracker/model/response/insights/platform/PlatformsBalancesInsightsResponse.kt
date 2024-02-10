package com.distasilucas.cryptobalancetracker.model.response.insights.platform

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse

data class PlatformsBalancesInsightsResponse(
  val balances: BalancesResponse,
  val platforms: List<PlatformsInsights>
)

data class PlatformsInsights(
  val platformName: String,
  val balances: BalancesResponse,
  val percentage: Float
)

