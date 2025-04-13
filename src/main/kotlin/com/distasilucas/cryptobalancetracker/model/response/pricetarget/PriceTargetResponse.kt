package com.distasilucas.cryptobalancetracker.model.response.pricetarget

import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import java.io.Serializable

data class PriceTargetResponse(
  val priceTargetId: String,
  val cryptoInfo: CryptoInfo,
  val currentPrice: String,
  val priceTarget: String,
  val change: Float
): Serializable
