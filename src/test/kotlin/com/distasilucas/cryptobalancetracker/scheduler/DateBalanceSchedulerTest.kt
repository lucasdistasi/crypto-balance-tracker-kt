package com.distasilucas.cryptobalancetracker.scheduler

import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.HomeInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import com.distasilucas.cryptobalancetracker.service.InsightsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class DateBalanceSchedulerTest {

  private val dateBalancesRepositoryMock = mockk<DateBalanceRepository>()
  private val insightsServiceMock = mockk<InsightsService>()
  private val clockMock = mockk<Clock>()
  private val dateBalanceScheduler = DateBalanceScheduler(dateBalancesRepositoryMock, insightsServiceMock, clockMock)

  @Test
  fun `should save daily balance`() {
    val localDate = LocalDate.of(2024, 3, 17)
    val uuid = "2771242a-8021-48e1-85b2-61967f6558e5"
    val dateBalance = DateBalance(uuid, localDate.toString(), "22822.29", "19927.78", "0.25127936")

    every { clockMock.instant() } returns localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns localDate.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { insightsServiceMock.retrieveHomeInsightsResponse() } returns homeInsights()
    every { dateBalancesRepositoryMock.findDateBalanceByDate(localDate.toString()) } returns null
    every { dateBalancesRepositoryMock.save(dateBalance) } returns dateBalance
    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns uuid

    dateBalanceScheduler.saveDateBalance()

    verify(exactly = 1) { dateBalancesRepositoryMock.save(dateBalance) }
  }

  @Test
  fun `should update daily balance`() {
    val localDate = LocalDate.of(2024, 3, 17)
    val uuid = "2771242a-8021-48e1-85b2-61967f6558e5"
    val homeInsights = homeInsights(Balances(FiatBalance("1500", "1377"), "0.022"))
    val dateBalance = DateBalance(uuid, localDate.toString(), "1500", "1377", "0.022")

    every { clockMock.instant() } returns localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns localDate.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { insightsServiceMock.retrieveHomeInsightsResponse() } returns homeInsights
    every { dateBalancesRepositoryMock.findDateBalanceByDate(localDate.toString()) } returns dateBalance
    every { dateBalancesRepositoryMock.save(dateBalance) } returns dateBalance
    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns uuid

    dateBalanceScheduler.saveDateBalance()

    verify(exactly = 1) { dateBalancesRepositoryMock.save(dateBalance) }
  }

  private fun homeInsights(
    balances: Balances = Balances(FiatBalance("22822.29", "19927.78"), "0.25127936")
  ) = HomeInsightsResponse(
    balances,
    "199.92",
    CryptoInfo(
      coingeckoCryptoId = "bitcoin",
      symbol = "btc",
      image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
      price = Price("90824.40", "79305.30"),
      priceChange = PriceChange(10.00)
    )
  )
}
