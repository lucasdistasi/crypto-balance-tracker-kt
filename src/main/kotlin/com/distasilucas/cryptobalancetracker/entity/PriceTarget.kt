package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable
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
): Serializable {

  fun toPriceTargetResponse(
    cryptoInfo: CryptoInfo,
    currentPrice: BigDecimal,
    change: Float
  ) = PriceTargetResponse(id, cryptoInfo, currentPrice.toPlainString(), target.toPlainString(), change)

  fun calculateChangeNeeded(currentPrice: BigDecimal): Float {
    val change = target.subtract(currentPrice)
      .divide(currentPrice, 3, RoundingMode.HALF_UP)
      .multiply(BigDecimal("100"))
      .setScale(2, RoundingMode.HALF_UP)

    return if (isChangeNeededGreaterThanMaxFloat(change)) {
      9999999F
    } else {
      change.toFloat()
    }
  }

  private fun isChangeNeededGreaterThanMaxFloat(change: BigDecimal) = change > BigDecimal("9999999")
}
