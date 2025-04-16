package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import com.distasilucas.cryptobalancetracker.repository.PlatformRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class PlatformServiceTest {

  private val platformRepositoryMock = mockk<PlatformRepository>()
  private val userCryptoServiceMock = mockk<UserCryptoService>()
  private val _platformServiceMock = mockk<PlatformService>()
  private val cacheServiceMock = mockk<CacheService>()

  private val platformService = PlatformService(platformRepositoryMock, userCryptoServiceMock, cacheServiceMock, _platformServiceMock)

  @Test
  fun `should return count of platforms`() {
    every { platformRepositoryMock.count() } returns 5

    val countPlatforms = platformService.countPlatforms()

    assertThat(countPlatforms).isEqualTo(5)
  }

  @Test
  fun `should retrieve all platforms by id`() {
    val platformsIds = listOf("e86b1068-8635-4606-83fb-a056040d6c9e", "d6fa4d2a-7760-4e7b-9df3-eabb26a92dd8")
    val platforms = listOf(
      Platform(
        id = "e86b1068-8635-4606-83fb-a056040d6c9e",
        name = "BINANCE"
      ),
      Platform(
        id = "d6fa4d2a-7760-4e7b-9df3-eabb26a92dd8",
        name = "COINBASE"
      )
    )

    every {
      platformRepositoryMock.findAllByIdIn(platformsIds)
    } returns platforms

    val platformsList = platformService.findAllByIds(
      listOf(
        "e86b1068-8635-4606-83fb-a056040d6c9e",
        "d6fa4d2a-7760-4e7b-9df3-eabb26a92dd8"
      )
    )

    assertThat(platformsList)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          Platform(
            id = "e86b1068-8635-4606-83fb-a056040d6c9e",
            name = "BINANCE"
          ),
          Platform(
            id = "d6fa4d2a-7760-4e7b-9df3-eabb26a92dd8",
            name = "COINBASE"
          )
        )
      )
  }

  @Test
  fun `should retrieve platform`() {
    val id = "123e4567-e89b-12d3-a456-426614174000"
    val platformEntity = Platform(id, "BINANCE")

    every { platformRepositoryMock.findById(id) } returns Optional.of(platformEntity)

    val platform = platformService.retrievePlatformById(id)

    assertThat(platform)
      .usingRecursiveComparison()
      .isEqualTo(platformEntity.toPlatformResponse())
  }

  @Test
  fun `should throw PlatformNotFoundException when retrieving platform`() {
    val id = "123e4567-e89b-12d3-a456-426614174000"

    every { platformRepositoryMock.findById(id) } returns Optional.empty()

    val exception = assertThrows<PlatformNotFoundException> { platformService.retrievePlatformById(id) }

    assertThat(exception.message).isEqualTo(PLATFORM_ID_NOT_FOUND.format(id))
  }

  @Test
  fun `should retrieve all platforms`() {
    val id = UUID.randomUUID().toString()
    val platformEntity = Platform(id, "BINANCE")

    every { platformRepositoryMock.findAll() } returns listOf(platformEntity)

    val platforms = platformService.retrieveAllPlatforms()

    assertThat(platforms)
      .hasSize(1)
      .containsExactlyInAnyOrder(Platform(id, "BINANCE"))
  }

  @Test
  fun `should return empty list for all platforms`() {
    every { platformRepositoryMock.findAll() } returns emptyList()

    val platforms = platformService.retrieveAllPlatforms()

    assertThat(platforms).isEmpty()
  }

  @Test
  fun `should save platform`() {
    val platformRequest = PlatformRequest("BINANCE")
    val platformEntity = platformRequest.toEntity()

    val slot = slot<Platform>()
    every { platformRepositoryMock.findByName("BINANCE") } returns null
    every { platformRepositoryMock.save(capture(slot)) } returns platformEntity
    justRun { cacheServiceMock.invalidate(CacheType.PLATFORMS_CACHES) }

    val platformResponse = platformService.savePlatform(platformRequest)

    verify(exactly = 1) { platformRepositoryMock.save(slot.captured) }

    assertThat(platformResponse)
      .usingRecursiveComparison()
      .isEqualTo(PlatformResponse(platformEntity.id, platformEntity.name))
  }

  @Test
  fun `should throw DuplicatedPlatformException when saving platform`() {
    val platformRequest = PlatformRequest("BINANCE")
    val platformEntity = platformRequest.toEntity()
    val existingEntity = platformEntity.copy(id = UUID.randomUUID().toString())

    every { platformRepositoryMock.findByName("BINANCE") } returns existingEntity

    val exception = assertThrows<DuplicatedPlatformException> { platformService.savePlatform(platformRequest) }

    verify(exactly = 0) { platformRepositoryMock.save(platformEntity) }

    assertThat(exception.message).isEqualTo(DUPLICATED_PLATFORM.format("BINANCE"))
  }

  @Test
  fun `should update platform`() {
    val platformRequest = PlatformRequest("OKX")
    val existingPlatform = Platform(UUID.randomUUID().toString(), "BINANCE")
    val id = existingPlatform.id
    val newPlatform = existingPlatform.copy(name = "OKX")

    every { platformRepositoryMock.findByName("OKX") } returns null
    every { _platformServiceMock.retrievePlatformById(existingPlatform.id) } returns newPlatform
    every { platformRepositoryMock.findById(id) } returns Optional.of(existingPlatform)
    every { platformRepositoryMock.save(newPlatform) } returns newPlatform
    justRun { cacheServiceMock.invalidate(CacheType.PLATFORMS_CACHES, CacheType.USER_CRYPTOS_CACHES, CacheType.INSIGHTS_CACHES) }

    val updatedPlatform = platformService.updatePlatform(id, platformRequest)

    verify(exactly = 1) { platformRepositoryMock.save(newPlatform) }

    assertThat(updatedPlatform)
      .usingRecursiveComparison()
      .isEqualTo(PlatformResponse(id, "OKX"))
  }

  @Test
  fun `should throw DuplicatedPlatformException when updating platform`() {
    val platformRequest = PlatformRequest("BINANCE")
    val existingPlatform = Platform(UUID.randomUUID().toString(), "BINANCE")
    val id = existingPlatform.id

    every { platformRepositoryMock.findByName("BINANCE") } returns existingPlatform

    val exception = assertThrows<DuplicatedPlatformException> {
      platformService.updatePlatform(id, platformRequest)
    }

    verify(exactly = 0) { platformRepositoryMock.save(existingPlatform) }

    assertThat(exception.message).isEqualTo(DUPLICATED_PLATFORM.format("BINANCE"))
  }

  @Test
  fun `should throw PlatformNotFoundException when updating platform`() {
    val platformRequest = PlatformRequest("OKX")
    val existingPlatform = Platform(UUID.randomUUID().toString(), "BINANCE")
    val id = existingPlatform.id
    val exceptionMessage = PLATFORM_ID_NOT_FOUND.format(id)

    every { platformRepositoryMock.findByName("OKX") } returns null
    every {
      _platformServiceMock.retrievePlatformById(id)
    } throws PlatformNotFoundException(exceptionMessage)

    val exception = assertThrows<PlatformNotFoundException> {
      platformService.updatePlatform(id, platformRequest)
    }

    verify(exactly = 0) { platformRepositoryMock.save(existingPlatform) }

    assertThat(exception.message).isEqualTo(exceptionMessage)
  }

  @Test
  fun `should delete platform`() {
    val id = UUID.randomUUID().toString()
    val existingPlatform = Platform(id, "BINANCE")
    val userCryptos = UserCrypto(
      id = "123e4567-e89b-12d3-a456-426614174000",
      coingeckoCryptoId = "bitcoin",
      quantity = BigDecimal("1"),
      platformId = id
    )

    every { _platformServiceMock.retrievePlatformById(id) } returns existingPlatform
    every { userCryptoServiceMock.findAllByPlatformId(id) } returns listOf(userCryptos)
    justRun { userCryptoServiceMock.deleteUserCryptos(listOf(userCryptos)) }
    justRun { platformRepositoryMock.delete(existingPlatform) }
    justRun { cacheServiceMock.invalidate(CacheType.PLATFORMS_CACHES, CacheType.INSIGHTS_CACHES) }

    platformService.deletePlatform(id)

    verify(exactly = 1) { platformRepositoryMock.delete(existingPlatform) }
    verify(exactly = 1) { userCryptoServiceMock.deleteUserCryptos(listOf(userCryptos)) }
  }

  @Test
  fun `should throw PlatformNotFoundException when deleting platform`() {
    val id = UUID.randomUUID().toString()
    val existingPlatform = Platform(id, "BINANCE")
    val exceptionMessage = PLATFORM_ID_NOT_FOUND.format(id)

    every {
      _platformServiceMock.retrievePlatformById(id)
    } throws PlatformNotFoundException(exceptionMessage)

    val exception = assertThrows<PlatformNotFoundException> { platformService.deletePlatform(id) }

    verify(exactly = 0) { platformRepositoryMock.delete(existingPlatform) }

    assertThat(exception.message).isEqualTo(exceptionMessage)
  }
}
