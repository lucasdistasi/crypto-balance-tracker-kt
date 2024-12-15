package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.transaction.TransactionResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Document("Transactions")
data class Transaction(
  @Id
  val id: String,

  @Field("crypto_id")
  val coingeckoCryptoId: String,

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
) : Serializable {

  fun toTransactionResponse() =
    TransactionResponse(
      id,
      coingeckoCryptoId,
      cryptoTicker,
      quantity.toPlainString(),
      price.toPlainString(),
      calculateTotal().toPlainString(),
      transactionType,
      platform,
      LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
    )

  private fun calculateTotal() = quantity.multiply(price).setScale(2, RoundingMode.HALF_UP)
}

enum class TransactionType {
  BUY,
  SELL
}
