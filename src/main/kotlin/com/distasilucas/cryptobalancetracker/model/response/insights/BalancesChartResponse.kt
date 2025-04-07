package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable

data class BalancesChartResponse(
  val name: String,
  val balance: String,
  val percentage: Float,
): Serializable
