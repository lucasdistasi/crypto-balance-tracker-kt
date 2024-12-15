package com.distasilucas.cryptobalancetracker.model.response.goal

import java.io.Serializable

data class GoalResponse(
  val id: String,
  val cryptoName: String,
  val actualQuantity: String,
  val progress: Float,
  val remainingQuantity: String,
  val goalQuantity: String,
  val moneyNeeded: String
) : Serializable
