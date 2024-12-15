package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable

// TODO - add sats balance?

data class TotalBalancesResponse(
  val totalUSDBalance: String,
  val totalEURBalance: String,
  val totalBTCBalance: String,
  val stableCoinsBalance: String,
  val totalNonBtcBalance: String,
): Serializable
