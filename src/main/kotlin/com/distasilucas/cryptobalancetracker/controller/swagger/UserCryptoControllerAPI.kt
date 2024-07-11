package com.distasilucas.cryptobalancetracker.controller.swagger

import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_UUID
import com.distasilucas.cryptobalancetracker.model.request.crypto.TransferCryptoRequest
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.TransferCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
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
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "UserCrypto Controller", description = "API endpoints for user cryptos management")
interface UserCryptoControllerAPI {

  @Operation(summary = "Retrieve information for the given user crypto id")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User crypto information",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = UserCryptoResponse::class
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
        description = "User crypto not found",
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
  fun retrieveUserCrypto(
    @UUID(message = USER_CRYPTO_ID_UUID) userCryptoId: String
  ): ResponseEntity<UserCryptoResponse>

  @Operation(summary = "Retrieves user cryptos by page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User cryptos by page",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = UserCryptoResponse::class
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No user cryptos found",
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
  fun retrieveUserCryptosForPage(@Min(value = 0, message = INVALID_PAGE_NUMBER) page: Int): ResponseEntity<PageUserCryptoResponse>

  @Operation(summary = "Save user crypto")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User crypto saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = UserCryptoResponse::class
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
  fun saveUserCrypto(@Valid @RequestBody userCryptoRequest: UserCryptoRequest): ResponseEntity<UserCryptoResponse>

  @Operation(summary = "Update user crypto")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User crypto updated",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = UserCryptoResponse::class
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
        description = "User crypto not found",
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
  fun updateUserCrypto(
    @UUID(message = USER_CRYPTO_ID_UUID) userCryptoId: String,
    @Valid userCryptoRequest: UserCryptoRequest
  ): ResponseEntity<UserCryptoResponse>

  @Operation(summary = "Delete user crypto")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "User crypto deleted",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = UserCryptoResponse::class
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
        description = "User crypto not found",
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
  fun deleteUserCrypto(
    @UUID(message = USER_CRYPTO_ID_UUID) userCryptoId: String
  ): ResponseEntity<Unit>

  @Operation(summary = "Transfer user crypto")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "User crypto transferred",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = TransferCryptoResponse::class
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
        description = "User crypto not found, Platform not found",
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
  fun transferUserCrypto(
    @Valid transferCryptoRequest: TransferCryptoRequest
  ): ResponseEntity<TransferCryptoResponse>
}
