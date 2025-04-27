package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.CryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import cryptos
import getCryptoEntity
import getPlatformEntity
import getUserCrypto
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import userCryptos
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class PlatformInsightServiceTest {

  private val platformServiceMock = mockk<PlatformService>()
  private val userCryptoServiceMock = mockk<UserCryptoService>()
  private val cryptoServiceMock = mockk<CryptoService>()
  private val insightsServiceMock = mockk<InsightsService>()

  private val platformInsightService = PlatformInsightService(
    platformServiceMock, userCryptoServiceMock, cryptoServiceMock, insightsServiceMock
  )

  @Test
  fun `should retrieve platform insights with one crypto`() {
    val platformEntity = getPlatformEntity()
    val bitcoinUserCrypto = getUserCrypto()
    val bitcoinCryptoEntity = getCryptoEntity()

    every {
      userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")
    } returns listOf(bitcoinUserCrypto)
    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platformEntity
    every {
      cryptoServiceMock.findAllByIds(listOf("bitcoin"))
    } returns listOf(bitcoinCryptoEntity)
    every {
      insightsServiceMock.getUserCryptoQuantity(listOf(bitcoinUserCrypto))
    } returns mapOf("bitcoin" to BigDecimal("0.25"))
    every {
      insightsServiceMock.getTotalBalances(listOf(bitcoinCryptoEntity), mapOf("bitcoin" to BigDecimal("0.25")))
    } returns Balances(FiatBalance("7500", "6750"), "0.25")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoinCryptoEntity, BigDecimal("0.25"))
    } returns Balances(FiatBalance("7500", "6750"), "0.25")
    every {
      insightsServiceMock.calculatePercentage("7500", "7500")
    } returns 100F

    val platformInsights = platformInsightService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

    assertThat(platformInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        PlatformInsightsResponse(
          platformName = "BINANCE",
          balances = Balances(FiatBalance("7500", "6750"), "0.25"),
          cryptos = listOf(
            CryptoInsights(
              id = "123e4567-e89b-12d3-a456-426614174000",
              cryptoInfo = CryptoInfo(
                cryptoName = "Bitcoin",
                coingeckoCryptoId = "bitcoin",
                symbol = "btc",
                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
              ),
              quantity = "0.25",
              percentage = 100f,
              balances = Balances(FiatBalance("7500", "6750"), "0.25")
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve platform insights with multiple cryptos`() {
    val localDateTime = LocalDateTime.now()
    val platformEntity = getPlatformEntity()
    val bitcoinUserCrypto = getUserCrypto()
    val polkadotUserCrypto = UserCrypto(
      coingeckoCryptoId = "polkadot",
      quantity = BigDecimal("100"),
      platformId = "123e4567-e89b-12d3-a456-426614174111"
    )
    val bitcoinCryptoEntity = getCryptoEntity()
    val polkadotCryptoEntity = Crypto(
      id = "polkadot",
      name = "Polkadot",
      ticker = "dot",
      image = "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644",
      circulatingSupply = BigDecimal("1272427996.25919"),
      lastKnownPrice = BigDecimal("4.25"),
      lastKnownPriceInBTC = BigDecimal("0.00016554"),
      lastKnownPriceInEUR = BigDecimal("3.97"),
      maxSupply = BigDecimal.ZERO,
      marketCapRank = 13,
      marketCap = BigDecimal("8946471948"),
      changePercentageIn24h = -2.75,
      changePercentageIn7d = 10.25,
      changePercentageIn30d = -5.15,
      lastUpdatedAt = localDateTime
    )

    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platformEntity
    every {
      userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")
    } returns listOf(bitcoinUserCrypto, polkadotUserCrypto)
    every {
      cryptoServiceMock.findAllByIds(listOf("bitcoin", "polkadot"))
    } returns listOf(bitcoinCryptoEntity, polkadotCryptoEntity)
    every {
      insightsServiceMock.getUserCryptoQuantity(listOf(bitcoinUserCrypto, polkadotUserCrypto))
    } returns mapOf("bitcoin" to BigDecimal("0.25"), "polkadot" to BigDecimal("100"))
    every {
      insightsServiceMock.getTotalBalances(
        listOf(bitcoinCryptoEntity, polkadotCryptoEntity),
        mapOf("bitcoin" to BigDecimal("0.25"), "polkadot" to BigDecimal("100"))
      )
    } returns Balances(FiatBalance("7925", "7147"), "0.266554")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoinCryptoEntity, bitcoinUserCrypto.quantity)
    } returns Balances(FiatBalance("7500", "6750"), "0.25")
    every {
      insightsServiceMock.getCryptoTotalBalances(polkadotCryptoEntity, polkadotUserCrypto.quantity)
    } returns Balances(FiatBalance("425", "397"), "0.016554")
    every {
      insightsServiceMock.calculatePercentage("7925", "7500")
    } returns 94.64F
    every {
      insightsServiceMock.calculatePercentage("7925", "425")
    } returns 5.36F

    val platformInsights = platformInsightService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

    assertThat(platformInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        PlatformInsightsResponse(
          platformName = "BINANCE",
          balances = Balances(FiatBalance("7925", "7147"), "0.266554"),
          cryptos = listOf(
            CryptoInsights(
              id = "123e4567-e89b-12d3-a456-426614174000",
              cryptoInfo = CryptoInfo(
                cryptoName = "Bitcoin",
                coingeckoCryptoId = "bitcoin",
                symbol = "btc",
                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
              ),
              quantity = "0.25",
              percentage = 94.64F,
              balances = Balances(FiatBalance("7500", "6750"), "0.25")
            ),
            CryptoInsights(
              id = polkadotUserCrypto.id,
              cryptoInfo = CryptoInfo(
                cryptoName = "Polkadot",
                coingeckoCryptoId = "polkadot",
                symbol = "dot",
                image = "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644",
              ),
              quantity = "100",
              percentage = 5.36F,
              balances = Balances(FiatBalance("425", "397"), "0.016554")
            )
          )
        )
      )
  }

  @Test
  fun `should throw ApiException if no cryptos are found for retrievePlatformInsights`() {
    every {
      userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")
    } returns emptyList()

    val exception = assertThrows<ApiException> {
      platformInsightService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")
    }

    assertEquals(HttpStatus.NO_CONTENT, exception.httpStatusCode)
    assertEquals("There is no user cryptos in platform", exception.message)
  }

  @Test
  fun `should retrieve platforms balances insights`() {
    val cryptos = listOf("bitcoin", "tether", "ethereum", "litecoin")
    val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
    val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }
    val binancePlatform = Platform(
      id = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8",
      name = "BINANCE"
    )
    val coinbasePlatform = Platform(
      id = "a76b400e-8ffc-42d6-bf47-db866eb20153",
      name = "COINBASE"
    )
    val userCryptosQuantityMap = mapOf(
      "bitcoin" to BigDecimal("0.15"),
      "tether" to BigDecimal("200"),
      "ethereum" to BigDecimal("1.372"),
      "litecoin" to BigDecimal("3.125")
    )
    val (bitcoin, tether, ethereum, litecoin) = cryptosEntities

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      platformServiceMock.findAllByIds(setOf("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153"))
    } returns listOf(binancePlatform, coinbasePlatform)
    every { cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum", "litecoin")) } returns cryptosEntities
    every { insightsServiceMock.getUserCryptoQuantity(userCryptos) } returns userCryptosQuantityMap
    every {
      insightsServiceMock.getTotalBalances(cryptosEntities, userCryptosQuantityMap)
    } returns Balances(FiatBalance("10108.39", "9184.22"), "0.35128759")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoin, BigDecimal("0.15"))
    } returns Balances(FiatBalance("4500", "4050"), "0.15")
    every {
      insightsServiceMock.getCryptoTotalBalances(tether, BigDecimal("200"))
    } returns Balances(FiatBalance("199.92", "186.61"), "0.00776")
    every {
      insightsServiceMock.getCryptoTotalBalances(ethereum, BigDecimal("0.26"))
    } returns Balances(FiatBalance("420.53", "392.43"), "0.01632892")
    every {
      insightsServiceMock.getCryptoTotalBalances(ethereum, BigDecimal("1.112"))
    } returns Balances(FiatBalance("1798.59", "1678.41"), "0.06983755")
    every {
      insightsServiceMock.getCryptoTotalBalances(litecoin, BigDecimal("3.125"))
    } returns Balances(FiatBalance("189.34", "176.75"), "0.00735287")
    every {
      insightsServiceMock.calculatePercentage("10108.39", "5120.45")
    } returns 72.03F
    every {
      insightsServiceMock.calculatePercentage("10108.39", "1987.93")
    } returns 27.97F

    val platformBalancesInsights = platformInsightService.retrievePlatformsBalancesInsights()

    assertThat(platformBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          BalancesChartResponse("BINANCE", "5120.45", 72.03F),
          BalancesChartResponse("COINBASE", "1987.93", 27.97F),
        )
      )
  }

  @Test
  fun `should retrieve empty if no cryptos are found for retrievePlatformBalancesInsights`() {
    every { userCryptoServiceMock.findAll() } returns emptyList()

    val platformBalancesInsightsResponse = platformInsightService.retrievePlatformsBalancesInsights()

    assertThat(platformBalancesInsightsResponse)
      .usingRecursiveComparison()
      .isEqualTo(emptyList<BalancesChartResponse>())
  }
}
