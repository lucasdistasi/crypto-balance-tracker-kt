package com.distasilucas.cryptobalancetracker.scheduler

import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import com.distasilucas.cryptobalancetracker.service.InsightsService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
class DateBalanceScheduler(
  private val dateBalancesRepository: DateBalanceRepository,
  private val insightsService: InsightsService,
  private val clock: Clock
) {

  private val logger = KotlinLogging.logger { }

  @Scheduled(cron = "\${day-balance.cron}")
  fun saveDateBalance() {
    logger.info { "Running cron to save daily balance" }

    val now = LocalDateTime.now(clock).toLocalDate().atTime(23, 59, 59, 0)
    val totalUSDBalance = insightsService.retrieveTotalBalances().map { it.totalUSDBalance }
    val optionalDateBalance = dateBalancesRepository.findDateBalanceByDate(now)

    totalUSDBalance.ifPresent { balance ->
      optionalDateBalance.ifPresentOrElse(
        {
          logger.info {"Updating balance for date $now. Old Balance: ${it.balance}. New balance $balance" }
          dateBalancesRepository.save(DateBalance(it.id, now, balance))
        },
        {
          logger.info { "Saving balance $balance for date $now" }
          dateBalancesRepository.save(DateBalance(date = now, balance = balance))
        }
      )
    }
  }
}
