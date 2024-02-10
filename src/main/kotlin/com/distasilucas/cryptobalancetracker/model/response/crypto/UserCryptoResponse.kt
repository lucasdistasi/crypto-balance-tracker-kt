package com.distasilucas.cryptobalancetracker.model.response.crypto

data class UserCryptoResponse(
  val id: String,
  val cryptoName: String,
  val quantity: String,
  val platform: String
)
