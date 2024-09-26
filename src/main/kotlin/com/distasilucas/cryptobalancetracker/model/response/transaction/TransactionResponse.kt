package com.distasilucas.cryptobalancetracker.model.response.transaction

import com.distasilucas.cryptobalancetracker.entity.TransactionType

data class TransactionResponse(
  val id: String,
  val ticker: String,
  val quantity: String,
  val price: String,
  val total: String,
  val transactionType: TransactionType,
  val platform: String,
  val date: String
)
