package com.distasilucas.cryptobalancetracker.model.response.insights

import com.distasilucas.cryptobalancetracker.entity.Crypto
import java.math.BigDecimal

data class MarketData(
  val circulatingSupply: CirculatingSupply,
  val maxSupply: String,
  val currentPrice: CurrentPrice,
  val marketCap: String,
  val priceChange: PriceChange
) {

  constructor(crypto: Crypto, circulatingSupply: CirculatingSupply): this(
      circulatingSupply,
      crypto.maxSupply.toPlainString(),
      CurrentPrice(crypto.lastKnownPrice.toPlainString(), crypto.lastKnownPriceInEUR.toPlainString(), crypto.lastKnownPriceInBTC.toPlainString()),
      crypto.marketCap.toPlainString(),
      PriceChange(crypto.changePercentageIn24h, crypto.changePercentageIn7d, crypto.changePercentageIn30d)
    )

}

data class CurrentPrice(
  val usd: String,
  val eur: String,
  val btc: String
)

data class PriceChange(
  val changePercentageIn24h: BigDecimal,
  val changePercentageIn7d: BigDecimal,
  val changePercentageIn30d: BigDecimal
)

data class CirculatingSupply(
  val totalCirculatingSupply: String,
  val percentage: Float = 0F
)
