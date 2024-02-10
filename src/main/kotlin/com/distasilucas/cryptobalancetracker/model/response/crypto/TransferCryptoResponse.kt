package com.distasilucas.cryptobalancetracker.model.response.crypto

data class TransferCryptoResponse(
  val fromPlatform: FromPlatform,
  val toPlatform: ToPlatform
)

data class FromPlatform(
  val userCryptoId: String,
  val networkFee: String,
  val quantityToTransfer: String,
  val totalToSubtract: String,
  val quantityToSendReceive: String,
  val remainingCryptoQuantity: String,
  val sendFullQuantity: Boolean
)

data class ToPlatform(
  val platformId: String,
  val newQuantity: String
)
