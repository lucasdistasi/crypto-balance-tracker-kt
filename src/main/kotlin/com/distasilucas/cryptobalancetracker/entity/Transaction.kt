package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.transaction.TransactionResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Document("Transactions")
data class Transaction(
  @Id
  val id: String,

  @Field("crypto_ticker")
  val cryptoTicker: String,

  @Field("quantity")
  val quantity: BigDecimal,

  @Field("price")
  val price: BigDecimal,

  @Field("transaction_type")
  val transactionType: TransactionType,

  @Field("platform")
  val platform: String,

  @Field("date")
  val date: String,
) {

  fun toTransactionResponse() =
    TransactionResponse(
      id,
      cryptoTicker, quantity.toPlainString(),
      price.toPlainString(),
      transactionType,
      platform,
      LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
    )
}

enum class TransactionType {
  BUY,
  SELL
}
