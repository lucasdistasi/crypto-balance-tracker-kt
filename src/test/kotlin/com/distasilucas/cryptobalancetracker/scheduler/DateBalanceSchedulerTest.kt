package com.distasilucas.cryptobalancetracker.scheduler

import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import com.distasilucas.cryptobalancetracker.service.InsightsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Optional
import java.util.UUID

class DateBalanceSchedulerTest {

  private val dateBalancesRepositoryMock = mockk<DateBalanceRepository>()
  private val insightsServiceMock = mockk<InsightsService>()
  private val clockMock = mockk<Clock>()
  private val dateBalanceScheduler = DateBalanceScheduler(dateBalancesRepositoryMock, insightsServiceMock, clockMock)

  @Test
  fun `should save daily balance`() {
    val localDate = LocalDate.of(2024, 3, 17)
    val uuid = "2771242a-8021-48e1-85b2-61967f6558e5"
    val dateBalance = DateBalance(uuid, localDate.toString(), "1000", "918", "0.015")
    val balancesResponse = BalancesResponse("1000", "918", "0.015")

    every { clockMock.instant() } returns localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns localDate.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { insightsServiceMock.retrieveTotalBalances() } returns Optional.of(balancesResponse)
    every { dateBalancesRepositoryMock.findDateBalanceByDate(localDate.toString()) } returns Optional.empty()
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
    val dateBalance = DateBalance(uuid, localDate.toString(), "1500", "1377", "0.022")
    val balancesResponse = BalancesResponse("1500", "1377", "0.022")

    every { clockMock.instant() } returns localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns localDate.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { insightsServiceMock.retrieveTotalBalances() } returns Optional.of(balancesResponse)
    every { dateBalancesRepositoryMock.findDateBalanceByDate(localDate.toString()) } returns Optional.of(dateBalance)
    every { dateBalancesRepositoryMock.save(dateBalance) } returns dateBalance
    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns uuid

    dateBalanceScheduler.saveDateBalance()

    verify(exactly = 1) { dateBalancesRepositoryMock.save(dateBalance) }
  }

  @Test
  fun `should not save nor update daily balance`() {
    val localDate = LocalDate.of(2024, 3, 17)

    every { clockMock.instant() } returns localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns localDate.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { insightsServiceMock.retrieveTotalBalances() } returns Optional.empty()
    every { dateBalancesRepositoryMock.findDateBalanceByDate(localDate.toString()) } returns Optional.empty()

    dateBalanceScheduler.saveDateBalance()

    verify(exactly = 0) { dateBalancesRepositoryMock.save(any()) }
  }
}
