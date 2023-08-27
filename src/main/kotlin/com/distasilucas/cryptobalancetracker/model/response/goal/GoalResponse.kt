package com.distasilucas.cryptobalancetracker.model.response.goal

import java.math.BigDecimal

data class GoalResponse(
    val id: String,
    val cryptoName: String,
    val actualQuantity: BigDecimal,
    val progress: BigDecimal,
    val remainingQuantity: BigDecimal,
    val goalQuantity: BigDecimal,
    val moneyNeeded: BigDecimal
)