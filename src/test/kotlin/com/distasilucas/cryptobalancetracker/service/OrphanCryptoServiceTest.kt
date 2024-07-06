package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.Goal
import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.repository.GoalRepository
import com.distasilucas.cryptobalancetracker.repository.PriceTargetRepository
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class OrphanCryptoServiceTest {

  private val userCryptoRepositoryMock = mockk<UserCryptoRepository>()
  private val goalRepositoryMock = mockk<GoalRepository>()
  private val priceTargetRepositoryMock = mockk<PriceTargetRepository>()

  private val orphanCryptoService = OrphanCryptoService(userCryptoRepositoryMock, goalRepositoryMock, priceTargetRepositoryMock)

  @Test
  fun `should return true if crypto it's not being used`() {
    every { userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns emptyList()
    every { goalRepositoryMock.findByCoingeckoCryptoId("bitcoin") } returns Optional.empty()
    every { priceTargetRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns emptyList()

    val result = orphanCryptoService.isCryptoOrphan("bitcoin")

    assertTrue(result)
  }

  @Test
  fun `should return false if crypto it's used in user cryptos document`() {
    val userCrypto = UserCrypto(
      coingeckoCryptoId = "bitcoin",
      quantity = BigDecimal("1"),
      platformId = UUID.randomUUID().toString()
    )

    every { userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(userCrypto)
    every { goalRepositoryMock.findByCoingeckoCryptoId("bitcoin") } returns Optional.empty()
    every { priceTargetRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns emptyList()

    val result = orphanCryptoService.isCryptoOrphan("bitcoin")

    assertFalse(result)
  }

  @Test
  fun `should return false if crypto it's used in goals document`() {
    val goalEntity = Goal(
      coingeckoCryptoId = "bitcoin",
      goalQuantity = BigDecimal("1")
    )

    every { userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns emptyList()
    every { goalRepositoryMock.findByCoingeckoCryptoId("bitcoin") } returns Optional.of(goalEntity)
    every { priceTargetRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns emptyList()

    val result = orphanCryptoService.isCryptoOrphan("bitcoin")

    assertFalse(result)
  }

  @Test
  fun `should return false if crypto it's used in price targets document`() {
    val priceTargetEntity = PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", BigDecimal("120000"))

    every { userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns emptyList()
    every { goalRepositoryMock.findByCoingeckoCryptoId("bitcoin") } returns Optional.empty()
    every { priceTargetRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(priceTargetEntity)

    val result = orphanCryptoService.isCryptoOrphan("bitcoin")

    assertFalse(result)
  }

}
