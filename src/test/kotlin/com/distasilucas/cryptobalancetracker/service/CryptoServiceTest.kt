package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Goal
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository
import com.distasilucas.cryptobalancetracker.repository.GoalRepository
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import getCoingeckoCryptoInfo
import getCryptoEntity
import getMarketData
import getUserCrypto
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Optional

class CryptoServiceTest {

    private val coingeckoServiceMock = mockk<CoingeckoService>()
    private val cryptoRepositoryMock = mockk<CryptoRepository>()
    private val userCryptoRepositoryMock = mockk<UserCryptoRepository>()
    private val goalRepositoryMock = mockk<GoalRepository>()
    private val clockMock = mockk<Clock>()

    private val cryptoService = CryptoService(
        coingeckoServiceMock,
        cryptoRepositoryMock,
        userCryptoRepositoryMock,
        goalRepositoryMock,
        clockMock
    )

    @Test
    fun `should retrieve crypto info by id`() {
        val localDateTime = LocalDateTime.now()
        val cryptoEntity = getCryptoEntity(lastUpdatedAt = localDateTime)

        every { cryptoRepositoryMock.findById("bitcoin") } returns Optional.of(cryptoEntity)

        val crypto = cryptoService.retrieveCryptoInfoById("bitcoin")

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(
                Crypto(
                    id = "bitcoin",
                    name = "Bitcoin",
                    ticker = "btc",
                    lastKnownPrice = BigDecimal("30000"),
                    lastKnownPriceInEUR = BigDecimal("27000"),
                    lastKnownPriceInBTC = BigDecimal("1"),
                    circulatingSupply = BigDecimal("19000000"),
                    maxSupply = BigDecimal("21000000"),
                    lastUpdatedAt = localDateTime
                )
            )
    }

    @Test
    fun `should call retrieveCryptoInfo and save crypto when retrieving crypto info by id`() {
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val coingeckoCryptoInfo = getCoingeckoCryptoInfo()
        val cryptoEntity = getCryptoEntity(
            lastUpdatedAt = localDateTime
        )

        every { cryptoRepositoryMock.findById("bitcoin") } returns Optional.empty()
        every { coingeckoServiceMock.retrieveCryptoInfo("bitcoin") } returns coingeckoCryptoInfo
        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every { cryptoRepositoryMock.save(cryptoEntity) } returns cryptoEntity

        val crypto = cryptoService.retrieveCryptoInfoById("bitcoin")

        verify(exactly = 1) { cryptoRepositoryMock.save(crypto) }

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(
                Crypto(
                    id = "bitcoin",
                    name = "Bitcoin",
                    ticker = "btc",
                    circulatingSupply = BigDecimal("19000000"),
                    lastKnownPrice = BigDecimal("30000"),
                    lastKnownPriceInBTC = BigDecimal("1"),
                    lastKnownPriceInEUR = BigDecimal("27000"),
                    maxSupply = BigDecimal("21000000"),
                    lastUpdatedAt = localDateTime
                )
            )
    }

    @Test
    fun `should call retrieveCryptoInfo and save crypto with ZERO as max supply when retrieving crypto info by id`() {
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val marketData = getMarketData(maxSupply = null)
        val coingeckoCryptoInfo = getCoingeckoCryptoInfo(marketData = marketData)

        val cryptoEntity = getCryptoEntity(
            maxSupply = BigDecimal.ZERO,
            lastUpdatedAt = localDateTime
        )

        every { cryptoRepositoryMock.findById("bitcoin") } returns Optional.empty()
        every { coingeckoServiceMock.retrieveCryptoInfo("bitcoin") } returns coingeckoCryptoInfo
        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every { cryptoRepositoryMock.save(cryptoEntity) } returns cryptoEntity

        val crypto = cryptoService.retrieveCryptoInfoById("bitcoin")

        verify(exactly = 1) { cryptoRepositoryMock.save(crypto) }

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(
                Crypto(
                    id = "bitcoin",
                    name = "Bitcoin",
                    ticker = "btc",
                    circulatingSupply = BigDecimal("19000000"),
                    lastKnownPrice = BigDecimal("30000"),
                    lastKnownPriceInBTC = BigDecimal("1"),
                    lastKnownPriceInEUR = BigDecimal("27000"),
                    maxSupply = BigDecimal.ZERO,
                    lastUpdatedAt = localDateTime
                )
            )
    }

    @Test
    fun `should retrieve coingecko crypto info by name`() {
        val coingeckoCrypto = CoingeckoCrypto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin"
        )

        every { coingeckoServiceMock.retrieveAllCryptos() } returns listOf(coingeckoCrypto)

