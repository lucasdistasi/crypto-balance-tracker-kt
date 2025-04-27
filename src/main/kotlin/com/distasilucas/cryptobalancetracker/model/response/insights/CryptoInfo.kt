package com.distasilucas.cryptobalancetracker.model.response.insights

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CryptoInfo(
  val cryptoName: String? = null,

  @JsonProperty("cryptoId")
  val coingeckoCryptoId: String,
  val symbol: String,
  val image: String,
  val price: Price? = null,
  val priceChange: PriceChange? = null,
): Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Price(
  val usd: String,
  val eur: String,
  val btc: String? = null,
): Serializable {
  constructor(usd: BigDecimal, eur: BigDecimal, btc: BigDecimal) : this(
    usd.toPlainString(), eur.toPlainString(), btc.toPlainString()
  )

  constructor(usd: BigDecimal, eur: BigDecimal) : this(
    usd.toPlainString(), eur.toPlainString()
  )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PriceChange(
  val changePercentageIn24h: Double? = null,
  val changePercentageIn7d: Double? = null,
  val changePercentageIn30d: Double? = null
): Serializable
