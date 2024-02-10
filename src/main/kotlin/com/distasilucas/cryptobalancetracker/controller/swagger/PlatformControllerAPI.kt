package com.distasilucas.cryptobalancetracker.controller.swagger

import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity

@Tag(name = "Platform Controller", description = "API endpoints for platform management")
interface PlatformControllerAPI {

  @Operation(summary = "Retrieve number of platforms")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Number of platforms",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = Long::class
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
  fun countPlatforms(): ResponseEntity<Long>

  @Operation(summary = "Retrieve platform")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Platform information",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = PlatformResponse::class
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
        description = "Platform not found",
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
  fun retrievePlatform(@UUID(message = PLATFORM_ID_UUID) platformId: String): ResponseEntity<PlatformResponse>

  @Operation(summary = "Retrieve all platforms")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Platforms",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = PlatformResponse::class
            )
          )
        )]
      ),
      ApiResponse(
        responseCode = "204",
        description = "No platforms saved",
        content = [Content(
          mediaType = "application/json",
          schema = Schema(
            implementation = Unit::class
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
  fun retrieveAllPlatforms(): ResponseEntity<List<PlatformResponse>>

  @Operation(summary = "Save platform")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Platform saved",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = PlatformResponse::class
            )
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
  fun savePlatform(@Valid platformRequest: PlatformRequest): ResponseEntity<PlatformResponse>

  @Operation(summary = "Update platform")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Platform updated",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = PlatformResponse::class
            )
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
        description = "Platform not found",
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
  fun updatePlatform(
    @UUID(message = PLATFORM_ID_UUID) platformId: String,
    @Valid platformRequest: PlatformRequest
  ): ResponseEntity<PlatformResponse>

  @Operation(summary = "Delete platform")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Platform deleted",
        content = [Content(
          mediaType = "application/json",
          array = ArraySchema(
            schema = Schema(
              implementation = Unit::class
            )
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
        description = "Platform not found",
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
  fun deletePlatform(@UUID(message = PLATFORM_ID_UUID) platformId: String): ResponseEntity<Unit>
}
