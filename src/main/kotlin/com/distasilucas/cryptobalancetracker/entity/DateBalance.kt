package com.distasilucas.cryptobalancetracker.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime
import java.util.UUID

@Document("DateBalances")
data class DateBalance(
  @Id
  val id: String = UUID.randomUUID().toString(),
  val date: LocalDateTime,

  @Field("usd_balance")
  val usdBalance: String,

  @Field("eur_balance")
  val eurBalance: String,

  @Field("btc_balance")
  val btcBalance: String,
)
