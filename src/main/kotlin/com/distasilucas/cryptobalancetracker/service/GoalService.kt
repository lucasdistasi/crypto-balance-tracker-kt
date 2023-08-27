package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_GOAL
import com.distasilucas.cryptobalancetracker.constants.GOAL_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Goal
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
import com.distasilucas.cryptobalancetracker.repository.GoalRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class GoalService(
    val goalRepository: GoalRepository,
    val cryptoService: CryptoService,
    val userCryptoService: UserCryptoService
) {

    private val logger = KotlinLogging.logger { }

    fun retrieveGoalById(goalId: String): GoalResponse {
        logger.info { "Retrieving for goal with $goalId" }

        return goalRepository.findById(goalId)
            .orElseThrow { GoalNotFoundException(GOAL_ID_NOT_FOUND.format(goalId)) }
            .toGoalResponse(id = goalId)
    }

    fun retrieveGoalsForPage(page: Int): PageGoalResponse {
        logger.info { "Retrieving goals for page $page" }

        val pageRequest: Pageable = PageRequest.of(page, 10)
        val entityGoalsPage = goalRepository.findAll(pageRequest)
        val goalsResponse = entityGoalsPage.content.map { it.toGoalResponse(id = it.id) }

        return PageGoalResponse(
            page = page,
            totalPages = entityGoalsPage.totalPages,
            goals = goalsResponse
        )
    }

    fun saveGoal(goalRequest: GoalRequest): GoalResponse {
        val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByName(goalRequest.cryptoName!!)

        val existingGoal = goalRepository.findByCoingeckoCryptoId(coingeckoCrypto.id)

        if (existingGoal.isPresent) {
            throw DuplicatedGoalException(DUPLICATED_GOAL.format(coingeckoCrypto.name))
        }

        val goal = goalRequest.toEntity(coingeckoCrypto.id)
        cryptoService.saveCryptoIfNotExists(goal.coingeckoCryptoId)
        goalRepository.save(goal)
        logger.info { "Saved goal $goal" }

        return goal.toGoalResponse(id = goal.id)
    }

    fun updateGoal(goalId: String, goalRequest: GoalRequest): GoalResponse {
        val goal = goalRepository.findById(goalId)
            .orElseThrow { GoalNotFoundException(GOAL_ID_NOT_FOUND.format(goalId)) }

        val updatedGoal = goal.copy(goalQuantity = goalRequest.goalQuantity!!)

        goalRepository.save(updatedGoal)
        logger.info { "Updated goal. Before: $goal | After: $updatedGoal" }

        return updatedGoal.toGoalResponse(id = updatedGoal.id)
    }

    fun deleteGoal(goalId: String) {
        goalRepository.findById(goalId)
            .ifPresentOrElse({
                goalRepository.deleteById(goalId)
                logger.info { "Deleted goal $it" }
            }, {
                throw GoalNotFoundException(GOAL_ID_NOT_FOUND.format(goalId))
            })
    }

    private fun Goal.toGoalResponse(id: String): GoalResponse {
        val crypto = cryptoService.retrieveCryptoInfoById(coingeckoCryptoId)
        val userCryptos = userCryptoService.findAllByCoingeckoCryptoId(coingeckoCryptoId)
        val actualQuantity = userCryptos.map { it.quantity }
            .fold(BigDecimal.ZERO, BigDecimal::add)
        val progress = getProgress(goalQuantity, actualQuantity)
        val remainingQuantity = getRemainingQuantity(goalQuantity, actualQuantity)
        val moneyNeeded = crypto.getMoneyNeeded(remainingQuantity)

        return toGoalResponse(
            id = id,
            cryptoName = crypto.name,
            actualQuantity = actualQuantity,
            progress = progress,
            remainingQuantity = remainingQuantity,
            moneyNeeded = moneyNeeded
        )
    }

    private fun getRemainingQuantity(goalQuantity: BigDecimal, actualQuantity: BigDecimal): BigDecimal {
        return if (goalQuantity <= actualQuantity) BigDecimal.ZERO else goalQuantity.minus(actualQuantity)
    }

    private fun getProgress(goalQuantity: BigDecimal, actualQuantity: BigDecimal): BigDecimal {
        return if (goalQuantity <= actualQuantity) BigDecimal("100") else actualQuantity.multiply(BigDecimal("100"))
            .divide(goalQuantity, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun Crypto.getMoneyNeeded(remainingQuantity: BigDecimal): BigDecimal {
        return lastKnownPrice.multiply(remainingQuantity).setScale(2, RoundingMode.HALF_UP)
    }
}

class GoalNotFoundException(message: String) : RuntimeException(message)
class DuplicatedGoalException(message: String) : RuntimeException(message)