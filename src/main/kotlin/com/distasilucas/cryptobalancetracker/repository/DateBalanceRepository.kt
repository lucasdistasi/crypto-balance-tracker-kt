package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.DateBalance
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository

interface DateBalanceRepository : MongoRepository<DateBalance, String> {

  @Aggregation(pipeline = [
    "{ \$match: { 'date': { \$gte: ?0, \$lte: ?1 } } }"
  ])
  fun findDateBalancesByInclusiveDateBetween(from: String, to: String): List<DateBalance>
  fun findAllByDateIn(dates: List<String>): List<DateBalance>
  fun findDateBalanceByDate(date: String): DateBalance?
}
