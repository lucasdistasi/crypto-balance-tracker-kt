package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.DateBalance
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime
import java.util.Optional

interface DateBalanceRepository : MongoRepository<DateBalance, String> {

  fun findDateBalancesByDateBetween(from: LocalDateTime, to: LocalDateTime): List<DateBalance>
  fun findAllByDateIn(dates: List<LocalDateTime>): List<DateBalance>
  fun findDateBalanceByDate(date: LocalDateTime): Optional<DateBalance>
}
