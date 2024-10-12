package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable

data class BalancesResponse(
  val totalUSDBalance: String,
  val totalEURBalance: String,
  val totalBTCBalance: String
) : Serializable
