package com.distasilucas.cryptobalancetracker.model.response.insights.crypto

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import java.io.Serializable

data class CryptoInsightResponse(
  val cryptoName: String?,
  val balances: BalancesResponse,
  val platforms: List<PlatformInsight>
) : Serializable

data class PlatformInsight(
  val quantity: String,
  val balances: BalancesResponse,
  val percentage: Float,
  val platformName: String
) : Serializable
