package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.entity.TransactionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository

interface TransactionRepository : MongoRepository<Transaction, String> {

  fun findAllByDateBetween(dateFrom: String, dateTo: String, pageable: Pageable): Page<Transaction>

  @Aggregation(
    pipeline = [
      "{ '\$match': { '\$and': [" +
        "{ 'date': { '\$gte': ?0, '\$lte': ?1 } }," +
        "{ '\$expr': { '\$cond': [ { '\$ne': [?2, null] }, { '\$eq': ['\$crypto_ticker', ?2] }, true ] } }," +
        "{ '\$expr': { '\$cond': [ { '\$ne': [?3, null] }, { '\$eq': ['\$transaction_type', ?3] }, true ] } }," +
        "{ '\$expr': { '\$cond': [ { '\$ne': [?4, null] }, { '\$eq': ['\$platform', ?4] }, true ] } }" +
        "] } }",
      "{ '\$sort': { 'date': -1 } }",
    ]
  )
  fun findTransactions(
    dateFrom: String,
    dateTo: String,
    cryptoTicker: String? = null,
    transactionType: TransactionType? = null,
    platform: String? = null
  ): List<Transaction>
}
