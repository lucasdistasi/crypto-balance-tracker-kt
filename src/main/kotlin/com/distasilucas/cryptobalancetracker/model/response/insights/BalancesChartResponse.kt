package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable
import java.math.BigDecimal

data class BalancesChartResponse(
  val name: String,
  val balance: String,
  val percentage: Float,
): Serializable {

  constructor(name: String, balance: BigDecimal, percentage: Float): this(name, balance.toPlainString(), percentage)
}
