package com.distasilucas.cryptobalancetracker.model.response.insights.platform

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import java.io.Serializable

data class PlatformsBalancesInsightsResponse(
  val balances: BalancesResponse,
  val platforms: List<PlatformsInsights>
) : Serializable

data class PlatformsInsights(
  val platformName: String,
  val balances: BalancesResponse,
  val percentage: Float
) : Serializable

