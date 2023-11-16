package com.distasilucas.cryptobalancetracker.scheduler

import com.distasilucas.cryptobalancetracker.constants.REQUEST_LIMIT_REACHED
import com.distasilucas.cryptobalancetracker.exception.TooManyRequestsException
import com.distasilucas.cryptobalancetracker.service.CoingeckoService
import com.distasilucas.cryptobalancetracker.service.CryptoService
import getCoingeckoCryptoInfo
import getCryptoEntity
import getMarketData
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

private const val LIMIT = 9
private const val MINUTES = 5L

class CryptoSchedulerTest {

    private val clockMock = mockk<Clock>()
    private val cryptoServiceMock = mockk<CryptoService>()
    private val coingeckoServiceMock = mockk<CoingeckoService>()

    private val cryptoScheduler = CryptoScheduler(LIMIT, clockMock, cryptoServiceMock, coingeckoServiceMock)

    @Test
    fun `should update top 9 cryptos information from the last 5 minutes`() {
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val cryptos = getCryptoEntity()
        val coingeckoCryptoInfo = getCoingeckoCryptoInfo()

        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every {
            cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(localDateTime.minusMinutes(MINUTES), LIMIT)
        } returns listOf(cryptos)
        every { coingeckoServiceMock.retrieveCryptoInfo("bitcoin") } returns coingeckoCryptoInfo
        justRun { cryptoServiceMock.updateCryptos(listOf(getCryptoEntity(lastUpdatedAt = localDateTime))) }

        cryptoScheduler.updateCryptosInformation()
    }

    @Test
    fun `should update top 9 cryptos information from the last 5 minutes with ZERO as max supply`() {
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val cryptos = getCryptoEntity(maxSupply = null)
        val marketData = getMarketData(maxSupply = null)
        val coingeckoCryptoInfo = getCoingeckoCryptoInfo(marketData = marketData)

        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every {
            cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(localDateTime.minusMinutes(MINUTES), LIMIT)
        } returns listOf(cryptos)
        every { coingeckoServiceMock.retrieveCryptoInfo("bitcoin") } returns coingeckoCryptoInfo
        justRun {
            cryptoServiceMock.updateCryptos(
                listOf(
                    getCryptoEntity(
                        maxSupply = null,
                        lastUpdatedAt = localDateTime
                    )
                )
            )
        }

        cryptoScheduler.updateCryptosInformation()
    }

    @Test
    fun `should not update if there are no cryptos to update`() {
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))

        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every {
            cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(localDateTime.minusMinutes(MINUTES), LIMIT)
        } returns emptyList()

        cryptoScheduler.updateCryptosInformation()

        verify(exactly = 0) { cryptoServiceMock.updateCryptos(any()) }
    }

    @Test
    fun `should throw TooManyRequestsException when reaching Coingecko limit`() {
        val cryptos = getCryptoEntity()
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val queryLocalDateTime = LocalDateTime.of(2023, 5, 3, 18, 50, 0)

        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every { cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(queryLocalDateTime, LIMIT) } returns listOf(cryptos)
        every {
            coingeckoServiceMock.retrieveCryptoInfo("bitcoin")
        } throws WebClientResponseException(
            HttpStatus.TOO_MANY_REQUESTS,
            "reasonPhrase",
            null,
            null,
            null,
            null
        )

        val exception = assertThrows<TooManyRequestsException> { cryptoScheduler.updateCryptosInformation() }

        assertThat(exception.message).isEqualTo(REQUEST_LIMIT_REACHED)
        verify(exactly = 0) { cryptoServiceMock.updateCryptos(any()) }
    }

    @Test
    fun `should save same crypto when WebClientResponseException occurs with status != 429`() {
        val cryptos = getCryptoEntity()
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val queryLocalDateTime = LocalDateTime.of(2023, 5, 3, 18, 50, 0)

        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every { cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(queryLocalDateTime, LIMIT) } returns listOf(cryptos)
        every {
            coingeckoServiceMock.retrieveCryptoInfo("bitcoin")
        } throws WebClientResponseException(
            HttpStatus.I_AM_A_TEAPOT,
            "reasonPhrase",
            null,
            null,
            null,
            null
        )
        justRun { cryptoServiceMock.updateCryptos(listOf(cryptos)) }

        cryptoScheduler.updateCryptosInformation()

        verify(exactly = 1) { cryptoServiceMock.updateCryptos(listOf(cryptos)) }
    }

    @Test
    fun `should save same crypto if exception occurs when retrieving crypto info`() {
        val cryptos = getCryptoEntity()
        val localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0)
        val zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"))
        val queryLocalDateTime = LocalDateTime.of(2023, 5, 3, 18, 50, 0)

        every { clockMock.instant() } returns localDateTime.toInstant(ZoneOffset.UTC)
        every { clockMock.zone } returns zonedDateTime.zone
        every { cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(queryLocalDateTime, LIMIT) } returns listOf(cryptos)
        every { coingeckoServiceMock.retrieveCryptoInfo("bitcoin") } throws Exception("some exception")
        justRun { cryptoServiceMock.updateCryptos(listOf(cryptos)) }

        cryptoScheduler.updateCryptosInformation()

        verify(exactly = 1) { cryptoServiceMock.updateCryptos(listOf(cryptos)) }
    }
}