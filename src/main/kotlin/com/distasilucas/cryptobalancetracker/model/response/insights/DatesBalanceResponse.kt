package com.distasilucas.cryptobalancetracker.model.response.insights

data class DatesBalanceResponse(
  val datesBalances: List<DatesBalances>,
  val change: Float,
  val priceDifference: String
)

data class DatesBalances(
  val date: String,
  val balance: String
)