        val crypto = cryptoService.retrieveCoingeckoCryptoInfoByName("bitcoin")

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(
                CoingeckoCrypto(
                    id = "bitcoin",
                    symbol = "btc",
                    name = "Bitcoin"
                )
            )
    }

    @Test
    fun `should throw CoingeckoCryptoNotFoundException when retrieving coingecko crypto info by name`() {
        val coingeckoCrypto = CoingeckoCrypto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin"
        )

        every { coingeckoServiceMock.retrieveAllCryptos() } returns listOf(coingeckoCrypto)

        val exception = assertThrows<CoingeckoCryptoNotFoundException> {
            cryptoService.retrieveCoingeckoCryptoInfoByName("dogecoin")
        }

        assertThat(exception.message).isEqualTo(COINGECKO_CRYPTO_NOT_FOUND.format("dogecoin"))
    }

    @Test
    fun `should save crypto if not exists`() {
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val coingeckoCryptoInfo = getCoingeckoCryptoInfo()

        val slot = slot<Crypto>()
        every { cryptoRepositoryMock.findById("bitcoin") } returns Optional.empty()
        every { coingeckoServiceMock.retrieveCryptoInfo("bitcoin") } returns coingeckoCryptoInfo
        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every { cryptoRepositoryMock.save(capture(slot)) } answers { slot.captured }

        cryptoService.saveCryptoIfNotExists("bitcoin")

        verify(exactly = 1) { cryptoRepositoryMock.save(slot.captured) }

        assertThat(slot.captured)
            .usingRecursiveComparison()
            .isEqualTo(
                Crypto(
                    id = "bitcoin",
                    name = "Bitcoin",
                    ticker = "btc",
                    lastKnownPrice = BigDecimal("30000"),
                    lastKnownPriceInEUR = BigDecimal("27000"),
                    lastKnownPriceInBTC = BigDecimal("1"),
                    circulatingSupply = BigDecimal("19000000"),
                    maxSupply = BigDecimal("21000000"),
                    lastUpdatedAt = localDateTime
                )
            )
    }

    @Test
    fun `should save crypto if not exists with ZERO as max supply`() {
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val marketData = getMarketData(maxSupply = null)
        val coingeckoCryptoInfo = getCoingeckoCryptoInfo(marketData = marketData)

        val slot = slot<Crypto>()
        every { cryptoRepositoryMock.findById("bitcoin") } returns Optional.empty()
        every { coingeckoServiceMock.retrieveCryptoInfo("bitcoin") } returns coingeckoCryptoInfo
        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every { cryptoRepositoryMock.save(capture(slot)) } answers { slot.captured }

        cryptoService.saveCryptoIfNotExists("bitcoin")

        verify(exactly = 1) { cryptoRepositoryMock.save(slot.captured) }

        assertThat(slot.captured)
            .usingRecursiveComparison()
            .isEqualTo(
                Crypto(
                    id = "bitcoin",
                    name = "Bitcoin",
                    ticker = "btc",
                    lastKnownPrice = BigDecimal("30000"),
                    lastKnownPriceInEUR = BigDecimal("27000"),
                    lastKnownPriceInBTC = BigDecimal("1"),
                    circulatingSupply = BigDecimal("19000000"),
                    maxSupply = BigDecimal.ZERO,
                    lastUpdatedAt = localDateTime
                )
            )
    }

    @Test
    fun `should not save crypto if it already exists`() {
        val crypto = getCryptoEntity()

        every { cryptoRepositoryMock.findById("bitcoin") } returns Optional.of(crypto)

        cryptoService.saveCryptoIfNotExists("bitcoin")

        verify(exactly = 0) { cryptoRepositoryMock.save(any()) }
    }

    @Test
    fun `should delete crypto if it is not being used`() {
        every { userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns emptyList()
        every { goalRepositoryMock.findByCoingeckoCryptoId("bitcoin") } returns Optional.empty()
        justRun { cryptoRepositoryMock.deleteById("bitcoin") }

        cryptoService.deleteCryptoIfNotUsed("bitcoin")

        verify(exactly = 1) { cryptoRepositoryMock.deleteById("bitcoin") }
    }

    @Test
    fun `should not delete crypto if it is being used by goals table`() {
        val goal = Goal(
            coingeckoCryptoId = "bitcoin",
            goalQuantity = BigDecimal("1")
        )

        every { userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns emptyList()
        every { goalRepositoryMock.findByCoingeckoCryptoId("bitcoin") } returns Optional.of(goal)

        cryptoService.deleteCryptoIfNotUsed("bitcoin")

        verify(exactly = 0) { cryptoRepositoryMock.deleteById("bitcoin") }
    }

    @Test
    fun `should not delete crypto if it is being used by user cryptos table`() {
        val userCrypto = getUserCrypto()

        every { userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin") } returns listOf(userCrypto)

        cryptoService.deleteCryptoIfNotUsed("bitcoin")

        verify(exactly = 0) { cryptoRepositoryMock.deleteById("bitcoin") }
    }

    @Test
    fun `should find top cryptos by last price update`() {
        val localDateTime = LocalDateTime.now()
        val cryptosEntities = getCryptoEntity(lastUpdatedAt = localDateTime)

        every {
            cryptoRepositoryMock.findOldestNCryptosByLastPriceUpdate(localDateTime, 5)
        } returns listOf(cryptosEntities)

        val cryptos = cryptoService.findOldestNCryptosByLastPriceUpdate(localDateTime, 5)

        assertThat(cryptos)
            .usingRecursiveComparison()
            .isEqualTo(
                listOf(
                    Crypto(
                        id = "bitcoin",
                        name = "Bitcoin",
                        ticker = "btc",
                        circulatingSupply = BigDecimal("19000000"),
                        lastKnownPrice = BigDecimal("30000"),
                        lastKnownPriceInBTC = BigDecimal("1"),
                        lastKnownPriceInEUR = BigDecimal("27000"),
                        maxSupply = BigDecimal("21000000"),
                        lastUpdatedAt = localDateTime
                    )
                )
            )
    }

    @Test
    fun `should update cryptos`() {
        val cryptosEntities = getCryptoEntity()

        every { cryptoRepositoryMock.saveAll(listOf(cryptosEntities)) } returns listOf(cryptosEntities)

        cryptoService.updateCryptos(listOf(cryptosEntities))
    }
}