package com.distasilucas.cryptobalancetracker.scheduler

import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import com.distasilucas.cryptobalancetracker.service.InsightsService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate

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

    val today = LocalDate.now(clock).toString()
    val totalBalances = insightsService.retrieveTotalBalances()
    val optionalDateBalance = dateBalancesRepository.findDateBalanceByDate(today)

    optionalDateBalance.ifPresentOrElse(
      {
        val updatedDateBalance = DateBalance(it.id, today, totalBalances)
        logger.info {"Updating balances for date $today. Old Balance: $it. New balances $updatedDateBalance" }
        dateBalancesRepository.save(updatedDateBalance)
      },
      {
        logger.info { "Saving balances $totalBalances for date $today" }
        dateBalancesRepository.save(
          DateBalance(
            date = today,
            usdBalance = totalBalances.totalUSDBalance,
            eurBalance = totalBalances.totalEURBalance,
            btcBalance = totalBalances.totalBTCBalance)
        )
      }
    )
  }
}
