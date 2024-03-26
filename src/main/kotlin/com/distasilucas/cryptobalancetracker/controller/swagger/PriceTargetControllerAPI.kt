package com.distasilucas.cryptobalancetracker.controller.swagger

import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.constants.INVALID_PRICE_TARGET_UUID
import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.UUID
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity

@Tag(name = "Price Target Controller", description = "API endpoints for price target management")
interface PriceTargetControllerAPI {

  @Operation(summary = "Retrieve price target")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Price target",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = PriceTargetResponse::class
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
        description = "Price target not found",
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
  fun retrievePriceTarget(
    @UUID(message = INVALID_PRICE_TARGET_UUID) priceTargetId: String
  ): ResponseEntity<PriceTargetResponse>

  @Operation(summary = "Retrieve price targets by page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Price targets by page",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = PagePriceTargetResponse::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No price targets found",
        content = [Content(
          mediaType = "application/json",
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
  fun retrievePriceTargetsByPage(
    @Min(value = 0, message = INVALID_PAGE_NUMBER) page: Int,
  ): ResponseEntity<PagePriceTargetResponse>

  @Operation(summary = "Save price target")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Price target saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = PriceTargetResponse::class
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
  fun savePriceTarget(@Valid priceTargetRequest: PriceTargetRequest): ResponseEntity<PriceTargetResponse>

  @Operation(summary = "Update price target")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Price target updated",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = PriceTargetResponse::class
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
        description = "Price target not found",
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
  fun updatePriceTarget(
    @UUID(message = INVALID_PRICE_TARGET_UUID) priceTargetId: String,
    @Valid priceTargetRequest: PriceTargetRequest
  ): ResponseEntity<PriceTargetResponse>

  @Operation(summary = "Delete price target")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Price target deleted",
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
        description = "Price target not found",
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
  fun deletePriceTarget(
    @UUID(message = INVALID_PRICE_TARGET_UUID) priceTargetId: String
  ): ResponseEntity<Unit>

}
