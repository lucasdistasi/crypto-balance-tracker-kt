package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.entity.TransactionType
import com.distasilucas.cryptobalancetracker.model.request.transaction.TransactionRequest
import com.distasilucas.cryptobalancetracker.model.response.transaction.PageTransactionsResponse
import com.distasilucas.cryptobalancetracker.model.response.transaction.TransactionResponse
import com.distasilucas.cryptobalancetracker.service.TransactionService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class TransactionControllerTest {

  private val transactionServiceMock = mockk<TransactionService>()
  private val transactionController = TransactionController(transactionServiceMock)

  @Test
  fun `should return 200 when retrieving latest transactions`() {
    val transactions = listOf(
      Transaction(
        id = "e460cbd3-f6a2-464a-80d9-843e28f01d73",
        cryptoTicker = "BTC",
        quantity = BigDecimal("1"),
        price = BigDecimal("60000"),
        transactionType = TransactionType.BUY,
        platform = "BINANCE",
        date = "2024-02-14"
      ),
      Transaction(
        id = "12de547d-714c-4942-bbf5-2947e53dc8c0",
        cryptoTicker = "ETH",
        quantity = BigDecimal("0.5"),
        price = BigDecimal("2360"),
        transactionType = TransactionType.SELL,
        platform = "BINANCE",
        date = "2024-03-15"
      )
    )
    val transactionsPage = PageImpl(transactions, PageRequest.of(0, 10), 2)
    val transactionsResponse = transactions.map { it.toTransactionResponse() }

    every {
      transactionServiceMock.retrieveLastSixMonthsTransactions()
    } returns transactionsPage

    val responseEntity = transactionController.retrieveLatestTransactions()
    val pageTransactions = PageTransactionsResponse(0, 1, transactionsResponse)

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(pageTransactions))
  }

  @Test
  fun `should return 204 when retrieving latest transactions`() {
    every { transactionServiceMock.retrieveLastSixMonthsTransactions() } returns Page.empty()

    val responseEntity = transactionController.retrieveLatestTransactions()

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<PageTransactionsResponse>())
  }

  @Test
  fun `should return 200 when retrieving filtered transactions`() {
    val dateFrom = LocalDate.of(2024, 1, 1)
    val dateTo = LocalDate.of(2024, 6, 1)
    val transactionFilters = TransactionFilters(dateFrom, dateTo, "BTC")
    val transactions = listOf(
      Transaction(
        id = "e460cbd3-f6a2-464a-80d9-843e28f01d73",
        cryptoTicker = "BTC",
        quantity = BigDecimal("1"),
        price = BigDecimal("60000"),
        transactionType = TransactionType.BUY,
        platform = "BINANCE",
        date = "2024-02-14"
      ),
      Transaction(
        id = "12de547d-714c-4942-bbf5-2947e53dc8c0",
        cryptoTicker = "ETH",
        quantity = BigDecimal("0.5"),
        price = BigDecimal("2360"),
        transactionType = TransactionType.SELL,
        platform = "BINANCE",
        date = "2024-03-15"
      )
    )
    val transactionsResponse = transactions.map { it.toTransactionResponse() }

    every {
      transactionServiceMock.retrieveFilteredTransactions(transactionFilters)
    } returns transactions

    val responseEntity = transactionController.retrieveFilteredTransactions(
      dateFrom = dateFrom,
      dateTo = dateTo,
      cryptoTicker = "BTC"
    )

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(transactionsResponse))
  }

  @Test
  fun `should return 204 when retrieving filtered transactions`() {
    val dateFrom = LocalDate.of(2024, 1, 1)
    val dateTo = LocalDate.of(2024, 6, 1)
    val transactionFilters = TransactionFilters(dateFrom, dateTo)

    every {
      transactionServiceMock.retrieveFilteredTransactions(transactionFilters)
    } returns emptyList()

    val responseEntity = transactionController.retrieveFilteredTransactions(
      dateFrom = dateFrom,
      dateTo = dateTo
    )

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<List<TransactionResponse>>())
  }

  @Test
  fun `should return 201 when saving transaction`() {
    val transactionRequest = TransactionRequest(
      "BTC",
      BigDecimal("0.25"),
      BigDecimal("60000"),
      TransactionType.SELL,
      "BINANCE",
      LocalDate.of(2024, 9, 22),
    )
    val transaction = transactionRequest.toTransactionEntity("e460cbd3-f6a2-464a-80d9-843e28f01d73")

    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns "e460cbd3-f6a2-464a-80d9-843e28f01d73"
    justRun { transactionServiceMock.saveTransaction(transaction) }

    val responseEntity = transactionController.saveTransaction(transactionRequest)

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(
        ResponseEntity.status(HttpStatus.CREATED)
          .header(HttpHeaders.LOCATION, "/api/v1/transactions/e460cbd3-f6a2-464a-80d9-843e28f01d73")
          .body(transaction.toTransactionResponse())
      )
  }

  @Test
  fun `should return 200 when updating transaction`() {
    val transactionRequest = TransactionRequest(
      "BTC",
      BigDecimal("0.275"),
      BigDecimal("60000"),
      TransactionType.SELL,
      "BINANCE",
      LocalDate.of(2024, 9, 22),
    )
    val transaction = transactionRequest.toTransactionEntity("e460cbd3-f6a2-464a-80d9-843e28f01d73")

    justRun { transactionServiceMock.updateTransaction(transaction) }

    val responseEntity = transactionController.updateTransaction(
      "e460cbd3-f6a2-464a-80d9-843e28f01d73",
      transactionRequest
    )

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(transaction.toTransactionResponse()))
  }

  @Test
  fun `should return 204 when deleting transaction`() {
    justRun { transactionServiceMock.deleteTransaction("e460cbd3-f6a2-464a-80d9-843e28f01d73") }

    val responseEntity = transactionController.deleteTransaction("e460cbd3-f6a2-464a-80d9-843e28f01d73")

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<Unit>())
  }
}
