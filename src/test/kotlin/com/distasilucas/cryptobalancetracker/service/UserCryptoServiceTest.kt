package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_CRYPTO_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import getCoingeckoCrypto
import getCryptoEntity
import getPlatformEntity
import getUserCrypto
import getUserCryptoRequest
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional

class UserCryptoServiceTest {

  private val userCryptoRepositoryMock = mockk<UserCryptoRepository>()
  private val platformServiceMock = mockk<PlatformService>()
  private val cryptoServiceMock = mockk<CryptoService>()
  private val cacheServiceMock = mockk<CacheService>()
  private val _userCryptoServiceMock = mockk<UserCryptoService>()

  private val userCryptoService = UserCryptoService(
    userCryptoRepositoryMock,
    platformServiceMock,
    cryptoServiceMock,
    cacheServiceMock,
    _userCryptoServiceMock
  )

  @Test
  fun `should retrieve user crypto`() {
    val userCrypto = getUserCrypto()
    val crypto = getCryptoEntity()
    val platform = getPlatformEntity()

    every {
      userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000")
    } returns Optional.of(userCrypto)
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns crypto
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platform

    val userCryptoResponse = userCryptoService.retrieveUserCryptoResponseById("123e4567-e89b-12d3-a456-426614174000")

    assertThat(userCryptoResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        UserCryptoResponse(
          id = "123e4567-e89b-12d3-a456-426614174000",
          cryptoName = "Bitcoin",
          quantity = "0.25",
          platform = "BINANCE"
        )
      )
  }

  @Test
  fun `should throw UserCryptoNotFoundException if user crypto does not exists when retrieving user crypto by id`() {
    every { userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000") } returns Optional.empty()

    val exception = assertThrows<UserCryptoNotFoundException> {
      userCryptoService.retrieveUserCryptoResponseById("123e4567-e89b-12d3-a456-426614174000")
    }

    assertThat(exception.message).isEqualTo(USER_CRYPTO_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000"))
  }

  @Test
  fun `should retrieve user cryptos by page`() {
    val userCrypto = getUserCrypto()
    val platform = getPlatformEntity()
    val crypto = getCryptoEntity()

    every { userCryptoRepositoryMock.findAll(PageRequest.of(0, 10)) } returns PageImpl(listOf(userCrypto))
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platform
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns crypto

    val pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0)

    assertThat(pageUserCryptoResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        PageUserCryptoResponse(
          page = 1,
          totalPages = 1,
          hasNextPage = false,
          cryptos = listOf(
            userCrypto.toUserCryptoResponse(
              cryptoName = "Bitcoin",
              platformName = "BINANCE"
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve user cryptos by page with next page`() {
    val userCrypto = getUserCrypto()
    val platform = getPlatformEntity()
    val crypto = getCryptoEntity()
    val userCryptosPage = List(2) { userCrypto }
    val pageImpl = PageImpl(userCryptosPage, PageRequest.of(0, 2), 10L)

    every { userCryptoRepositoryMock.findAll(PageRequest.of(0, 10)) } returns pageImpl
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platform
    every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns crypto

    val pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0)

    assertThat(pageUserCryptoResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        PageUserCryptoResponse(
          page = 1,
          totalPages = 5,
          hasNextPage = true,
          cryptos = listOf(
            userCrypto.toUserCryptoResponse(
              cryptoName = "Bitcoin",
              platformName = "BINANCE"
            ),
            userCrypto.toUserCryptoResponse(
              cryptoName = "Bitcoin",
              platformName = "BINANCE"
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve empty user cryptos for page`() {
    every { userCryptoRepositoryMock.findAll(PageRequest.of(0, 10)) } returns Page.empty()

    val pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0)

    assertThat(pageUserCryptoResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        PageUserCryptoResponse(
          page = 1,
          totalPages = 1,
          hasNextPage = false,
          cryptos = emptyList()
        )
      )
  }

  @Test
  fun `should save user crypto`() {
    val userCryptoRequest = getUserCryptoRequest()
    val coingeckoCrypto = getCoingeckoCrypto()
    val platform = getPlatformEntity()

    val slot = slot<UserCrypto>()
    every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin") } returns coingeckoCrypto
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platform
    every {
      userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
        "bitcoin",
        "123e4567-e89b-12d3-a456-426614174111"
      )
    } returns Optional.empty()
    every { userCryptoRepositoryMock.save(capture(slot)) } answers { slot.captured }
    justRun { cryptoServiceMock.saveCryptoIfNotExists("bitcoin") }
    justRun { cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.INSIGHTS_CACHES) }

    val userCryptoResponse = userCryptoService.saveUserCrypto(userCryptoRequest)

    verify(exactly = 1) { userCryptoRepositoryMock.save(slot.captured) }
    verify(exactly = 1) { cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.INSIGHTS_CACHES) }

    assertThat(userCryptoResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        UserCryptoResponse(
          id = slot.captured.id,
          cryptoName = "Bitcoin",
          quantity = "1",
          platform = "BINANCE"
        )
      )
  }

  @Test
  fun `should throw DuplicatedCryptoPlatFormException when saving user crypto`() {
    val userCryptoRequest = getUserCryptoRequest()
    val coingeckoCrypto = getCoingeckoCrypto()
    val platform = getPlatformEntity()
    val userCrypto = getUserCrypto(
      id = "123e4567-e89b-12d3-a456-426614174312"
    )

    every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin") } returns coingeckoCrypto
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platform
    every {
      userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
        "bitcoin",
        "123e4567-e89b-12d3-a456-426614174111"
      )
    } returns Optional.of(userCrypto)

    val exception = assertThrows<DuplicatedCryptoPlatFormException> {
      userCryptoService.saveUserCrypto(userCryptoRequest)
    }

    verify(exactly = 0) { userCryptoRepositoryMock.save(any()) }

    assertThat(exception.message).isEqualTo(DUPLICATED_CRYPTO_PLATFORM.format("Bitcoin", "BINANCE"))
  }

  @Test
  fun `should update user crypto`() {
    val userCryptoRequest = getUserCryptoRequest(
      quantity = BigDecimal("1.25")
    )
    val userCrypto = getUserCrypto()
    val updatedUserCrypto = getUserCrypto(
      quantity = BigDecimal("1.25")
    )
    val platform = getPlatformEntity()
    val coingeckoCrypto = getCoingeckoCrypto()

    every {
      _userCryptoServiceMock.findByUserCryptoId("123e4567-e89b-12d3-a456-426614174000")
    } returns userCrypto
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platform
    every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin") } returns coingeckoCrypto
    every {
      userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
        "bitcoin",
        "123e4567-e89b-12d3-a456-426614174111"
      )
    } returns Optional.empty()
    every { userCryptoRepositoryMock.save(updatedUserCrypto) } returns updatedUserCrypto
    justRun { cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.INSIGHTS_CACHES, CacheType.GOALS_CACHES) }

