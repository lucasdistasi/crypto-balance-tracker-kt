package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Document("PriceTargets")
data class PriceTarget(
  @Id
  val id: String = UUID.randomUUID().toString(),

  @Field("crypto_id")
  val coingeckoCryptoId: String,
  val target: BigDecimal
) {

  fun toPriceTargetResponse(cryptoName: String, currentPrice: BigDecimal, change: Float) =
    PriceTargetResponse(id, cryptoName, currentPrice.toPlainString(), target.toPlainString(), change)

  fun calculateChangeNeeded(currentPrice: BigDecimal): Float {
    return target.subtract(currentPrice)
      .divide(currentPrice, 3, RoundingMode.HALF_UP)
      .multiply(BigDecimal("100"))
      .setScale(2, RoundingMode.HALF_UP)
      .toFloat()
  }
}
