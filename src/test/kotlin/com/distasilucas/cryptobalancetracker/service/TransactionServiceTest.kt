package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_DATE_RANGE_EXCEEDED
import com.distasilucas.cryptobalancetracker.controller.TransactionFilters
import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.entity.TransactionType
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.request.transaction.TransactionRequest
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.repository.TransactionRepository
import getCoingeckoCrypto
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class TransactionServiceTest {

  private val transactionRepositoryMock = mockk<TransactionRepository>()
  private val cryptoServiceMock = mockk<CryptoService>()
  private val cacheServiceMock = mockk<CacheService>()
  private val clockMock = mockk<Clock>()

  private val transactionService: TransactionService = TransactionService(
    transactionRepositoryMock,
    cryptoServiceMock,
    cacheServiceMock,
    clockMock
  )

  @Test
  fun `should retrieve last six months transaction`() {
    val localDateTime = LocalDateTime.of(2024, 9, 15, 18, 55, 0)
    val zonedDateTime = ZonedDateTime.of(2024, 9, 15, 19, 0, 0, 0, ZoneId.of("UTC"))
    val pageRequest = PageRequest.of(0, 10)
    val transactions = listOf(
      Transaction(
        "e460cbd3-f6a2-464a-80d9-843e28f01d73",
        "bitcoin",
        "btc",
        BigDecimal("1"),
        BigDecimal("60000"),
        TransactionType.BUY,
        "BINANCE",
        "2024-09-15"
      ),
      Transaction(
        "12de547d-714c-4942-bbf5-2947e53dc8c0",
        "ethereum",
        "eth",
        BigDecimal("0.5"),
        BigDecimal("2360"),
        TransactionType.SELL,
        "BINANCE",
        "2024-09-15"
      )
    )
    val pageTransaction = PageImpl(transactions, pageRequest, 2)
    val from = localDateTime.toLocalDate().minusMonths(6).minusDays(1)
    val to = localDateTime.toLocalDate().plusDays(1)

    every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns zonedDateTime.zone
    every {
      transactionRepositoryMock.findAllByDateBetweenOrderByDateDescIdDesc(from.toString(), to.toString(), pageRequest)
    } returns pageTransaction

    val transaction = transactionService.retrieveLastSixMonthsTransactions(0)

    assertThat(transaction)
      .usingRecursiveComparison()
      .isEqualTo(pageTransaction)
  }

  @Test
  fun `should retrieve last months transaction with filters`() {
    val transactionFilters = TransactionFilters(
      dateFrom = LocalDate.of(2024, 1, 1),
      dateTo = LocalDate.of(2024, 4, 15),
      transactionType = TransactionType.BUY,
    )
    val entityTransactions = listOf(
      Transaction(
        "e460cbd3-f6a2-464a-80d9-843e28f01d73",
        "bitcoin",
        "btc",
        BigDecimal("1"),
        BigDecimal("60000"),
        TransactionType.BUY,
        "BINANCE",
        "2024-02-14"
      ),
      Transaction(
        "12de547d-714c-4942-bbf5-2947e53dc8c0",
        "ethereum",
        "eth",
        BigDecimal("0.5"),
        BigDecimal("2360"),
        TransactionType.SELL,
        "BINANCE",
        "2024-03-15"
      )
    )
    every {
      transactionRepositoryMock.findTransactions(
        dateFrom = "2024-01-01",
        dateTo = "2024-04-15",
        transactionType = TransactionType.BUY,
      )
    } returns entityTransactions

    val transactions = transactionService.retrieveFilteredTransactions(transactionFilters)

    assertThat(entityTransactions)
      .usingRecursiveComparison()
      .isEqualTo(transactions)
  }

  @Test
  fun `should throw ApiException when retrieving transaction with range longer than six months`() {
    val transactionFilters = TransactionFilters(
      dateFrom = LocalDate.of(2024, 1, 1),
      dateTo = LocalDate.of(2024, 7, 2),
    )

    val exception = assertThrows<ApiException> {
      transactionService.retrieveFilteredTransactions(transactionFilters)
    }

    assertEquals(TRANSACTION_DATE_RANGE_EXCEEDED, exception.message)
  }

  @Test
  fun `should save transaction`() {
    val transaction = Transaction(
      "99a430c9-9af8-494e-b5dc-64ef27d0a8ac",
      "bitcoin",
      "btc",
      BigDecimal("0.5"),
      BigDecimal("60000"),
      TransactionType.SELL,
      "BINANCE",
      "2024-09-22"
    )
    val transactionRequest = TransactionRequest(
      "bitcoin",
      BigDecimal("0.5"),
      BigDecimal("60000"),
      TransactionType.SELL,
      "Binance",
      LocalDate.of(2024, 9, 22)
    )

    mockkStatic(UUID::class)
    every {
      cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")
    } returns getCoingeckoCrypto()
    every { UUID.randomUUID().toString() } returns "99a430c9-9af8-494e-b5dc-64ef27d0a8ac"
    every { transactionRepositoryMock.save(transaction) } returns transaction
    justRun { cacheServiceMock.invalidate(CacheType.TRANSACTION_CACHES) }

    transactionService.saveTransaction(transactionRequest)

    verify(exactly = 1) { transactionRepositoryMock.save(transaction) }
    verify(exactly = 1) { cacheServiceMock.invalidate(CacheType.TRANSACTION_CACHES) }
  }

  @Test
  fun `should update transaction`() {
    val transaction = Transaction(
      "99a430c9-9af8-494e-b5dc-64ef27d0a8ac",
      "bitcoin",
      "btc",
      BigDecimal("0.5"),
      BigDecimal("60000"),
      TransactionType.SELL,
      "BINANCE",
      "2024-09-22"
    )
    val newTransaction = transaction.copy(price = BigDecimal("60500"))
    val transactionRequest = TransactionRequest(
      "bitcoin",
      BigDecimal("0.5"),
      BigDecimal("60500"),
      TransactionType.SELL,
      "Binance",
        LocalDate.of(2024, 9, 22)
    )

    mockkStatic(UUID::class)
    every {
      cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")
    } returns getCoingeckoCrypto()
    every { UUID.randomUUID().toString() } returns "99a430c9-9af8-494e-b5dc-64ef27d0a8ac"
    every {
      transactionRepositoryMock.findById("99a430c9-9af8-494e-b5dc-64ef27d0a8ac")
    } returns Optional.of(transaction)
    every { transactionRepositoryMock.save(newTransaction) } returns newTransaction
    justRun { cacheServiceMock.invalidate(CacheType.TRANSACTION_CACHES) }

    transactionService.updateTransaction("99a430c9-9af8-494e-b5dc-64ef27d0a8ac", transactionRequest)

    verify(exactly = 1) { transactionRepositoryMock.save(newTransaction) }
    verify(exactly = 1) { cacheServiceMock.invalidate(CacheType.TRANSACTION_CACHES) }
  }

  @Test
  fun `should throw ApiException with Not Found when updating transaction`() {
    val transactionId = "99a430c9-9af8-494e-b5dc-64ef27d0a8ac"
    val transactionRequest = TransactionRequest(
      "bitcoin",
      BigDecimal("0.5"),
      BigDecimal("60000"),
      TransactionType.SELL,
      "Binance",
        LocalDate.of(2024, 9, 22)
    )

    every {
      cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")
    } returns getCoingeckoCrypto()
    every {
      transactionRepositoryMock.findById(transactionId)
    } returns Optional.empty()

    val exception = assertThrows<ApiException> {
      transactionService.updateTransaction(transactionId, transactionRequest)
    }

    verify(exactly = 0) { transactionRepositoryMock.save(any()) }
    assertEquals("Transaction $transactionId does not exists", exception.message)
    assertEquals(HttpStatus.NOT_FOUND, exception.httpStatusCode)
  }

  @Test
  fun `should delete transaction`() {
    val transaction = Transaction(
      "99a430c9-9af8-494e-b5dc-64ef27d0a8ac",
      "bitcoin",
      "btc",
      BigDecimal("0.5"),
      BigDecimal("60000"),
      TransactionType.SELL,
      "Binance",
      "2024-09-22"
    )

    every {
      transactionRepositoryMock.findById("99a430c9-9af8-494e-b5dc-64ef27d0a8ac")
    } returns Optional.of(transaction)
    justRun { transactionRepositoryMock.delete(transaction) }
    justRun { cacheServiceMock.invalidate(CacheType.TRANSACTION_CACHES) }

    transactionService.deleteTransaction("99a430c9-9af8-494e-b5dc-64ef27d0a8ac")

    verify(timeout = 1) { transactionRepositoryMock.delete(transaction) }
    verify(timeout = 1) { cacheServiceMock.invalidate(CacheType.TRANSACTION_CACHES) }
  }

  @Test
  fun `should throw ApiException when deleting transaction`() {
    val transactionId = "99a430c9-9af8-494e-b5dc-64ef27d0a8ac"

    every { transactionRepositoryMock.findById(transactionId) } returns Optional.empty()

    val exception = assertThrows<ApiException> {
      transactionService.deleteTransaction(transactionId)
    }

    verify(exactly = 0) { transactionRepositoryMock.delete(any()) }
    assertEquals("Transaction $transactionId does not exists", exception.message)
    assertEquals(HttpStatus.NOT_FOUND, exception.httpStatusCode)
  }
}
