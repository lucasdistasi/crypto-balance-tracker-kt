package com.distasilucas.cryptobalancetracker.model.response.coingecko

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

data class CoingeckoCryptoInfo(
  val id: String,
  val symbol: String,
  val name: String,
  val image: Image,

  @JsonProperty("market_cap_rank")
  val marketCapRank: Int,

  @JsonProperty("market_data")
  val marketData: MarketData
) : Serializable

data class Image(
  val large: String
) : Serializable

data class MarketData(
  @JsonProperty("current_price")
  val currentPrice: CurrentPrice,

  @JsonProperty("circulating_supply")
  val circulatingSupply: BigDecimal,

  @JsonProperty("max_supply")
  val maxSupply: BigDecimal?,

  @JsonProperty("market_cap")
  val marketCap: MarketCap,

  @JsonProperty("price_change_percentage_24h")
  val changePercentageIn24h: BigDecimal,

  @JsonProperty("price_change_percentage_7d")
  val changePercentageIn7d: BigDecimal,

  @JsonProperty("price_change_percentage_30d")
  val changePercentageIn30d: BigDecimal
) : Serializable

data class CurrentPrice(
  val usd: BigDecimal,
  val eur: BigDecimal,
  val btc: BigDecimal
) : Serializable

data class MarketCap(
  val usd: BigDecimal
) : Serializable
