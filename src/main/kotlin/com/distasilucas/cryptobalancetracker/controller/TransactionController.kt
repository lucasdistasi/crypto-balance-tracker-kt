package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.controller.swagger.TransactionControllerAPI
import com.distasilucas.cryptobalancetracker.entity.TransactionType
import com.distasilucas.cryptobalancetracker.model.request.transaction.TransactionRequest
import com.distasilucas.cryptobalancetracker.model.response.transaction.PageTransactionsResponse
import com.distasilucas.cryptobalancetracker.model.response.transaction.TransactionResponse
import com.distasilucas.cryptobalancetracker.service.TransactionService
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.UUID
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Validated
@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = ["\${allowed-origins}"])
class TransactionController(private val transactionService: TransactionService): TransactionControllerAPI {

  @GetMapping("/latest")
  override fun retrieveLatestTransactions(
    @RequestParam @Min(value = 0, message = INVALID_PAGE_NUMBER) page: Int
  ): ResponseEntity<PageTransactionsResponse> {
    val transactions = transactionService.retrieveLastSixMonthsTransactions(page)
    val pageTransactions = PageTransactionsResponse(page, transactions.totalPages, transactions.content.map { it.toTransactionResponse() })

    return if (pageTransactions.transactions.isEmpty()) ResponseEntity.noContent().build() else ResponseEntity.ok(pageTransactions)
  }

  @GetMapping
  override fun retrieveFilteredTransactions(
    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") dateFrom: LocalDate,
    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") dateTo: LocalDate,
    @RequestParam(required = false) cryptoTicker: String?,
    @RequestParam(required = false) transactionType: TransactionType?,
    @RequestParam(required = false) platform: String?
  ): ResponseEntity<List<TransactionResponse>> {
    val transactionFilters = TransactionFilters(dateFrom, dateTo, cryptoTicker, transactionType, platform)
    val transactions = transactionService.retrieveFilteredTransactions(transactionFilters)

    return if (transactions.isEmpty()) ResponseEntity.noContent().build() else ResponseEntity.ok(transactions.map { it.toTransactionResponse() })
  }

  @PostMapping
  override fun saveTransaction(@RequestBody transactionRequest: TransactionRequest): ResponseEntity<TransactionResponse> {
    val transactionEntity = transactionRequest.toTransactionEntity()
    transactionService.saveTransaction(transactionEntity)

    return ResponseEntity.status(HttpStatus.CREATED)
      .header(HttpHeaders.LOCATION, "/api/v1/transactions/${transactionEntity.id}")
      .body(transactionEntity.toTransactionResponse())
  }

  @PutMapping("/{transactionId}")
  override fun updateTransaction(
    @PathVariable @UUID transactionId: String,
    @RequestBody transactionRequest: TransactionRequest
  ): ResponseEntity<TransactionResponse> {
    val transaction = transactionRequest.toTransactionEntity(transactionId)
    transactionService.updateTransaction(transaction)

    return ResponseEntity.ok(transaction.toTransactionResponse())
  }

  @DeleteMapping("/{transactionId}")
  override fun deleteTransaction(@PathVariable transactionId: String): ResponseEntity<Unit> {
    transactionService.deleteTransaction(transactionId)

    return ResponseEntity.noContent().build()
  }
}

data class TransactionFilters(
  val dateFrom: LocalDate,
  val dateTo: LocalDate,
  val cryptoTicker: String? = null,
  val transactionType: TransactionType? = null,
  val platform: String? = null
)
