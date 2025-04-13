package com.distasilucas.cryptobalancetracker.model.response.goal

import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import java.io.Serializable

data class GoalResponse(
  val id: String,
  val cryptoInfo: CryptoInfo,
  val actualQuantity: String,
  val progress: Float,
  val remainingQuantity: String,
  val goalQuantity: String,
  val moneyNeeded: String
): Serializable
