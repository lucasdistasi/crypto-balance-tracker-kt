package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.LATEST_TRANSACTIONS_CACHE
import com.distasilucas.cryptobalancetracker.constants.TRANSACTIONS_INFO_CACHE
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_DATE_RANGE_EXCEEDED
import com.distasilucas.cryptobalancetracker.controller.TransactionFilters
import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.request.transaction.TransactionRequest
import com.distasilucas.cryptobalancetracker.model.response.insights.TransactionsInfo
import com.distasilucas.cryptobalancetracker.repository.TransactionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate

@Service
class TransactionService(
  private val transactionRepository: TransactionRepository,
  private val cryptoService: CryptoService,
  private val cacheService: CacheService,
  private val clock: Clock
) {

  private val logger = KotlinLogging.logger { }

  @Cacheable(cacheNames = [LATEST_TRANSACTIONS_CACHE], key = "#page")
  fun retrieveLastSixMonthsTransactions(page: Int = 0): Page<Transaction> {
    val now = LocalDate.now(clock)
    val to = now.plusDays(1).toString()
    val from = now.minusMonths(6).minusDays(1).toString()
    val pageRequest = PageRequest.of(page, 10)

    logger.info { "Retrieving transactions from $from to $to with page $pageRequest" }

    return transactionRepository.findAllByDateBetweenOrderByDateDescIdDesc(from, to, pageRequest)
  }

  fun retrieveFilteredTransactions(transactionFilters: TransactionFilters): List<Transaction> {
    logger.info { "Retrieving filtered transactions" }
    validateSixMonthsRange(transactionFilters.dateFrom, transactionFilters.dateTo)

    val transactions = transactionRepository.findTransactions(
      dateFrom = transactionFilters.dateFrom.toString(),
      dateTo = transactionFilters.dateTo.toString(),
      cryptoTicker = transactionFilters.cryptoTicker,
      transactionType = transactionFilters.transactionType,
      platform = transactionFilters.platform
    )

    logger.info { "Found ${transactions.size} transactions with filters $transactionFilters" }

    return transactions
  }

  fun saveTransaction(transactionRequest: TransactionRequest): Transaction {
    logger.info { "Saving transaction $transactionRequest" }
    val crypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(transactionRequest.cryptoNameOrId!!)
    val transaction = transactionRequest.toTransactionEntity(coingeckoCrypto = crypto)

    transactionRepository.save(transaction)
    cacheService.invalidate(CacheType.TRANSACTION_CACHES)

    return transaction
  }

  fun updateTransaction(transactionId: String, transactionRequest: TransactionRequest): Transaction {
    logger.info { "Updating transaction $transactionRequest" }
    val crypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(transactionRequest.cryptoNameOrId!!)
    val optionalTransaction = transactionRepository.findById(transactionId)

    if (optionalTransaction.isEmpty)
      throw ApiException(HttpStatus.NOT_FOUND, "Transaction $transactionId does not exists")

    val transaction = transactionRequest.toTransactionEntity(transactionId, crypto)
    val updatedTransaction = transactionRepository.save(transaction)

    logger.info { "Updated transaction $transaction" }
    cacheService.invalidate(CacheType.TRANSACTION_CACHES)

    return updatedTransaction
  }

  fun deleteTransaction(transactionId: String) {
    logger.info { "Deleting transaction $transactionId" }

    transactionRepository.findById(transactionId)
      .ifPresentOrElse({
        transactionRepository.delete(it)
        logger.info { "Deleted transaction $it" }
        cacheService.invalidate(CacheType.TRANSACTION_CACHES)
      }, {
        throw ApiException(HttpStatus.NOT_FOUND, "Transaction $transactionId does not exists")
      })
  }

  @Cacheable(cacheNames = [TRANSACTIONS_INFO_CACHE], key = "#coingeckoCryptoId")
  fun retrieveTransactionsInfo(coingeckoCryptoId: String): TransactionsInfo? {
    val transactions = transactionRepository.findAllByCoingeckoCryptoId(coingeckoCryptoId)

    if (transactions.isEmpty()) return null

    val totalBought = transactions.sumOf { it.quantity }
    val totalSpent = transactions.sumOf { it.quantity.multiply(it.price) }.setScale(2, RoundingMode.HALF_UP)
    //val sellTransactions = transactions.filter { TransactionType.SELL == it.transactionType }
    //val buyTransactions = transactions.filter { TransactionType.BUY == it.transactionType }
    val averageBuyPrice = totalSpent.divide(totalBought, RoundingMode.HALF_UP)

    return TransactionsInfo(averageBuyPrice)
  }

  private fun validateSixMonthsRange(dateFrom: LocalDate, dateTo: LocalDate) {
    if (dateFrom.plusMonths(6) < dateTo)
      throw ApiException(HttpStatus.BAD_REQUEST, TRANSACTION_DATE_RANGE_EXCEEDED)
  }
}
