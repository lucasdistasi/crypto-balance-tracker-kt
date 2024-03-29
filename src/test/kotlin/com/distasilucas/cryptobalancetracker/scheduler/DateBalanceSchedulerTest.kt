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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class DateBalanceSchedulerTest {

  private val dateBalancesRepositoryMock = mockk<DateBalanceRepository>()
  private val insightsServiceMock = mockk<InsightsService>()
  private val clockMock = mockk<Clock>()
  private val dateBalanceScheduler = DateBalanceScheduler(dateBalancesRepositoryMock, insightsServiceMock, clockMock)

  @Test
  fun `should save daily balance`() {
    val localDateTime = LocalDateTime.of(2024, 3, 17, 23, 59, 59, 0)
    val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))
    val uuid = "2771242a-8021-48e1-85b2-61967f6558e5"

    every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns zonedDateTime.zone
    every { insightsServiceMock.retrieveTotalBalances() } returns
      Optional.of(BalancesResponse("1000", "918", "0.015"))
    every { dateBalancesRepositoryMock.findDateBalanceByDate(localDateTime) } returns Optional.empty()
    every { dateBalancesRepositoryMock.save(DateBalance(uuid, localDateTime, "1000")) } returns DateBalance(uuid, localDateTime, "1000")
    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns uuid

    dateBalanceScheduler.saveDateBalance()

    verify(exactly = 1) { dateBalancesRepositoryMock.save(DateBalance(uuid, localDateTime, "1000")) }
  }

  @Test
  fun `should update daily balance`() {
    val localDateTime = LocalDateTime.of(2024, 3, 17, 23, 59, 59, 0)
    val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))
    val uuid = "2771242a-8021-48e1-85b2-61967f6558e5"

    every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns zonedDateTime.zone
    every { insightsServiceMock.retrieveTotalBalances() } returns
      Optional.of(BalancesResponse("1500", "1377", "0.022"))
    every { dateBalancesRepositoryMock.findDateBalanceByDate(localDateTime) } returns
      Optional.of(DateBalance(uuid, localDateTime, "1000"))
    every { dateBalancesRepositoryMock.save(DateBalance(uuid, localDateTime, "1500")) } returns DateBalance(uuid, localDateTime, "1500")
    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns uuid

    dateBalanceScheduler.saveDateBalance()

    verify(exactly = 1) { dateBalancesRepositoryMock.save(DateBalance(uuid, localDateTime, "1500")) }
  }

  @Test
  fun `should not save nor update daily balance`() {
    val localDateTime = LocalDateTime.of(2024, 3, 17, 23, 59, 59, 0)
    val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))

    every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns zonedDateTime.zone
    every { insightsServiceMock.retrieveTotalBalances() } returns Optional.empty()
    every { dateBalancesRepositoryMock.findDateBalanceByDate(localDateTime) } returns Optional.empty()

    dateBalanceScheduler.saveDateBalance()

    verify(exactly = 0) { dateBalancesRepositoryMock.save(any()) }
  }
}
