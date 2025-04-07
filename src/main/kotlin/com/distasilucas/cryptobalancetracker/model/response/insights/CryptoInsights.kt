package com.distasilucas.cryptobalancetracker.model.response.insights

import java.io.Serializable

data class CryptoInsights(
  val id: String,
  val userCryptoInfo: UserCryptoInsights
): Serializable
