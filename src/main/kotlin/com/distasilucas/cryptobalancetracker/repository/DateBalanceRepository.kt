package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.DateBalance
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface DateBalanceRepository : MongoRepository<DateBalance, String> {

  fun findDateBalancesByDateBetween(from: String, to: String): List<DateBalance>
  fun findAllByDateIn(dates: List<String>): List<DateBalance>
  fun findDateBalanceByDate(date: String): Optional<DateBalance>
}
