package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable
import java.math.BigDecimal

data class TransactionsInfo(
  val averageBuyPrice: BigDecimal,
  //val totalBought: BigDecimal,
  //val totalSold: BigDecimal,
) : Serializable
