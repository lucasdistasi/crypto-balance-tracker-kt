package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable

data class Balances(
  val fiat: FiatBalance,
  val btc: String
): Serializable {
  companion object {
    val EMPTY = Balances(FiatBalance.EMPTY, "0")
  }

  fun usd() = fiat.usd

  fun eur() = fiat.eur
}

data class FiatBalance(
  val usd: String,
  val eur: String,
): Serializable {
  companion object {
    val EMPTY = FiatBalance("0", "0")
  }
}
