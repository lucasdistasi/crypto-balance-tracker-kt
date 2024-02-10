package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.util.UUID

@Document("Goals")
data class Goal(
  @Id
  val id: String = UUID.randomUUID().toString(),

  @Field("crypto_id")
  val coingeckoCryptoId: String,

  @Field("goal_quantity")
  val goalQuantity: BigDecimal
) {

  fun toGoalResponse(
    id: String,
    cryptoName: String,
    actualQuantity: BigDecimal,
    progress: Float,
    remainingQuantity: BigDecimal,
    moneyNeeded: BigDecimal
  ): GoalResponse {
    return GoalResponse(
      id = id,
      cryptoName = cryptoName,
      actualQuantity = actualQuantity.toPlainString(),
      progress = progress,
      remainingQuantity = remainingQuantity.toPlainString(),
      goalQuantity = goalQuantity.toPlainString(),
      moneyNeeded = moneyNeeded.toPlainString()
    )
  }
}
