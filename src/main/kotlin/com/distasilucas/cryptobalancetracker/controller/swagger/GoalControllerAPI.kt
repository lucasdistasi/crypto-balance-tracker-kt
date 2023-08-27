package com.distasilucas.cryptobalancetracker.controller.swagger

import com.distasilucas.cryptobalancetracker.constants.INVALID_GOAL_UUID
import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
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

@Tag(name = "Goal Controller", description = "API endpoints for goal management")
interface GoalControllerAPI {

    @Operation(summary = "Retrieve information for the given goal")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Goal information",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = GoalResponse::class
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
                responseCode = "404",
                description = "Goal not found",
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
    fun retrieveGoal(@UUID(message = INVALID_GOAL_UUID) goalId: String): ResponseEntity<GoalResponse>

    @Operation(summary = "Retrieve goals by page")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Goals by page",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = PageGoalResponse::class
                    )
                )]
            ),
            ApiResponse(
                responseCode = "204",
                description = "No goals found",
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
    fun retrieveGoalsForPage(@Min(value = 0, message = INVALID_PAGE_NUMBER) page: Int): ResponseEntity<PageGoalResponse>

    @Operation(summary = "Save goal")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Goal saved",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = GoalResponse::class
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
    fun saveGoal(@Valid goalRequest: GoalRequest): ResponseEntity<GoalResponse>

    @Operation(summary = "Update goal")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Goal updated",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = GoalResponse::class
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
                responseCode = "404",
                description = "Goal not found",
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
    fun updateGoal(
        @UUID(message = INVALID_GOAL_UUID) goalId: String,
        @Valid goalRequest: GoalRequest
    ): ResponseEntity<GoalResponse>

    @Operation(summary = "Delete goal")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Goal deleted",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = GoalResponse::class
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
                responseCode = "404",
                description = "Goal not found",
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
    fun deleteGoal(@UUID(message = INVALID_GOAL_UUID) goalId: String): ResponseEntity<Unit>
}