    val userCryptoResponse =
      userCryptoService.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)

    verify(exactly = 1) { userCryptoRepositoryMock.save(updatedUserCrypto) }
    verify(exactly = 1) { cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.INSIGHTS_CACHES, CacheType.GOALS_CACHES) }

    assertThat(userCryptoResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        UserCryptoResponse(
          id = "123e4567-e89b-12d3-a456-426614174000",
          cryptoName = "Bitcoin",
          quantity = "1.25",
          platform = "BINANCE"
        )
      )
  }

  @Test
  fun `should update user crypto with different platform`() {
    val userCryptoRequest = getUserCryptoRequest(
      quantity = BigDecimal("1.25"),
      platformId = "123e4567-e89b-12d3-a456-426614174333"
    )
    val userCrypto = getUserCrypto()
    val updatedUserCrypto = getUserCrypto(
      quantity = BigDecimal("1.25"),
      platformId = "123e4567-e89b-12d3-a456-426614174333"
    )
    val platform = Platform(
      id = "123e4567-e89b-12d3-a456-426614174333",
      name = "COINBASE"
    )
    val coingeckoCrypto = getCoingeckoCrypto()

    every {
      _userCryptoServiceMock.findByUserCryptoId("123e4567-e89b-12d3-a456-426614174000")
    } returns userCrypto
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174333")
    } returns platform
    every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin") } returns coingeckoCrypto
    every {
      userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
        "bitcoin",
        "123e4567-e89b-12d3-a456-426614174333"
      )
    } returns Optional.empty()
    every { userCryptoRepositoryMock.save(updatedUserCrypto) } returns updatedUserCrypto
    justRun { cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.INSIGHTS_CACHES, CacheType.GOALS_CACHES) }

    val userCryptoResponse =
      userCryptoService.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)

    verify(exactly = 1) { userCryptoRepositoryMock.save(updatedUserCrypto) }
    verify(exactly = 1) { cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.INSIGHTS_CACHES, CacheType.GOALS_CACHES) }

    assertThat(userCryptoResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        UserCryptoResponse(
          id = "123e4567-e89b-12d3-a456-426614174000",
          cryptoName = "Bitcoin",
          quantity = "1.25",
          platform = "COINBASE"
        )
      )
  }

  @Test
  fun `should throw UserCryptoNotFoundException when updating user crypto`() {
    val userCryptoRequest = getUserCryptoRequest(
      quantity = BigDecimal("1.25")
    )
    val exceptionMessage = USER_CRYPTO_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000")

    every {
      _userCryptoServiceMock.findByUserCryptoId("123e4567-e89b-12d3-a456-426614174000")
    } throws UserCryptoNotFoundException(exceptionMessage)

    val exception = assertThrows<UserCryptoNotFoundException> {
      userCryptoService.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)
    }

    verify(exactly = 0) { userCryptoRepositoryMock.save(any()) }

    assertThat(exception.message).isEqualTo(exceptionMessage)
  }

  @Test
  fun `should throw DuplicatedCryptoPlatFormException when updating user crypto with different platform`() {
    val userCryptoRequest = getUserCryptoRequest(
      quantity = BigDecimal("1.25"),
      platformId = "123e4567-e89b-12d3-a456-426614174333"
    )
    val userCrypto = getUserCrypto()
    val duplicatedUserCrypto = getUserCrypto()
    val platform = Platform(
      id = "123e4567-e89b-12d3-a456-426614174333",
      name = "COINBASE"
    )
    val coingeckoCrypto = getCoingeckoCrypto()

    every {
      _userCryptoServiceMock.findByUserCryptoId("123e4567-e89b-12d3-a456-426614174000")
    } returns userCrypto
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174333")
    } returns platform
    every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin") } returns coingeckoCrypto
    every {
      userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
        "bitcoin",
        "123e4567-e89b-12d3-a456-426614174333"
      )
    } returns Optional.of(duplicatedUserCrypto)

    val exception = assertThrows<DuplicatedCryptoPlatFormException> {
      userCryptoService.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)
    }

    verify(exactly = 0) { userCryptoRepositoryMock.save(any()) }

    assertThat(exception.message).isEqualTo(DUPLICATED_CRYPTO_PLATFORM.format("Bitcoin", "COINBASE"))
  }

  @Test
  fun `should delete user crypto`() {
    every {
      _userCryptoServiceMock.findByUserCryptoId("123e4567-e89b-12d3-a456-426614174000")
    } returns getUserCrypto()
    justRun { userCryptoRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174000") }
    justRun { cryptoServiceMock.deleteCryptoIfNotUsed("bitcoin") }
    justRun {
      cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.GOALS_CACHES, CacheType.INSIGHTS_CACHES)
    }

    userCryptoService.deleteUserCrypto("123e4567-e89b-12d3-a456-426614174000")

    verify(exactly = 1) { userCryptoRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174000") }
    verify(exactly = 1) { cryptoServiceMock.deleteCryptoIfNotUsed("bitcoin") }
    verify(exactly = 1) {
      cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.GOALS_CACHES, CacheType.INSIGHTS_CACHES)
    }
  }

  @Test
  fun `should delete user cryptos`() {
    val userCryptos = UserCrypto(
      id = "123e4567-e89b-12d3-a456-426614174000",
      coingeckoCryptoId = "bitcoin",
      quantity = BigDecimal("1"),
      platformId = "123e4567-e89b-12d3-a456-426614174444"
    )

    justRun { cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.GOALS_CACHES, CacheType.INSIGHTS_CACHES) }
    justRun { userCryptoRepositoryMock.deleteAllById(listOf("123e4567-e89b-12d3-a456-426614174000")) }

    userCryptoService.deleteUserCryptos(listOf(userCryptos))

    verify(exactly = 1) { cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.GOALS_CACHES, CacheType.INSIGHTS_CACHES) }
    verify(exactly = 1) { userCryptoRepositoryMock.deleteAllById(listOf("123e4567-e89b-12d3-a456-426614174000")) }
  }

  @Test
  fun `should throw UserCryptoNotFoundException when deleting user crypto`() {
    val exceptionMessage = USER_CRYPTO_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000")

    every {
      _userCryptoServiceMock.findByUserCryptoId("123e4567-e89b-12d3-a456-426614174000")
    } throws UserCryptoNotFoundException(exceptionMessage)

    val exception = assertThrows<UserCryptoNotFoundException> {
      userCryptoService.deleteUserCrypto("123e4567-e89b-12d3-a456-426614174000")
    }

    verify(exactly = 0) { userCryptoRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174000") }

    assertThat(exception.message)
      .isEqualTo(exceptionMessage)
  }

  @Test
  fun `should find all by coingecko crypto id`() {
    val userCrypto = getUserCrypto()

    every {
      userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin")
    } returns listOf(userCrypto)

    val userCryptos = userCryptoService.findAllByCoingeckoCryptoId("bitcoin")

    assertThat(userCryptos)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          UserCrypto(
            id = "123e4567-e89b-12d3-a456-426614174000",
            coingeckoCryptoId = "bitcoin",
            quantity = BigDecimal("0.25"),
            platformId = "123e4567-e89b-12d3-a456-426614174111"
          )
        )
      )
  }

  @Test
  fun `should find by user crypto id`() {
    val userCrypto = getUserCrypto()

    every {
      userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000")
    } returns Optional.of(userCrypto)

    val response = userCryptoService.findByUserCryptoId("123e4567-e89b-12d3-a456-426614174000")

    assertThat(response)
      .usingRecursiveComparison()
      .isEqualTo(
        UserCrypto(
          id = "123e4567-e89b-12d3-a456-426614174000",
          coingeckoCryptoId = "bitcoin",
          quantity = BigDecimal("0.25"),
          platformId = "123e4567-e89b-12d3-a456-426614174111"
        )
      )
  }

  @Test
  fun `should throw UserCryptoNotFoundException if user crypto does not exists`() {
    every {
      userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000")
    } returns Optional.empty()

    val exception = assertThrows<UserCryptoNotFoundException> {
      userCryptoService.findByUserCryptoId("123e4567-e89b-12d3-a456-426614174000")
    }

    assertThat(exception.message)
      .isEqualTo(USER_CRYPTO_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000"))
  }

  @Test
  fun `should find by coingecko crypto id and platform id`() {
    val userCrypto = getUserCrypto()

    every {
      userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
        "bitcoin", "123e4567-e89b-12d3-a456-426614174111"
      )
    } returns Optional.of(userCrypto)

    val response =
      userCryptoService.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "123e4567-e89b-12d3-a456-426614174111")

    assertThat(response.isPresent)
    assertThat(response.get())
      .usingRecursiveComparison()
      .isEqualTo(
        UserCrypto(
          id = "123e4567-e89b-12d3-a456-426614174000",
          coingeckoCryptoId = "bitcoin",
          quantity = BigDecimal("0.25"),
          platformId = "123e4567-e89b-12d3-a456-426614174111"
        )
      )
  }

  @Test
  fun `should save or update all`() {
    val userCryptos = listOf(getUserCrypto())

    every { userCryptoRepositoryMock.saveAll(userCryptos) } returns userCryptos
    justRun {
      cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.GOALS_CACHES, CacheType.INSIGHTS_CACHES)
    }

    userCryptoService.saveOrUpdateAll(userCryptos)

    verify(exactly = 1) { userCryptoRepositoryMock.saveAll(userCryptos) }
    verify(exactly = 1) {
      cacheServiceMock.invalidate(CacheType.USER_CRYPTOS_CACHES, CacheType.GOALS_CACHES, CacheType.INSIGHTS_CACHES)
    }
  }

  @Test
  fun `should find all user cryptos by platform id`() {
    val userCrypto = getUserCrypto()

    every {
      userCryptoRepositoryMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")
    } returns listOf(userCrypto)

    val userCryptos = userCryptoService.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")

    assertThat(userCryptos)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          UserCrypto(
            id = "123e4567-e89b-12d3-a456-426614174000",
            coingeckoCryptoId = "bitcoin",
            quantity = BigDecimal("0.25"),
            platformId = "123e4567-e89b-12d3-a456-426614174111"
          )
        )
      )
  }

  @Test
  fun `should find all user cryptos`() {
    val userCrypto = getUserCrypto()

    every {
      userCryptoRepositoryMock.findAll()
    } returns listOf(userCrypto)

    val userCryptos = userCryptoService.findAll()

    assertThat(userCryptos)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          UserCrypto(
            id = "123e4567-e89b-12d3-a456-426614174000",
            coingeckoCryptoId = "bitcoin",
            quantity = BigDecimal("0.25"),
            platformId = "123e4567-e89b-12d3-a456-426614174111"
          )
        )
      )
  }
}
