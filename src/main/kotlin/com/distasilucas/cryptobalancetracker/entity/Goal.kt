package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
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
    cryptoInfo: CryptoInfo,
    actualQuantity: BigDecimal,
    progress: Float,
    remainingQuantity: BigDecimal,
    moneyNeeded: BigDecimal
  ) = GoalResponse(
    id,
    cryptoInfo,
    actualQuantity.toPlainString(),
    progress,
    remainingQuantity.toPlainString(),
    goalQuantity.toPlainString(),
    moneyNeeded.toPlainString()
  )
}
