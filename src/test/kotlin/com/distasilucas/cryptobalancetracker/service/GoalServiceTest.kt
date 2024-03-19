package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_GOAL
import com.distasilucas.cryptobalancetracker.constants.GOAL_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Goal
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
import com.distasilucas.cryptobalancetracker.repository.GoalRepository
import getCoingeckoCrypto
import getCryptoEntity
import getUserCrypto
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional

class GoalServiceTest {

  private val goalRepositoryMock = mockk<GoalRepository>()
  private val cryptoServiceMock = mockk<CryptoService>()
  private val userCryptoServiceMock = mockk<UserCryptoService>()

  private val goalService = GoalService(goalRepositoryMock, cryptoServiceMock, userCryptoServiceMock)

  @Test
  fun `should retrieve goal by id`() {
    val goalEntity = Goal(
      coingeckoCryptoId = "bitcoin",
      goalQuantity = BigDecimal("1")
    )
    val cryptoEntity = getCryptoEntity()
    val userCrypto = getUserCrypto()

    every { goalRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174111") } returns Optional.of(goalEntity)
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns cryptoEntity
    every { userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(userCrypto)

    val goalResponse = goalService.retrieveGoalById("123e4567-e89b-12d3-a456-426614174111")

    assertThat(goalResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        GoalResponse(
          id = "123e4567-e89b-12d3-a456-426614174111",
          cryptoName = "Bitcoin",
          actualQuantity = "0.25",
          progress = 25f,
          remainingQuantity = "0.75",
          goalQuantity = "1",
          moneyNeeded = "22500.00"
        )
      )
  }

  @Test
  fun `should retrieve completed goal`() {
    val goalEntity = Goal(
      coingeckoCryptoId = "bitcoin",
      goalQuantity = BigDecimal("1")
    )
    val cryptoEntity = getCryptoEntity()
    val userCrypto = getUserCrypto(
      quantity = BigDecimal("1")
    )

    every { goalRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174111") } returns Optional.of(goalEntity)
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns cryptoEntity
    every { userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(userCrypto)

    val goalResponse = goalService.retrieveGoalById("123e4567-e89b-12d3-a456-426614174111")

    assertThat(goalResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        GoalResponse(
          id = "123e4567-e89b-12d3-a456-426614174111",
          cryptoName = "Bitcoin",
          actualQuantity = "1",
          progress = 100f,
          remainingQuantity = "0",
          goalQuantity = "1",
          moneyNeeded = "0.00"
        )
      )
  }

  @Test
  fun `should throw GoalNotFoundException when retrieving goal by id`() {
    every { goalRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174111") } returns Optional.empty()

    val exception = assertThrows<GoalNotFoundException> {
      goalService.retrieveGoalById("123e4567-e89b-12d3-a456-426614174111")
    }

    assertThat(exception.message).isEqualTo(GOAL_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174111"))
  }

  @Test
  fun `should retrieve goals for page`() {
    val pageRequest = PageRequest.of(0, 10)
    val goal = Goal(
      id = "123e4567-e89b-12d3-a456-426614174111",
      coingeckoCryptoId = "bitcoin",
      goalQuantity = BigDecimal("1")
    )
    val cryptoEntity = getCryptoEntity()
    val userCrypto = getUserCrypto()

    every { goalRepositoryMock.findAll(pageRequest) } returns PageImpl(listOf(goal))
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns cryptoEntity
    every { userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(userCrypto)

    val goalsResponse = goalService.retrieveGoalsForPage(0)

    assertThat(goalsResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        PageGoalResponse(
          page = 1,
          totalPages = 1,
          hasNextPage = false,
          goals = listOf(
            GoalResponse(
              id = "123e4567-e89b-12d3-a456-426614174111",
              cryptoName = "Bitcoin",
              actualQuantity = "0.25",
              progress = 25f,
              remainingQuantity = "0.75",
              goalQuantity = "1",
              moneyNeeded = "22500.00"
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve goals for page with next page`() {
    val goal = Goal(
      id = "123e4567-e89b-12d3-a456-426614174111",
      coingeckoCryptoId = "bitcoin",
      goalQuantity = BigDecimal("1")
    )
    val cryptoEntity = getCryptoEntity()
    val userCrypto = getUserCrypto()
    val goalPage = List(2) { goal }
    val pageImpl = PageImpl(goalPage, PageRequest.of(0, 2), 10L)

    every { goalRepositoryMock.findAll(PageRequest.of(0, 10)) } returns pageImpl
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns cryptoEntity
    every { userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(userCrypto)

    val goalsResponse = goalService.retrieveGoalsForPage(0)

    assertThat(goalsResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        PageGoalResponse(
          page = 1,
          totalPages = 5,
          hasNextPage = true,
          goals = listOf(
            GoalResponse(
              id = "123e4567-e89b-12d3-a456-426614174111",
              cryptoName = "Bitcoin",
              actualQuantity = "0.25",
              progress = 25f,
              remainingQuantity = "0.75",
              goalQuantity = "1",
              moneyNeeded = "22500.00"
            ),
            GoalResponse(
              id = "123e4567-e89b-12d3-a456-426614174111",
              cryptoName = "Bitcoin",
              actualQuantity = "0.25",
              progress = 25f,
              remainingQuantity = "0.75",
              goalQuantity = "1",
              moneyNeeded = "22500.00"
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve empty goals for page`() {
    val pageRequest = PageRequest.of(0, 10)

    every { goalRepositoryMock.findAll(pageRequest) } returns Page.empty()

    val goalsResponse = goalService.retrieveGoalsForPage(0)

    assertFalse(goalsResponse.hasNextPage)
    assertThat(goalsResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        PageGoalResponse(
          page = 1,
          totalPages = 1,
          hasNextPage = false,
          goals = emptyList()
        )
      )
  }

  @Test
  fun `should save goal`() {
    val goalRequest = GoalRequest(
      cryptoName = "bitcoin",
      goalQuantity = BigDecimal("1")
    )
    val coingeckoCrypto = getCoingeckoCrypto()
    val cryptoEntity = getCryptoEntity()
    val userCrypto = getUserCrypto()

    val slot = slot<Goal>()
    every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin") } returns coingeckoCrypto
    every { goalRepositoryMock.findByCoingeckoCryptoId("bitcoin") } returns Optional.empty()
    justRun { cryptoServiceMock.saveCryptoIfNotExists("bitcoin") }
    every { goalRepositoryMock.save(capture(slot)) } answers { slot.captured }
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns cryptoEntity
    every { userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(userCrypto)

    val goalResponse = goalService.saveGoal(goalRequest)

    verify(exactly = 1) { goalRepositoryMock.save(slot.captured) }

    assertThat(goalResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        GoalResponse(
          id = slot.captured.id,
          cryptoName = "Bitcoin",
          actualQuantity = "0.25",
          progress = 25f,
          remainingQuantity = "0.75",
          goalQuantity = "1",
          moneyNeeded = "22500.00"
        )
      )
  }

  @Test
  fun `should throw DuplicatedGoalException when saving goal`() {
    val goalRequest = GoalRequest(
      cryptoName = "bitcoin",
      goalQuantity = BigDecimal("1")
    )
    val existingGoal = Goal(
      coingeckoCryptoId = "bitcoin",
      goalQuantity = BigDecimal("0.5")
    )
    val coingeckoCrypto = getCoingeckoCrypto()

    every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin") } returns coingeckoCrypto
    every { goalRepositoryMock.findByCoingeckoCryptoId("bitcoin") } returns Optional.of(existingGoal)

    val exception = assertThrows<DuplicatedGoalException> { goalService.saveGoal(goalRequest) }

    verify(exactly = 0) { goalRepositoryMock.save(any()) }

    assertThat(exception.message).isEqualTo(DUPLICATED_GOAL.format("Bitcoin"))
  }

  @Test
  fun `should update goal`() {
    val goalRequest = GoalRequest(
      cryptoName = "bitcoin",
      goalQuantity = BigDecimal("0.75")
    )
    val goal = Goal(
      id = "123e4567-e89b-12d3-a456-426614174111",
      coingeckoCryptoId = "bitcoin",
      goalQuantity = BigDecimal("0.5")
    )
    val updatedGoal = goal.copy(
      goalQuantity = BigDecimal("0.75")
    )
    val cryptoEntity = getCryptoEntity()
    val userCrypto = getUserCrypto()

    every { goalRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174111") } returns Optional.of(goal)
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns cryptoEntity
    every { userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(userCrypto)
    every { goalRepositoryMock.save(updatedGoal) } returns updatedGoal

    val goalResponse = goalService.updateGoal("123e4567-e89b-12d3-a456-426614174111", goalRequest)

    assertThat(goalResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        GoalResponse(
          id = "123e4567-e89b-12d3-a456-426614174111",
          cryptoName = "Bitcoin",
          actualQuantity = "0.25",
          progress = 33.33f,
          remainingQuantity = "0.50",
          goalQuantity = "0.75",
          moneyNeeded = "15000.00"
        )
      )
  }

  @Test
  fun `should throw GoalNotFoundException when updating goal`() {
    val goalRequest = GoalRequest(
      cryptoName = "bitcoin",
      goalQuantity = BigDecimal("0.75")
    )

    every { goalRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174111") } returns Optional.empty()

    val exception = assertThrows<GoalNotFoundException> {
      goalService.updateGoal("123e4567-e89b-12d3-a456-426614174111", goalRequest)
    }

    assertThat(exception.message).isEqualTo(GOAL_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174111"))
  }

  @Test
  fun `should delete goal`() {
    val goal = Goal(
      id = "123e4567-e89b-12d3-a456-426614174111",
      coingeckoCryptoId = "bitcoin",
      goalQuantity = BigDecimal("1")
    )

    every { goalRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174111") } returns Optional.of(goal)
    justRun { goalRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174111") }
    justRun { cryptoServiceMock.deleteCryptoIfNotUsed("bitcoin") }

    goalService.deleteGoal("123e4567-e89b-12d3-a456-426614174111")

    verify(exactly = 1) { goalRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174111") }
    verify(exactly = 1) { cryptoServiceMock.deleteCryptoIfNotUsed("bitcoin") }
  }

  @Test
  fun `should throw GoalNotFoundException when deleting goal`() {
    every { goalRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174111") } returns Optional.empty()

    val exception = assertThrows<GoalNotFoundException> {
      goalService.deleteGoal("123e4567-e89b-12d3-a456-426614174111")
    }

    verify(exactly = 0) { goalRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174111") }

    assertThat(exception.message).isEqualTo(GOAL_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174111"))
  }
}
