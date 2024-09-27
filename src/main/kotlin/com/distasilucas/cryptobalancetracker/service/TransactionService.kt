package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.LATEST_TRANSACTIONS_CACHES
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_CRYPTO_TICKER_NOT_EXISTS
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_DATE_RANGE_EXCEEDED
import com.distasilucas.cryptobalancetracker.controller.TransactionFilters
import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.repository.TransactionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
class TransactionService(
  private val transactionRepository: TransactionRepository,
  private val coingeckoService: CoingeckoService,
  private val cacheService: CacheService,
  private val clock: Clock
) {

  private val logger = KotlinLogging.logger { }

  @Cacheable(cacheNames = [LATEST_TRANSACTIONS_CACHES], key = "#page")
  fun retrieveLastSixMonthsTransactions(page: Int = 0): Page<Transaction> {
    val now = LocalDate.now(clock)
    val to = now.plusDays(1).toString()
    val from = now.minusMonths(6).minusDays(1).toString()
    val pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "date"))

    logger.info { "Retrieving transactions from $from to $to with page $pageRequest" }

    return transactionRepository.findAllByDateBetween(from, to, pageRequest)
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

  fun saveTransaction(transaction: Transaction) {
    logger.info { "Saving transaction $transaction" }
    validateCryptoTickerExists(transaction.cryptoTicker)

    transactionRepository.save(transaction)
    cacheService.invalidate(CacheType.TRANSACTION_CACHES)
  }

  fun updateTransaction(transaction: Transaction) {
    logger.info { "Updating transaction $transaction" }
    validateCryptoTickerExists(transaction.cryptoTicker)

    transactionRepository.findById(transaction.id)
      .ifPresentOrElse({
        val updatedTransaction = transactionRepository.save(transaction)
        logger.info { "Updated transaction $updatedTransaction" }
        cacheService.invalidate(CacheType.TRANSACTION_CACHES)
      }, {
        throw ApiException(HttpStatus.NOT_FOUND, "Transaction ${transaction.id} does not exists")
      })
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

  private fun validateSixMonthsRange(dateFrom: LocalDate, dateTo: LocalDate) {
    if (dateFrom.plusMonths(6) < dateTo)
      throw ApiException(HttpStatus.BAD_REQUEST, TRANSACTION_DATE_RANGE_EXCEEDED)
  }

  private fun validateCryptoTickerExists(cryptoTicker: String) {
    val crypto = coingeckoService.retrieveAllCryptos()
      .firstOrNull { it.symbol.equals(cryptoTicker, true) }

    if (crypto == null)
      throw ApiException(HttpStatus.BAD_REQUEST, TRANSACTION_CRYPTO_TICKER_NOT_EXISTS)
  }
}
