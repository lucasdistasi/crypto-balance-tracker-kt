package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.constants.INVALID_TRANSACTION_UUID
import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.entity.TransactionType
import com.distasilucas.cryptobalancetracker.service.TransactionService
import com.ninjasquad.springmockk.MockkBean
import deleteTransaction
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockkStatic
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import readJsonFileAsString
import retrieveFilteredTransactions
import retrieveLatestTransactions
import saveTransaction
import updateTransaction
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension::class)
@WebMvcTest(TransactionController::class)
class TransactionControllerMvcTest(
  @Autowired private val mockMvc: MockMvc
) {

  @MockkBean
  private lateinit var transactionServiceMock: TransactionService

  private val basePath = "src/test/resources/transaction"

  @Test
  fun `should return 200 when retrieving latest transactions`() {
    every {
      transactionServiceMock.retrieveLastSixMonthsTransactions()
    } returns PageImpl(transactions(), PageRequest.of(0, 10), 2)

    mockMvc.retrieveLatestTransactions()
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.page", `is`(1)))
      .andExpect(jsonPath("$.totalPages", `is`(1)))
      .andExpect(jsonPath("$.hasNextPage", `is`(false)))
      .andExpect(jsonPath("$.transactions").isArray())
      .andExpect(jsonPath("$.transactions", hasSize<Int>(2)))
      .andExpect(jsonPath("$.transactions[0].id", `is`("e460cbd3-f6a2-464a-80d9-843e28f01d73")))
      .andExpect(jsonPath("$.transactions[0].ticker", `is`("BTC")))
      .andExpect(jsonPath("$.transactions[0].quantity", `is`("1")))
      .andExpect(jsonPath("$.transactions[0].price", `is`("60000")))
      .andExpect(jsonPath("$.transactions[0].total", `is`("60000.00")))
      .andExpect(jsonPath("$.transactions[0].transactionType", `is`("BUY")))
      .andExpect(jsonPath("$.transactions[0].platform", `is`("BINANCE")))
      .andExpect(jsonPath("$.transactions[0].date", `is`("September 15, 2024")))
      .andExpect(jsonPath("$.transactions[1].id", `is`("12de547d-714c-4942-bbf5-2947e53dc8c0")))
      .andExpect(jsonPath("$.transactions[1].ticker", `is`("ETH")))
      .andExpect(jsonPath("$.transactions[1].quantity", `is`("0.5")))
      .andExpect(jsonPath("$.transactions[1].price", `is`("2360")))
      .andExpect(jsonPath("$.transactions[1].total", `is`("1180.00")))
      .andExpect(jsonPath("$.transactions[1].transactionType", `is`("SELL")))
      .andExpect(jsonPath("$.transactions[1].platform", `is`("BINANCE")))
      .andExpect(jsonPath("$.transactions[1].date", `is`("September 15, 2024")))
  }

  @Test
  fun `should return 204 when retrieving empty latest transactions`() {
    every {
      transactionServiceMock.retrieveLastSixMonthsTransactions()
    } returns PageImpl(emptyList(), PageRequest.of(0, 10), 0)

    mockMvc.retrieveLatestTransactions()
      .andExpect(status().isNoContent)
  }

  @Test
  fun `should return 400 when retrieving latest transactions with invalid page`() {
    mockMvc.retrieveLatestTransactions(-1)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`(INVALID_PAGE_NUMBER)))
  }

  @Test
  fun `should return 200 when retrieving filtered transactions`() {
    val dateFrom = LocalDate.of(2024, 8, 15)
    val dateTo = LocalDate.of(2024, 10, 25)
    val transactionFilters = TransactionFilters(dateFrom, dateTo)

    every {
      transactionServiceMock.retrieveFilteredTransactions(transactionFilters)
    } returns transactions()

    mockMvc.retrieveFilteredTransactions("2024-08-15", "2024-10-25")
      .andExpect(status().isOk)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(2)))
      .andExpect(jsonPath("$.[0].id", `is`("e460cbd3-f6a2-464a-80d9-843e28f01d73")))
      .andExpect(jsonPath("$.[0].ticker", `is`("BTC")))
      .andExpect(jsonPath("$.[0].quantity", `is`("1")))
      .andExpect(jsonPath("$.[0].price", `is`("60000")))
      .andExpect(jsonPath("$.[0].total", `is`("60000.00")))
      .andExpect(jsonPath("$.[0].transactionType", `is`("BUY")))
      .andExpect(jsonPath("$.[0].platform", `is`("BINANCE")))
      .andExpect(jsonPath("$.[0].date", `is`("September 15, 2024")))
      .andExpect(jsonPath("$.[1].id", `is`("12de547d-714c-4942-bbf5-2947e53dc8c0")))
      .andExpect(jsonPath("$.[1].ticker", `is`("ETH")))
      .andExpect(jsonPath("$.[1].quantity", `is`("0.5")))
      .andExpect(jsonPath("$.[1].price", `is`("2360")))
      .andExpect(jsonPath("$.[1].total", `is`("1180.00")))
      .andExpect(jsonPath("$.[1].transactionType", `is`("SELL")))
      .andExpect(jsonPath("$.[1].platform", `is`("BINANCE")))
      .andExpect(jsonPath("$.[1].date", `is`("September 15, 2024")))
  }

  @Test
  fun `should return 204 when retrieving filtered transactions`() {
    val dateFrom = LocalDate.of(2024, 8, 15)
    val dateTo = LocalDate.of(2024, 10, 25)
    val transactionFilters = TransactionFilters(dateFrom, dateTo)

    every {
      transactionServiceMock.retrieveFilteredTransactions(transactionFilters)
    } returns emptyList()

    mockMvc.retrieveFilteredTransactions("2024-08-15", "2024-10-25")
      .andExpect(status().isNoContent)
  }

  @Test
  fun `should return 400 when retrieving filtered transactions with invalid dateFrom`() {
    mockMvc.retrieveFilteredTransactions("08-15-2024", "2024-10-25")
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Invalid value 08-15-2024 for dateFrom")))
  }

  @Test
  fun `should return 400 when retrieving filtered transactions with invalid dateTo`() {
    mockMvc.retrieveFilteredTransactions("2024-08-15", "25-10-2024")
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Invalid value 25-10-2024 for dateTo")))
  }

  @Test
  fun `should return 400 when retrieving filtered transactions with invalid TransactionType`() {
    mockMvc.retrieveFilteredTransactions(dateFrom = "2024-08-15", dateTo = "2024-10-25", transactionType = "NONE")
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Invalid value NONE for transactionType. Available values: [BUY, SELL]")))
  }

  /////////

  // save
  @Test
  fun `should return 201 and save transaction`() {
    val payload = readJsonFileAsString("$basePath/save-transaction.json")
    val transaction = Transaction(
      "e460cbd3-f6a2-464a-80d9-843e28f01d73",
      "BTC",
      BigDecimal("0.5"),
      BigDecimal("62500"),
      BigDecimal("31250.00"),
      TransactionType.BUY,
      "BINANCE",
      "2024-09-23"
    )

    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns "e460cbd3-f6a2-464a-80d9-843e28f01d73"
    justRun { transactionServiceMock.saveTransaction(transaction) }

    mockMvc.saveTransaction(payload)
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.id", `is`("e460cbd3-f6a2-464a-80d9-843e28f01d73")))
      .andExpect(jsonPath("$.ticker", `is`("BTC")))
      .andExpect(jsonPath("$.quantity", `is`("0.5")))
      .andExpect(jsonPath("$.price", `is`("62500")))
      .andExpect(jsonPath("$.total", `is`("31250.00")))
      .andExpect(jsonPath("$.transactionType", `is`("BUY")))
      .andExpect(jsonPath("$.platform", `is`("BINANCE")))
      .andExpect(jsonPath("$.date", `is`("September 23, 2024")))
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "ABCDEFGHIJKLMNOP"])
  fun `should return 400 when saving transaction with invalid crypto ticker`(cryptoTicker: String) {
    val payload = readJsonFileAsString("$basePath/transaction-invalid-crypto-ticker.json")
      .format(cryptoTicker)

    mockMvc.saveTransaction(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Crypto ticker must have at least 1 character and no more than 15")))
  }

  /////////

  // update
  @Test
  fun `should return 200 and update transaction`() {
    val payload = readJsonFileAsString("$basePath/update-transaction.json")
    val transaction = Transaction(
      "e460cbd3-f6a2-464a-80d9-843e28f01d73",
      "BTC",
      BigDecimal("0.5"),
      BigDecimal("62750"),
      BigDecimal("31375.00"),
      TransactionType.BUY,
      "BINANCE",
      "2024-09-23"
    )

    justRun { transactionServiceMock.updateTransaction(transaction) }

    mockMvc.updateTransaction("e460cbd3-f6a2-464a-80d9-843e28f01d73", payload)
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.id", `is`("e460cbd3-f6a2-464a-80d9-843e28f01d73")))
      .andExpect(jsonPath("$.ticker", `is`("BTC")))
      .andExpect(jsonPath("$.quantity", `is`("0.5")))
      .andExpect(jsonPath("$.price", `is`("62750")))
      .andExpect(jsonPath("$.total", `is`("31375.00")))
      .andExpect(jsonPath("$.transactionType", `is`("BUY")))
      .andExpect(jsonPath("$.platform", `is`("BINANCE")))
      .andExpect(jsonPath("$.date", `is`("September 23, 2024")))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should return 400 when updating transaction with invalid uuid`(transactionId: String) {
    val payload = readJsonFileAsString("$basePath/update-transaction.json")

    mockMvc.updateTransaction(transactionId, payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`(INVALID_TRANSACTION_UUID)))
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "ABCDEFGHIJKLMNOP"])
  fun `should return 400 when updating transaction with invalid crypto ticker`(cryptoTicker: String) {
    val payload = readJsonFileAsString("$basePath/transaction-invalid-crypto-ticker.json")
      .format(cryptoTicker)

    mockMvc.updateTransaction("e460cbd3-f6a2-464a-80d9-843e28f01d73", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Crypto ticker must have at least 1 character and no more than 15")))
  }

  /////////

  @Test
  fun `should return 204 when deleting transaction`() {
    justRun { transactionServiceMock.deleteTransaction("e460cbd3-f6a2-464a-80d9-843e28f01d73") }

    mockMvc.deleteTransaction("e460cbd3-f6a2-464a-80d9-843e28f01d73")
      .andExpect(status().isNoContent)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should return 400 when deleting transaction with invalid id`(transactionId: String) {
    mockMvc.deleteTransaction(transactionId)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`(INVALID_TRANSACTION_UUID)))
  }

  private fun transactions() = listOf(
    Transaction(
      "e460cbd3-f6a2-464a-80d9-843e28f01d73",
      "BTC",
      BigDecimal("1"),
      BigDecimal("60000"),
      BigDecimal("30000.00"),
      TransactionType.BUY,
      "BINANCE",
      "2024-09-15"
    ),
    Transaction(
      "12de547d-714c-4942-bbf5-2947e53dc8c0",
      "ETH",
      BigDecimal("0.5"),
      BigDecimal("2360"),
      BigDecimal("1180.00"),
      TransactionType.SELL,
      "BINANCE",
      "2024-09-15"
    )
  )
}
