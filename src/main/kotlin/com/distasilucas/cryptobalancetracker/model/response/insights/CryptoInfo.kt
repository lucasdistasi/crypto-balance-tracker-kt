package com.distasilucas.cryptobalancetracker.model.response.insights

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CryptoInfo(
  val cryptoName: String,

  @JsonProperty("cryptoId")
  val coingeckoCryptoId: String,
  val symbol: String,
  val image: String,
  val currentPrice: CurrentPrice,
  val priceChange: PriceChange,
)

data class CurrentPrice(
  val usd: String,
  val eur: String,
  val btc: String
) {
  constructor(usd: BigDecimal, eur: BigDecimal, btc: BigDecimal) : this(
    usd.toPlainString(), eur.toPlainString(), btc.toPlainString()
  )
}

data class PriceChange(
  val changePercentageIn24h: BigDecimal,
  val changePercentageIn7d: BigDecimal,
  val changePercentageIn30d: BigDecimal
)
