package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_CRYPTO_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import getCoingeckoCrypto
import getCryptoEntity
import getPlatformResponse
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

    private val userCryptoService = UserCryptoService(userCryptoRepositoryMock, platformServiceMock, cryptoServiceMock)

    @Test
    fun `should retrieve user crypto`() {
        val userCrypto = getUserCrypto()
        val crypto = getCryptoEntity()
        val platform = getPlatformResponse()

        every {
            userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000")
        } returns Optional.of(userCrypto)
        every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns crypto
        every { platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111") } returns platform

        val userCryptoResponse = userCryptoService.retrieveUserCryptoById("123e4567-e89b-12d3-a456-426614174000")

        assertThat(userCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                UserCryptoResponse(
                    id = "123e4567-e89b-12d3-a456-426614174000",
                    cryptoName = "Bitcoin",
                    quantity = BigDecimal("0.25"),
                    platform = "BINANCE"
                )
            )
    }

    @Test
    fun `should throw UserCryptoNotFoundException if user crypto does not exists`() {
        every { userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000") } returns Optional.empty()

        val exception = assertThrows<UserCryptoNotFoundException> {
            userCryptoService.retrieveUserCryptoById("123e4567-e89b-12d3-a456-426614174000")
        }

        assertThat(exception.message).isEqualTo(USER_CRYPTO_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000"))
    }

    @Test
    fun `should retrieve user cryptos by page`() {
        val userCrypto = getUserCrypto()
        val platformResponse = getPlatformResponse()
        val crypto = getCryptoEntity()

        every { userCryptoRepositoryMock.findAll(PageRequest.of(0, 10)) } returns PageImpl(listOf(userCrypto))
        every { platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111") } returns platformResponse
        every { cryptoServiceMock.retrieveCryptoInfoById("bitcoin") } returns crypto

        val pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0)

        assertThat(pageUserCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                PageUserCryptoResponse(
                    page = 0,
                    totalPages = 1,
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
    fun `should retrieve empty user cryptos for page`() {
        every { userCryptoRepositoryMock.findAll(PageRequest.of(0, 10)) } returns Page.empty()

        val pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0)

        assertThat(pageUserCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                PageUserCryptoResponse(
                    page = 0,
                    totalPages = 1,
                    cryptos = emptyList()
                )
            )
    }

    @Test
    fun `should save user crypto`() {
        val userCryptoRequest = getUserCryptoRequest()
        val coingeckoCrypto = getCoingeckoCrypto()
        val platformResponse = getPlatformResponse()

        val slot = slot<UserCrypto>()
        every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin") } returns coingeckoCrypto
        every { platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111") } returns platformResponse
        every {
            userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "123e4567-e89b-12d3-a456-426614174111"
            )
        } returns Optional.empty()
        every { userCryptoRepositoryMock.save(capture(slot)) } answers { slot.captured }
        justRun { cryptoServiceMock.saveCryptoIfNotExists("bitcoin") }

        val userCryptoResponse = userCryptoService.saveUserCrypto(userCryptoRequest)

        verify(exactly = 1) { userCryptoRepositoryMock.save(slot.captured) }

        assertThat(userCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                UserCryptoResponse(
                    id = slot.captured.id,
                    cryptoName = "Bitcoin",
                    quantity = BigDecimal("1"),
                    platform = "BINANCE"
                )
            )
    }

    @Test
    fun `should throw DuplicatedCryptoPlatFormException when saving user crypto`() {
        val userCryptoRequest = getUserCryptoRequest()
        val coingeckoCrypto = getCoingeckoCrypto()
        val platformResponse = getPlatformResponse()
        val userCrypto = getUserCrypto(
            id = "123e4567-e89b-12d3-a456-426614174312"
        )

        every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin") } returns coingeckoCrypto
        every { platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111") } returns platformResponse
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
        val platformResponse = getPlatformResponse()
        val coingeckoCrypto = getCoingeckoCrypto()

        every {
            userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000")
        } returns Optional.of(userCrypto)
        every { platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111") } returns platformResponse
        every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin") } returns coingeckoCrypto
        every {
            userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "123e4567-e89b-12d3-a456-426614174111"
            )
        } returns Optional.empty()
        every { userCryptoRepositoryMock.save(updatedUserCrypto) } returns updatedUserCrypto

        val userCryptoResponse =
            userCryptoService.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)

        verify(exactly = 1) { userCryptoRepositoryMock.save(updatedUserCrypto) }

        assertThat(userCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                UserCryptoResponse(
                    id = "123e4567-e89b-12d3-a456-426614174000",
                    cryptoName = "Bitcoin",
                    quantity = BigDecimal("1.25"),
                    platform = "BINANCE"
                )
            )
    }

    @Test
    fun `should throw UserCryptoNotFoundException when updating user crypto`() {
        val userCryptoRequest = getUserCryptoRequest(
            quantity = BigDecimal("1.25")
        )

        every {
            userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000")
        } returns Optional.empty()

        val exception = assertThrows<UserCryptoNotFoundException> {
            userCryptoService.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)
        }

        verify(exactly = 0) { userCryptoRepositoryMock.save(any()) }

        assertThat(exception.message).isEqualTo(USER_CRYPTO_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000"))
    }

    @Test
    fun `should throw DuplicatedCryptoPlatFormException when updating user crypto`() {
        val userCryptoRequest = getUserCryptoRequest(
            quantity = BigDecimal("1.25")
        )
        val userCrypto = getUserCrypto()
        val duplicatedUserCrypto = getUserCrypto()
        val platformResponse = getPlatformResponse()
        val coingeckoCrypto = getCoingeckoCrypto()

        every {
            userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000")
        } returns Optional.of(userCrypto)
        every { platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111") } returns platformResponse
        every { cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin") } returns coingeckoCrypto
        every {
            userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "123e4567-e89b-12d3-a456-426614174111"
            )
        } returns Optional.of(duplicatedUserCrypto)

        val exception = assertThrows<DuplicatedCryptoPlatFormException> {
            userCryptoService.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)
        }

        verify(exactly = 0) { userCryptoRepositoryMock.save(any()) }

        assertThat(exception.message).isEqualTo(DUPLICATED_CRYPTO_PLATFORM.format("Bitcoin", "BINANCE"))
    }

    @Test
    fun `should delete user crypto`() {
        val userCrypto = getUserCrypto()

        every {
            userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000")
        } returns Optional.of(userCrypto)
        justRun { userCryptoRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174000") }

        userCryptoService.deleteUserCrypto("123e4567-e89b-12d3-a456-426614174000")

        verify(exactly = 1) { userCryptoRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174000") }
    }

    @Test
    fun `should throw UserCryptoNotFoundException when deleting user crypto`() {
        every { userCryptoRepositoryMock.findById("123e4567-e89b-12d3-a456-426614174000") } returns Optional.empty()

        val exception = assertThrows<UserCryptoNotFoundException> {
            userCryptoService.deleteUserCrypto("123e4567-e89b-12d3-a456-426614174000")
        }

        verify(exactly = 0) { userCryptoRepositoryMock.deleteById("123e4567-e89b-12d3-a456-426614174000") }

        assertThat(exception.message).isEqualTo(USER_CRYPTO_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000"))
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
}