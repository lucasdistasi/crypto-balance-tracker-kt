package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.INVALID_GOAL_UUID
import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.controller.swagger.GoalControllerAPI
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
import com.distasilucas.cryptobalancetracker.service.GoalService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.UUID
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

@Validated
@RestController
@RequestMapping("/api/v1/goals")
@CrossOrigin(origins = ["\${allowed-origins}"])
class GoalController(private val goalService: GoalService) : GoalControllerAPI {

  @GetMapping("/{goalId}")
  override fun retrieveGoal(@PathVariable @UUID(message = INVALID_GOAL_UUID) goalId: String): ResponseEntity<GoalResponse> {
    val goal = goalService.retrieveGoalById(goalId)

    return ResponseEntity.ok(goal)
  }

  @GetMapping
  override fun retrieveGoalsForPage(
    @RequestParam @Min(value = 0, message = INVALID_PAGE_NUMBER) page: Int
  ): ResponseEntity<PageGoalResponse> {
    val pageGoalResponse = goalService.retrieveGoalsForPage(page)

    return if (pageGoalResponse.goals.isEmpty())
      ResponseEntity.noContent().build() else ResponseEntity.ok(pageGoalResponse)
  }

  @PostMapping
  override fun saveGoal(@RequestBody @Valid goalRequest: GoalRequest): ResponseEntity<GoalResponse> {
    val goal = goalService.saveGoal(goalRequest)

    return ResponseEntity.ok(goal)
  }

  @PutMapping("/{goalId}")
  override fun updateGoal(
    @PathVariable @UUID(message = INVALID_GOAL_UUID) goalId: String,
    @Valid @RequestBody goalRequest: GoalRequest
  ): ResponseEntity<GoalResponse> {
    val goal = goalService.updateGoal(goalId, goalRequest)

    return ResponseEntity.ok(goal)
  }

  @DeleteMapping("/{goalId}")
  override fun deleteGoal(@PathVariable @UUID(message = INVALID_GOAL_UUID) goalId: String): ResponseEntity<Unit> {
    goalService.deleteGoal(goalId)

    return ResponseEntity.ok().build()
  }
}
