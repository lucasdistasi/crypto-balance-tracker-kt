package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate
import java.util.*

@Document("DateBalances")
data class DateBalance(
  @Id
  val id: String = UUID.randomUUID().toString(),
  val date: String,

  @Field("usd_balance")
  val usdBalance: String,

  @Field("eur_balance")
  val eurBalance: String,

  @Field("btc_balance")
  val btcBalance: String,
) {
  constructor(date: LocalDate, usdBalance: String, eurBalance: String, btcBalance: String) : this(UUID.randomUUID().toString(), date.toString(), usdBalance, eurBalance, btcBalance)

  constructor(id: String, date: String, balances: Balances) : this(id, date, balances.fiat.usd, balances.fiat.eur, balances.btc)
}
