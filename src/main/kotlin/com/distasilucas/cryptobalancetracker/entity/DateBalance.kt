package com.distasilucas.cryptobalancetracker.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document("DateBalances")
data class DateBalance(
  @Id
  val id: String = UUID.randomUUID().toString(),
  val date: LocalDateTime,
  val balance: String
)
