package com.distasilucas.cryptobalancetracker.controller.swagger

import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.SortBy
import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.UUID
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity

@Tag(name = "Insights Controller", description = "API endpoints for retrieving insights")
interface InsightsControllerAPI {

  @Operation(summary = "Retrieve total balances in USD, BTC and EUR")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Total Balances",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = BalancesResponse::class
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
  fun retrieveTotalBalances(): ResponseEntity<BalancesResponse>

  @Operation(summary = "Retrieve day balances for the provided date range")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Days balances",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = DatesBalanceResponse::class
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
  fun retrieveDatesBalances(dateRange: DateRange): ResponseEntity<DatesBalanceResponse>

  @Operation(summary = "Retrieves information of each INDIVIDUAL user crypto, like the total balance, information about the crypto, in which platforms it's stored")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Cryptos Information",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = PageUserCryptosInsightsResponse::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = Void::class
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
  fun retrieveUserCryptosInsights(
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    page: Int,
    sortBy: SortBy = SortBy.PERCENTAGE,
    sortType: SortType = SortType.DESC
  ): ResponseEntity<PageUserCryptosInsightsResponse>

  @Operation(summary = "Retrieve insights balances for all user cryptos")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User cryptos balances insights",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = BalancesChartResponse::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = Void::class
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
  fun retrieveCryptosBalancesInsights(): ResponseEntity<List<BalancesChartResponse>>

  @Operation(summary = "Retrieve insights balances for all platforms")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Platforms balances insights",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = BalancesChartResponse::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = Void::class
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
  fun retrievePlatformsBalancesInsights(): ResponseEntity<List<BalancesChartResponse>>

  @Operation(summary = "Retrieve user cryptos insights for the given coingecko crypto id")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User cryptos insights",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = CryptoInsightResponse::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = Void::class
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
  fun retrieveCryptoInsights(coingeckoCryptoId: String): ResponseEntity<CryptoInsightResponse>

  @Operation(summary = "Retrieve insights for the given platform")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Platform insights",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = PlatformInsightsResponse::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = Void::class
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
  fun retrievePlatformInsights(
    @UUID(message = PLATFORM_ID_UUID)
    platformId: String
  ): ResponseEntity<PlatformInsightsResponse>
}
