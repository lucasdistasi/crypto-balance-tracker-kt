package com.distasilucas.cryptobalancetracker.model.response.coingecko

import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime

data class CoingeckoCryptoInfo(
  val id: String,
  val symbol: String,
  val name: String,
  val image: Image,

  @JsonProperty("market_cap_rank")
  val marketCapRank: Int,

  @JsonProperty("market_data")
  val marketData: MarketData
) : Serializable {

  fun toCrypto(clock: Clock): Crypto {
    return Crypto(
      id = id,
      name = name,
      ticker = symbol,
      image = image.large,
      lastKnownPrice = marketData.currentPrice.usd,
      lastKnownPriceInEUR = marketData.currentPrice.eur,
      lastKnownPriceInBTC = marketData.currentPrice.btc,
      circulatingSupply = marketData.circulatingSupply,
      maxSupply = marketData.maxSupply ?: BigDecimal.ZERO,
      marketCapRank = marketCapRank,
      marketCap = marketData.marketCap.usd,
      changePercentageIn24h = marketData.changePercentageIn24h.roundChangePercentage(),
      changePercentageIn7d = marketData.changePercentageIn7d.roundChangePercentage(),
      changePercentageIn30d = marketData.changePercentageIn30d.roundChangePercentage(),
      lastUpdatedAt = LocalDateTime.now(clock)
    )
  }

  private fun Double.roundChangePercentage() = String.format("%.2f", this).toDouble()
}

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
  val changePercentageIn24h: Double,

  @JsonProperty("price_change_percentage_7d")
  val changePercentageIn7d: Double,

  @JsonProperty("price_change_percentage_30d")
  val changePercentageIn30d: Double
) : Serializable

data class CurrentPrice(
  val usd: BigDecimal,
  val eur: BigDecimal,
  val btc: BigDecimal
) : Serializable

data class MarketCap(
  val usd: BigDecimal
) : Serializable
