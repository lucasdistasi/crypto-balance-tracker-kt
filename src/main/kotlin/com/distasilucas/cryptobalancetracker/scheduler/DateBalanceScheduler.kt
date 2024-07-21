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
    val totalBalances = insightsService.retrieveTotalBalances()
    val optionalDateBalance = dateBalancesRepository.findDateBalanceByDate(now)

    totalBalances.ifPresent { balances ->
      optionalDateBalance.ifPresentOrElse(
        {
          logger.info {"Updating balances for date $now. Old Balance: $it. New balances $balances" }
          dateBalancesRepository.save(DateBalance(it.id, now, balances.totalUSDBalance, balances.totalEURBalance, balances.totalBTCBalance))
        },
        {
          logger.info { "Saving balances $balances for date $now" }
          dateBalancesRepository.save(
            DateBalance(
              date = now,
              usdBalance = balances.totalUSDBalance,
              eurBalance = balances.totalEURBalance,
              btcBalance = balances.totalBTCBalance
            )
          )
        }
      )
    }
  }
}
