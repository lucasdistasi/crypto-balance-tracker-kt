package com.distasilucas.cryptobalancetracker.model.response.crypto

import java.io.Serializable

data class UserCryptoResponse(
  val id: String,
  val cryptoName: String,
  val quantity: String,
  val platform: String
): Serializable
