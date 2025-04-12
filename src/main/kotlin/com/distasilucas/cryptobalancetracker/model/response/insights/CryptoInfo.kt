package com.distasilucas.cryptobalancetracker.model.response.insights

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.math.BigDecimal

// TODO - instead of using nullable fields, maybe create new class for PlatformInsightsResponse

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CryptoInfo(
  val cryptoName: String,

  @JsonProperty("cryptoId")
  val coingeckoCryptoId: String,
  val symbol: String,
  val image: String,
  val currentPrice: CurrentPrice? = null,
  val priceChange: PriceChange? = null,
): Serializable

data class CurrentPrice(
  val usd: String,
  val eur: String,
  val btc: String
): Serializable {
  constructor(usd: BigDecimal, eur: BigDecimal, btc: BigDecimal) : this(
    usd.toPlainString(), eur.toPlainString(), btc.toPlainString()
  )
}

data class PriceChange(
  val changePercentageIn24h: BigDecimal,
  val changePercentageIn7d: BigDecimal,
  val changePercentageIn30d: BigDecimal
): Serializable
