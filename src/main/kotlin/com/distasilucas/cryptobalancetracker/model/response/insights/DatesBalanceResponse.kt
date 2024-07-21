package com.distasilucas.cryptobalancetracker.model.response.insights

data class DatesBalanceResponse(
  val datesBalances: List<DateBalances>,
  val change: BalanceChanges,
  val priceDifference: DifferencesChanges
)

data class DateBalances(
  val date: String,
  val balances: BalancesResponse
)

data class BalanceChanges(
  val usdChange: Float,
  val eurChange: Float,
  val btcChange: Float,
)

data class DifferencesChanges(
  val usdDifference: String,
  val eurDifference: String,
  val btcDifference: String,
)
