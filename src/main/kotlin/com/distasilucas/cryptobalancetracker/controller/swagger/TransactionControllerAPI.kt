package com.distasilucas.cryptobalancetracker.controller.swagger

import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.entity.TransactionType
import com.distasilucas.cryptobalancetracker.model.request.transaction.TransactionRequest
import com.distasilucas.cryptobalancetracker.model.response.transaction.PageTransactionsResponse
import com.distasilucas.cryptobalancetracker.model.response.transaction.TransactionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Min
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import java.time.LocalDate

@Tag(name = "Transactions Controller", description = "API endpoints for transactions management")
interface TransactionControllerAPI {

  @Operation(summary = "Retrieve last six months transactions")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Last six months transactions",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = PageTransactionsResponse::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No transactions found",
        content = [Content(
          schema = Schema(
            implementation = Void::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [Content(
          schema = Schema(
            implementation = Void::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. Not yet implemented",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = ProblemDetail::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      )
    ]
  )
  fun retrieveLatestTransactions(
    @Min(value = 0, message = INVALID_PAGE_NUMBER) page: Int
  ): ResponseEntity<PageTransactionsResponse>

  @Operation(summary = "Retrieve transactions for the given filters")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Transactions",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = TransactionResponse::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No transactions found",
        content = [Content(
          schema = Schema(
            implementation = Void::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [Content(
          schema = Schema(
            implementation = Void::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. Not yet implemented",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = ProblemDetail::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      )
    ]
  )
  fun retrieveFilteredTransactions(
    dateFrom: LocalDate,
    dateTo: LocalDate,
    cryptoTicker: String?,
    transactionType: TransactionType?,
    platform: String?
  ): ResponseEntity<List<TransactionResponse>>

  @Operation(summary = "Save transaction")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Transaction saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = TransactionResponse::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [Content(
          schema = Schema(
            implementation = Void::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. Not yet implemented",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = ProblemDetail::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      )
    ]
  )
  fun saveTransaction(transactionRequest: TransactionRequest): ResponseEntity<TransactionResponse>

  @Operation(summary = "Update transaction")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Transaction updated",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = TransactionResponse::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [Content(
          schema = Schema(
            implementation = Void::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. Not yet implemented",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = ProblemDetail::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Transaction not found",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = ProblemDetail::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      )
    ]
  )
  fun updateTransaction(transactionRequest: TransactionRequest, transactionId: String): ResponseEntity<TransactionResponse>

  @Operation(summary = "Delete transaction")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Transaction deleted",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = Unit::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [Content(
          schema = Schema(
            implementation = Void::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. Not yet implemented",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = ProblemDetail::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Transaction not found",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = ProblemDetail::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = ProblemDetail::class
            )
          )
        )]
      )
    ]
  )
  fun deleteTransaction(transactionId: String): ResponseEntity<Unit>
}
