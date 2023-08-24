package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CurrentPrice
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketData
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository
import getCryptoEntity
import io.mockk.every
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
    private val clockMock = mockk<Clock>()

    private val cryptoService = CryptoService(coingeckoServiceMock, cryptoRepositoryMock, clockMock)

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
                    id = "123e4567-e89b-12d3-a456-426614174000",
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
    fun `should throw CryptoNotFoundException when retrieving crypto info by id`() {
        every { cryptoRepositoryMock.findById("bitcoin") } returns Optional.empty()

        val exception = assertThrows<CryptoNotFoundException> {
            cryptoService.retrieveCryptoInfoById("bitcoin")
        }

        assertThat(exception.message).isEqualTo(CRYPTO_NOT_FOUND)
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
        val coingeckoCryptoInfo = CoingeckoCryptoInfo(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            marketData = MarketData(
                currentPrice = CurrentPrice(
                    usd = BigDecimal("30000"),
                    eur = BigDecimal("27000"),
                    btc = BigDecimal("1")
                ),
                circulatingSupply = BigDecimal("19000000"),
                maxSupply = BigDecimal("21000000")
            )
        )

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
            .isEqualTo(Crypto(
                id = "bitcoin",
                name = "Bitcoin",
                ticker = "btc",
                lastKnownPrice = BigDecimal("30000"),
                lastKnownPriceInEUR = BigDecimal("27000"),
                lastKnownPriceInBTC = BigDecimal("1"),
                circulatingSupply = BigDecimal("19000000"),
                maxSupply = BigDecimal("21000000"),
                lastUpdatedAt = localDateTime
            ))
    }

    @Test
    fun `should not save crypto if it already exists`() {
        val crypto = getCryptoEntity()

        every { cryptoRepositoryMock.findById("bitcoin") } returns Optional.of(crypto)

        verify(exactly = 0) { cryptoRepositoryMock.save(any()) }
    }
}