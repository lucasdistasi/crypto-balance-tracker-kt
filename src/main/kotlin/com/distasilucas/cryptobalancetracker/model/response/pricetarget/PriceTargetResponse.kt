package com.distasilucas.cryptobalancetracker.model.response.pricetarget

import java.io.Serializable

data class PriceTargetResponse(
  val priceTargetId: String,
  val cryptoName: String,
  val currentPrice: String,
  val priceTarget: String,
  val change: Float
) : Serializable
