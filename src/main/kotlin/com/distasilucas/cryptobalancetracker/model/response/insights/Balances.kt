package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

data class Balances(
  val fiat: FiatBalance,
  val btc: String
): Serializable {
  companion object {
    val EMPTY = Balances(FiatBalance.EMPTY, "0")
  }

  fun usd() = fiat.usd

  fun eur() = fiat.eur

  constructor(fiatBalance: FiatBalance, btcBalance: BigDecimal): this(
    fiatBalance, btcBalance.setScale(8, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
  )
}

data class FiatBalance(
  val usd: String,
  val eur: String,
): Serializable {
  companion object {
    val EMPTY = FiatBalance("0", "0")
  }

  constructor(usd: BigDecimal, eur: BigDecimal): this(
    usd.setScale(2, RoundingMode.HALF_UP).toPlainString(),
    eur.setScale(2, RoundingMode.HALF_UP).toPlainString()
  )
}
