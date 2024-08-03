package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
import com.distasilucas.cryptobalancetracker.service.GoalService
import getGoalResponse
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.math.BigDecimal

class GoalControllerTest {

  private val goalServiceMock = mockk<GoalService>()
  private val goalController = GoalController(goalServiceMock)

  @Test
  fun `should retrieve goal with status 200`() {
    val goalResponse = getGoalResponse()

    every {
      goalServiceMock.retrieveGoalById("123e4567-e89b-12d3-a456-426614174111")
    } returns goalResponse

    val responseEntity = goalController.retrieveGoal("123e4567-e89b-12d3-a456-426614174111")

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.ok(goalResponse))
  }

  @Test
  fun `should retrieve goals for page with status 200`() {
    val pageGoalResponse = PageGoalResponse(
      page = 1,
      totalPages = 1,
      hasNextPage = false,
      goals = listOf(getGoalResponse())
    )

    every {
      goalServiceMock.retrieveGoalsForPage(0)
    } returns pageGoalResponse

    val responseEntity = goalController.retrieveGoalsForPage(0)

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.ok(pageGoalResponse))
  }

  @Test
  fun `should retrieve goals for page with status 204`() {
    val pageGoalResponse = PageGoalResponse(
      page = 1,
      totalPages = 1,
      hasNextPage = false,
      goals = emptyList()
    )

    every {
      goalServiceMock.retrieveGoalsForPage(0)
    } returns pageGoalResponse

    val responseEntity = goalController.retrieveGoalsForPage(0)

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.noContent().build<PageGoalResponse>())
  }

  @Test
  fun `should save goal and return 200`() {
    val goalRequest = GoalRequest(
      cryptoName = "Bitcoin",
      goalQuantity = BigDecimal("1")
    )
    val goalResponse = getGoalResponse()

    every {
      goalServiceMock.saveGoal(goalRequest)
    } returns goalResponse

    val responseEntity = goalController.saveGoal(goalRequest)

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.ok(goalResponse))
  }

  @Test
  fun `should update goal and return 200`() {
    val goalRequest = GoalRequest(
      cryptoName = "Bitcoin",
      goalQuantity = BigDecimal("1.5")
    )
    val goalResponse = getGoalResponse(
      goalQuantity = BigDecimal("1.5")
    )

    every {
      goalServiceMock.updateGoal("123e4567-e89b-12d3-a456-426614174111", goalRequest)
    } returns goalResponse

    val responseEntity = goalController.updateGoal("123e4567-e89b-12d3-a456-426614174111", goalRequest)

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.ok(goalResponse))
  }

  @Test
  fun `should delete goal and return 200`() {
    justRun { goalServiceMock.deleteGoal("123e4567-e89b-12d3-a456-426614174111") }

    val responseEntity = goalController.deleteGoal("123e4567-e89b-12d3-a456-426614174111")

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.noContent().build<Unit>())
  }
}
