package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Document("Cryptos")
data class Crypto(
  @Id
  val id: String,
  val name: String,
  val ticker: String,
  val image: String,

  @Field("last_known_price")
  val lastKnownPrice: BigDecimal,

  @Field("last_known_price_in_eur")
  val lastKnownPriceInEUR: BigDecimal,

  @Field("last_known_price_in_btc")
  val lastKnownPriceInBTC: BigDecimal,

  @Field("circulating_supply")
  val circulatingSupply: BigDecimal,

  @Field("max_supply")
  val maxSupply: BigDecimal,

  @Field("market_cap_rank")
  val marketCapRank: Int,

  @Field("market_cap")
  val marketCap: BigDecimal,

  @Field("change_percentage_in_24h")
  val changePercentageIn24h: BigDecimal,

  @Field("change_percentage_in_7d")
  val changePercentageIn7d: BigDecimal,

  @Field("change_percentage_in_30d")
  val changePercentageIn30d: BigDecimal,

  @Field("last_updated_at")
  val lastUpdatedAt: LocalDateTime
) : Serializable {

  fun toCryptoInfo(
    price: Price? = null,
    priceChange: PriceChange? = null
  ) = CryptoInfo(name, id, ticker, image, price, priceChange)
}
