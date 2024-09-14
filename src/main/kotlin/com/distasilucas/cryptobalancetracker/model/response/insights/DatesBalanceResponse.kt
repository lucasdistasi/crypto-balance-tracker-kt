package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable

data class DatesBalanceResponse(
  val datesBalances: List<DateBalances>,
  val change: BalanceChanges,
  val priceDifference: DifferencesChanges
): Serializable

data class DateBalances(
  val date: String,
  val balances: BalancesResponse
): Serializable

data class BalanceChanges(
  val usdChange: Float,
  val eurChange: Float,
  val btcChange: Float,
): Serializable

data class DifferencesChanges(
  val usdDifference: String,
  val eurDifference: String,
  val btcDifference: String,
): Serializable
