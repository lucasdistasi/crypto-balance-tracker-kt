package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import cryptos
import getCryptoEntity
import getUserCrypto
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import userCryptos
import java.math.BigDecimal
import java.util.*

class CryptoInsightsServiceTest {

  private val platformServiceMock = mockk<PlatformService>()
  private val userCryptoServiceMock = mockk<UserCryptoService>()
  private val cryptoServiceMock = mockk<CryptoService>()
  private val insightsServiceMock = mockk<InsightsService>()

  private val cryptoInsightsService = CryptoInsightsService(
    3, 4, platformServiceMock, userCryptoServiceMock, cryptoServiceMock, insightsServiceMock
  )

  @Test
  fun `should retrieve coingeckoCryptoId insights with one platform`() {
    val bitcoinUserCrypto = getUserCrypto()
    val binancePlatform = Platform(
      id = "123e4567-e89b-12d3-a456-426614174111",
      name = "BINANCE"
    )
    val bitcoinCryptoEntity = getCryptoEntity()

    every {
      userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")
    } returns listOf(bitcoinUserCrypto)
    every {
      platformServiceMock.findAllByIds(listOf("123e4567-e89b-12d3-a456-426614174111"))
    } returns listOf(binancePlatform)
    every {
      cryptoServiceMock.retrieveCryptoInfoById("bitcoin")
    } returns bitcoinCryptoEntity
    every {
      insightsServiceMock.getTotalBalances(
        listOf(bitcoinCryptoEntity),
        mapOf("bitcoin" to BigDecimal("0.25"))
      )
    } returns Balances(FiatBalance("7500", "6750"), "0.25")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoinCryptoEntity, BigDecimal("0.25"))
    } returns Balances(FiatBalance("7500", "6750"), "0.25")
    every {
      insightsServiceMock.calculatePercentage("7500", "7500")
    } returns 100F

    val cryptoInsights = cryptoInsightsService.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          CryptoInsightResponse(
            cryptoInfo = CryptoInfo(
              cryptoName = "Bitcoin",
              coingeckoCryptoId = "bitcoin",
              symbol = "btc",
              image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
              price = Price("30000", "27000"),
              priceChange = PriceChange(10.0, -5.0, 0.0)
            ),
            balances = Balances(FiatBalance("7500", "6750"), "0.25"),
            platforms = listOf(
              PlatformInsight(
                quantity = "0.25",
                balances = Balances(FiatBalance("7500", "6750"), "0.25"),
                percentage = 100F,
                platformName = "BINANCE"
              )
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve coingeckoCryptoId insights with multiple platforms`() {
    val bitcoinUserCrypto = listOf(
      getUserCrypto(),
      getUserCrypto(
        id = "ed34425b-d9f7-4244-bd16-0212621848c6",
        quantity = BigDecimal("0.03455"),
        platformId = "fa3db02d-4d43-416a-951b-e7ea3a4fe386"
      )
    )
    val binancePlatform = Platform(
      id = "123e4567-e89b-12d3-a456-426614174111",
      name = "BINANCE"
    )
    val coinbasePlatform = Platform(
      id = "fa3db02d-4d43-416a-951b-e7ea3a4fe386",
      name = "COINBASE"
    )
    val bitcoinCryptoEntity = getCryptoEntity()

    every {
      userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")
    } returns bitcoinUserCrypto
    every {
      platformServiceMock.findAllByIds(
        listOf("123e4567-e89b-12d3-a456-426614174111", "fa3db02d-4d43-416a-951b-e7ea3a4fe386")
      )
    } returns listOf(binancePlatform, coinbasePlatform)
    every {
      cryptoServiceMock.retrieveCryptoInfoById("bitcoin")
    } returns bitcoinCryptoEntity
    every {
      insightsServiceMock.getTotalBalances(listOf(bitcoinCryptoEntity), mapOf("bitcoin" to BigDecimal("0.28455")))
    } returns Balances(FiatBalance("8536.50", "7682.85"), "0.28455")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoinCryptoEntity, BigDecimal("0.25"))
    } returns Balances(FiatBalance("7500", "6750"), "0.25")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoinCryptoEntity, BigDecimal("0.03455"))
    } returns Balances(FiatBalance("1036.50", "932.85"), "0.03455")
    every {
      insightsServiceMock.calculatePercentage("8536.50", "7500")
    } returns 87.86F
    every {
      insightsServiceMock.calculatePercentage("8536.50", "1036.50")
    } returns 12.14F

    val cryptoInsight = cryptoInsightsService.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsight)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          CryptoInsightResponse(
            cryptoInfo = CryptoInfo(
              cryptoName = "Bitcoin",
              coingeckoCryptoId = "bitcoin",
              symbol = "btc",
              image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
              price = Price("30000", "27000"),
              priceChange = PriceChange(10.0, -5.0, 0.0)
            ),
            balances = Balances(FiatBalance("8536.50", "7682.85"), "0.28455"),
            platforms = listOf(
              PlatformInsight(
                quantity = "0.25",
                balances = Balances(FiatBalance("7500", "6750"), "0.25"),
                percentage = 87.86F,
                platformName = "BINANCE"
              ),
              PlatformInsight(
                quantity = "0.03455",
                balances = Balances(FiatBalance("1036.50", "932.85"), "0.03455"),
                percentage = 12.14F,
                platformName = "COINBASE"
              )
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve null if no cryptos are found for retrieveCryptoInsights`() {
    every {
      userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")
    } returns emptyList()

    val cryptoInsight = cryptoInsightsService.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsight)
      .usingRecursiveComparison()
      .isEqualTo(Optional.empty<CryptoInsightResponse>())
  }

  @Test
  fun `should retrieve cryptos balances insights`() {
    val cryptos = listOf("bitcoin", "tether", "ethereum")
    val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
    val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }
    val (bitcoin, tether, ethereum) = cryptosEntities

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      insightsServiceMock.getUserCryptoQuantity(userCryptos)
    } returns mapOf(
      "bitcoin" to BigDecimal("0.15"),
      "tether" to BigDecimal("200"),
      "ethereum" to BigDecimal("1.372"),
    )
    every {
      cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum"))
    } returns cryptosEntities
    every {
      insightsServiceMock.getTotalBalances(
        cryptosEntities,
        mapOf(
          "bitcoin" to BigDecimal("0.15"),
          "tether" to BigDecimal("200"),
          "ethereum" to BigDecimal("1.372"),
        )
      )
    } returns Balances(FiatBalance("7108.39", "6484.23"), "0.25127936")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoin, BigDecimal("0.15"))
    } returns Balances(FiatBalance("4500", "4050"), "0.15")
    every {
      insightsServiceMock.getCryptoTotalBalances(ethereum, BigDecimal("1.372"))
    } returns Balances(FiatBalance("2219.13", "2070.84"), "0.08616647")
    every {
      insightsServiceMock.getCryptoTotalBalances(tether, BigDecimal("200"))
    } returns Balances(FiatBalance("199.92", "186.61"), "0.00776")
    every {
      insightsServiceMock.calculatePercentage("7108.39", "4500")
    } returns 63.31F
    every {
      insightsServiceMock.calculatePercentage("7108.39", "2219.13")
    } returns 31.22F
    every {
      insightsServiceMock.calculatePercentage("7108.39", "199.92")
    } returns 2.81F

    val cryptosBalancesInsights = cryptoInsightsService.retrieveCryptosBalancesInsights()

    assertThat(cryptosBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          BalancesChartResponse("Bitcoin", "4500", 63.31F),
          BalancesChartResponse("Ethereum", "2219.13", 31.22F),
          BalancesChartResponse("Tether", "199.92", 2.81F),
        )
      )
  }

  @Test
  fun `should retrieve cryptos balances insights with others`() {
    val cryptos = listOf("bitcoin", "tether", "ethereum", "litecoin", "binancecoin")
    val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
    val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }
    val (bitcoin, tether, ethereum, litecoin, binancecoin) = cryptosEntities

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      insightsServiceMock.getUserCryptoQuantity(userCryptos)
    } returns mapOf(
      "bitcoin" to BigDecimal("0.15"),
      "tether" to BigDecimal("200"),
      "ethereum" to BigDecimal("1.372"),
      "litecoin" to BigDecimal("3.125"),
      "binancecoin" to BigDecimal("1"),
    )
    every {
      cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum", "litecoin", "binancecoin"))
    } returns cryptosEntities
    every {
      insightsServiceMock.getTotalBalances(
        cryptosEntities,
        mapOf(
          "bitcoin" to BigDecimal("0.15"),
          "tether" to BigDecimal("200"),
          "ethereum" to BigDecimal("1.372"),
          "litecoin" to BigDecimal("3.125"),
          "binancecoin" to BigDecimal("1"),
        )
      )
    } returns Balances(FiatBalance("7320.17", "6682.01"), "0.25922951")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoin, BigDecimal("0.15"))
    } returns Balances(FiatBalance("4500", "4050"), "0.15")
    every {
      insightsServiceMock.getCryptoTotalBalances(ethereum, BigDecimal("1.372"))
    } returns Balances(FiatBalance("2219.12", "2070.85"), "0.08616648")
    every {
      insightsServiceMock.getCryptoTotalBalances(binancecoin, BigDecimal("1"))
    } returns Balances(FiatBalance("211.79", "197.80"), "0.00811016")
    every {
      insightsServiceMock.getCryptoTotalBalances(tether, BigDecimal("200"))
    } returns Balances(FiatBalance("199.92", "186.61"), "0.00776")
    every {
      insightsServiceMock.getCryptoTotalBalances(litecoin, BigDecimal("3.125"))
    } returns Balances(FiatBalance("189.34", "176.75"), "0.00735287")
    every {
      insightsServiceMock.calculatePercentage("7320.17", "4500")
    } returns 61.47F
    every {
      insightsServiceMock.calculatePercentage("7320.17", "2219.12")
    } returns 30.32F
    every {
      insightsServiceMock.calculatePercentage("7320.17", "211.79")
    } returns 2.89F
    every {
      insightsServiceMock.calculatePercentage("7320.17", "199.92")
    } returns 2.73F
    every {
      insightsServiceMock.calculatePercentage("7320.17", "189.34")
    } returns 2.59F
    every {
      insightsServiceMock.calculatePercentage("7320.17", "389.26")
    } returns 5.32F

    val cryptosBalancesInsights = cryptoInsightsService.retrieveCryptosBalancesInsights()

    assertThat(cryptosBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          BalancesChartResponse("Bitcoin", "4500", 61.47F),
          BalancesChartResponse("Ethereum", "2219.12", 30.32F),
          BalancesChartResponse("BNB", "211.79", 2.89F),
          BalancesChartResponse("Others", "389.26", 5.32F),
        )
      )
  }

  @Test
  fun `should retrieve empty if no cryptos are found for retrieveCryptosBalancesInsights`() {
    every { userCryptoServiceMock.findAll() } returns emptyList()

    val cryptosBalancesInsights = cryptoInsightsService.retrieveCryptosBalancesInsights()

    assertThat(cryptosBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(emptyList<BalancesChartResponse>())
  }

  @Test
  fun `should retrieve user cryptos insights`() {
    val cryptos = listOf("bitcoin", "ethereum", "tether")
    val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
    val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }
    val (bitcoin, tether, ethereum) = cryptosEntities

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      insightsServiceMock.getUserCryptoQuantity(userCryptos)
    } returns mapOf(
      "bitcoin" to BigDecimal("0.15"),
      "ethereum" to BigDecimal("1.372"),
      "tether" to BigDecimal("200"),
    )
    every { cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum")) } returns cryptosEntities
    every {
      insightsServiceMock.getTotalBalances(
        cryptosEntities,
        mapOf(
          "bitcoin" to BigDecimal("0.15"),
          "ethereum" to BigDecimal("1.372"),
          "tether" to BigDecimal("200"),
        )
      )
    } returns Balances(FiatBalance("6919.05", "6307.48"), "0.24392648")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoin, BigDecimal("0.15"))
    } returns Balances(FiatBalance("4500", "4050"), "0.15")
    every {
      insightsServiceMock.getCryptoTotalBalances(ethereum, BigDecimal("1.372"))
    } returns Balances(FiatBalance("2219.13", "2070.86"), "0.08616648")
    every {
      insightsServiceMock.getCryptoTotalBalances(tether, BigDecimal("200"))
    } returns Balances(FiatBalance("199.92", "186.62"), "0.00776")
    every {
      insightsServiceMock.calculatePercentage("6919.05", "4500")
    } returns 65.04F
    every {
      insightsServiceMock.calculatePercentage("6919.05", "2219.13")
    } returns 32.07F
    every {
      insightsServiceMock.calculatePercentage("6919.05", "199.92")
    } returns 2.89F

    val userCryptosPlatformsInsights = cryptoInsightsService.retrieveUserCryptosInsights(0)

    assertThat(userCryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        PageUserCryptosInsightsResponse(
          page = 1,
          totalPages = 1,
          hasNextPage = false,
          balances = Balances(FiatBalance("6919.05", "6307.48"), "0.24392648"),
          cryptos = listOf(
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Bitcoin",
                coingeckoCryptoId = "bitcoin",
                symbol = "btc",
                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                price = Price(
                  usd = "30000",
                  eur = "27000",
                  btc = "1"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 10.00,
                  changePercentageIn7d = -5.00,
                  changePercentageIn30d = 0.00
                )
              ),
              quantity = "0.15",
              percentage = 65.04F,
              balances = Balances(FiatBalance("4500", "4050"), "0.15")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Ethereum",
                coingeckoCryptoId = "ethereum",
                symbol = "eth",
                image = "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
                price = Price(
                  usd = "1617.44",
                  eur = "1509.37",
                  btc = "0.06280356"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 10.00,
                  changePercentageIn7d = -5.00,
                  changePercentageIn30d = 2.00
                )
              ),
              quantity = "1.372",
              percentage = 32.07F,
              balances = Balances(FiatBalance("2219.13", "2070.86"), "0.08616648")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Tether",
                coingeckoCryptoId = "tether",
                symbol = "usdt",
                image = "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663",
                price = Price(
                  usd = "0.999618",
                  eur = "0.933095",
                  btc = "0.0000388"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 0.00,
                  changePercentageIn7d = 0.00,
                  changePercentageIn30d = 0.00
                )
              ),
              quantity = "200",
              percentage = 2.89F,
              balances = Balances(FiatBalance("199.92", "186.62"), "0.00776")
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve user cryptos insights with next page`() {
    val cryptos = cryptos().take(5)
    val (bitcoin, tether, ethereum, litecoin, binancecoin) = cryptos
    val cryptosIds = cryptos.map { it.id }
    val userCryptos = userCryptos().filter { cryptosIds.contains(it.coingeckoCryptoId) }

    every {
      userCryptoServiceMock.findAll()
    } returns userCryptos
    every {
      insightsServiceMock.getUserCryptoQuantity(userCryptos)
    } returns userCryptoQuantityMap()
    every {
      cryptoServiceMock.findAllByIds(
        setOf(
          "bitcoin",
          "tether",
          "ethereum",
          "litecoin",
          "binancecoin"
        )
      )
    } returns cryptos
    every {
      insightsServiceMock.getTotalBalances(cryptos, userCryptoQuantityMap())
    } returns Balances(FiatBalance("8373.63", "7663.61"), "0.29959592")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoin, BigDecimal("0.15"))
    } returns Balances(FiatBalance("4500", "4050"), "0.15")
    every {
      insightsServiceMock.getCryptoTotalBalances(ethereum, BigDecimal("1.372"))
    } returns Balances(FiatBalance("2219.13", "2070.86"), "0.08616648")
    every {
      insightsServiceMock.getCryptoTotalBalances(binancecoin, BigDecimal("1"))
    } returns Balances(FiatBalance("211.79", "197.80"), "0.00811016")
    every {
      insightsServiceMock.getCryptoTotalBalances(tether, BigDecimal("200"))
    } returns Balances(FiatBalance("199.92", "186.62"), "0.00776")
    every {
      insightsServiceMock.getCryptoTotalBalances(litecoin, BigDecimal("3.125"))
    } returns Balances(FiatBalance("189.34", "176.75"), "0.00735288")
    every {
      insightsServiceMock.calculatePercentage("8373.63", "4500")
    } returns 53.74F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "2219.13")
    } returns 26.5F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "211.79")
    } returns 2.53F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "199.92")
    } returns 2.39F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "189.34")
    } returns 2.26F

    val userCryptosPlatformsInsights = cryptoInsightsService.retrieveUserCryptosInsights(0)

    assertThat(userCryptosPlatformsInsights.cryptos.size).isEqualTo(4)
    assertThat(userCryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        PageUserCryptosInsightsResponse(
          page = 1,
          totalPages = 2,
          hasNextPage = true,
          balances = Balances(FiatBalance("8373.63", "7663.61"), "0.29959592"),
          cryptos = listOf(
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Bitcoin",
                coingeckoCryptoId = "bitcoin",
                symbol = "btc",
                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                price = Price(
                  usd = "30000",
                  eur = "27000",
                  btc = "1"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 10.00,
                  changePercentageIn7d = -5.00,
                  changePercentageIn30d = 0.00
                )
              ),
              quantity = "0.15",
              percentage = 53.74F,
              balances = Balances(FiatBalance("4500", "4050"), "0.15")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Ethereum",
                coingeckoCryptoId = "ethereum",
                symbol = "eth",
                image = "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
                price = Price(
                  usd = "1617.44",
                  eur = "1509.37",
                  btc = "0.06280356"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 10.00,
                  changePercentageIn7d = -5.00,
                  changePercentageIn30d = 2.00
                )
              ),
              quantity = "1.372",
              percentage = 26.5F,
              balances = Balances(FiatBalance("2219.13", "2070.86"), "0.08616648")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "BNB",
                coingeckoCryptoId = "binancecoin",
                symbol = "bnb",
                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850",
                price = Price(
                  usd = "211.79",
                  eur = "197.80",
                  btc = "0.00811016"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 6.00,
                  changePercentageIn7d = -2.00,
                  changePercentageIn30d = 12.00
                )
              ),
              quantity = "1",
              percentage = 2.53F,
              balances = Balances(FiatBalance("211.79", "197.80"), "0.00811016")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Tether",
                coingeckoCryptoId = "tether",
                symbol = "usdt",
                image = "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663",
                price = Price(
                  usd = "0.999618",
                  eur = "0.933095",
                  btc = "0.0000388"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 0.00,
                  changePercentageIn7d = 0.00,
                  changePercentageIn30d = 0.00
                )
              ),
              quantity = "200",
              percentage = 2.39F,
              balances = Balances(FiatBalance("199.92", "186.62"), "0.00776")
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve user cryptos insights for second page`() {
    val cryptos = cryptos().take(7)
    val (bitcoin, tether, ethereum, litecoin, binancecoin) = cryptos
    val ripple = cryptos[5]
    val cardano = cryptos[6]
    val cryptosIds = cryptos.map { it.id }
    val userCryptos = userCryptos().filter { cryptosIds.contains(it.coingeckoCryptoId) }

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      insightsServiceMock.getUserCryptoQuantity(userCryptos)
    } returns mapOf(
      "bitcoin" to BigDecimal("0.15"),
      "tether" to BigDecimal("200"),
      "ethereum" to BigDecimal("1.372"),
      "litecoin" to BigDecimal("3.125"),
      "binancecoin" to BigDecimal("1"),
      "ripple" to BigDecimal("50"),
      "cardano" to BigDecimal("150")
    )
    every {
      cryptoServiceMock.findAllByIds(
        setOf(
          "bitcoin",
          "tether",
          "ethereum",
          "litecoin",
          "binancecoin",
          "ripple",
          "cardano"
        )
      )
    } returns cryptos
    every {
      insightsServiceMock.getTotalBalances(
        cryptos,
        mapOf(
          "bitcoin" to BigDecimal("0.15"),
          "tether" to BigDecimal("200"),
          "ethereum" to BigDecimal("1.372"),
          "litecoin" to BigDecimal("3.125"),
          "binancecoin" to BigDecimal("1"),
          "ripple" to BigDecimal("50"),
          "cardano" to BigDecimal("150")
        )
      )
    } returns Balances(FiatBalance("8373.63", "7663.61"), "0.29959592")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoin, BigDecimal("0.15"))
    } returns Balances(FiatBalance("4500", "4050"), "0.15")
    every {
      insightsServiceMock.getCryptoTotalBalances(ethereum, BigDecimal("1.372"))
    } returns Balances(FiatBalance("2219.13", "2070.86"), "0.08616648")
    every {
      insightsServiceMock.getCryptoTotalBalances(binancecoin, BigDecimal("1"))
    } returns Balances(FiatBalance("211.79", "197.80"), "0.00811016")
    every {
      insightsServiceMock.getCryptoTotalBalances(tether, BigDecimal("200"))
    } returns Balances(FiatBalance("199.92", "186.62"), "0.00776")
    every {
      insightsServiceMock.getCryptoTotalBalances(litecoin, BigDecimal("3.125"))
    } returns Balances(FiatBalance("189.34", "176.75"), "0.00735288")
    every {
      insightsServiceMock.getCryptoTotalBalances(cardano, BigDecimal("150"))
    } returns Balances(FiatBalance("37.34", "34.80"), "0.001425")
    every {
      insightsServiceMock.getCryptoTotalBalances(ripple, BigDecimal("50"))
    } returns Balances(FiatBalance("23.92", "22.33"), "0.0009165")
    every {
      insightsServiceMock.calculatePercentage("8373.63", "4500")
    } returns 53.74F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "2219.13")
    } returns 26.5F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "211.79")
    } returns 2.53F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "199.92")
    } returns 2.39F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "189.34")
    } returns 2.26F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "37.34")
    } returns 0.45F
    every {
      insightsServiceMock.calculatePercentage("8373.63", "23.92")
    } returns 0.29F

    val userCryptosPlatformsInsights = cryptoInsightsService.retrieveUserCryptosInsights(1)

    assertThat(userCryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(
        PageUserCryptosInsightsResponse(
          page = 2,
          totalPages = 2,
          hasNextPage = false,
          balances = Balances(FiatBalance("8373.63", "7663.61"), "0.29959592"),
          cryptos = listOf(
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Litecoin",
                coingeckoCryptoId = "litecoin",
                symbol = "ltc",
                image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580",
                price = Price(
                  usd = "60.59",
                  eur = "56.56",
                  btc = "0.00235292"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 6.00,
                  changePercentageIn7d = -2.00,
                  changePercentageIn30d = 12.00
                )
              ),
              quantity = "3.125",
              percentage = 2.26F,
              balances = Balances(FiatBalance("189.34", "176.75"), "0.00735288")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Cardano",
                coingeckoCryptoId = "cardano",
                symbol = "ada",
                image = "https://assets.coingecko.com/coins/images/975/large/cardano.png?1547034860",
                price = Price(
                  usd = "0.248915",
                  eur = "0.231985",
                  btc = "0.0000095"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 7.00,
                  changePercentageIn7d = 1.00,
                  changePercentageIn30d = -2.00
                )
              ),
              quantity = "150",
              percentage = 0.45F,
              balances = Balances(FiatBalance("37.34", "34.80"), "0.001425"),
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "XRP",
                coingeckoCryptoId = "ripple",
                symbol = "xrp",
                image = "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1605778731",
                price = Price(
                  usd = "0.478363",
                  eur = "0.446699",
                  btc = "0.00001833"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = 2.00,
                  changePercentageIn7d = 3.00,
                  changePercentageIn30d = -5.00
                )
              ),
              quantity = "50",
              percentage = 0.29F,
              balances = Balances(FiatBalance("23.92", "22.33"), "0.0009165")
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve null if no user cryptos are found for retrieveUserCryptosInsights`() {
    every { userCryptoServiceMock.findAll() } returns emptyList()

    val userCryptosPlatformsInsights = cryptoInsightsService.retrieveUserCryptosInsights(0)

    assertThat(userCryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(PageUserCryptosInsightsResponse.EMPTY)
  }

  @Test
  fun `should retrieve null if no user cryptos are found for page for retrieveUserCryptosInsights`() {
    val cryptos = listOf("bitcoin", "ethereum", "tether")
    val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
    val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }
    val (bitcoin, tether, ethereum) = cryptosEntities

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      insightsServiceMock.getUserCryptoQuantity(userCryptos)
    } returns mapOf(
      "bitcoin" to BigDecimal("0.15"),
      "ethereum" to BigDecimal("1.372"),
      "tether" to BigDecimal("200"),
    )
    every { cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum")) } returns cryptosEntities
    every {
      insightsServiceMock.getTotalBalances(
        cryptosEntities,
        mapOf(
          "bitcoin" to BigDecimal("0.15"),
          "ethereum" to BigDecimal("1.372"),
          "tether" to BigDecimal("200"),
        )
      )
    } returns Balances(FiatBalance("6919.05", "6307.47"), "0.24392648")
    every {
      insightsServiceMock.getCryptoTotalBalances(bitcoin, BigDecimal("0.15"))
    } returns Balances(FiatBalance("4500", "4050"), "0.15")
    every {
      insightsServiceMock.getCryptoTotalBalances(ethereum, BigDecimal("1.372"))
    } returns Balances(FiatBalance("2219.12", "2070.85"), "0.08616648")
    every {
      insightsServiceMock.getCryptoTotalBalances(tether, BigDecimal("200"))
    } returns Balances(FiatBalance("199.93", "186.61"), "0.0076")
    every {
      insightsServiceMock.calculatePercentage("6919.05", "4500")
    } returns 65.04F
    every {
      insightsServiceMock.calculatePercentage("6919.05", "2219.12")
    } returns 32.07F
    every {
      insightsServiceMock.calculatePercentage("6919.05", "199.93")
    } returns 2.89F

    val userCryptosPlatformsInsights = cryptoInsightsService.retrieveUserCryptosInsights(1)

    assertThat(userCryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(PageUserCryptosInsightsResponse.EMPTY)
  }

  private fun userCryptoQuantityMap() = mapOf(
    "bitcoin" to BigDecimal("0.15"),
    "tether" to BigDecimal("200"),
    "ethereum" to BigDecimal("1.372"),
    "litecoin" to BigDecimal("3.125"),
    "binancecoin" to BigDecimal("1"),
    "ripple" to BigDecimal("50"),
    "cardano" to BigDecimal("150"),
    "polkadot" to BigDecimal("40"),
    "solana" to BigDecimal("10"),
    "matic-network" to BigDecimal("100"),
    "chainlink" to BigDecimal("35"),
    "dogecoin" to BigDecimal("500"),
    "avalanche-2" to BigDecimal("25"),
    "uniswap" to BigDecimal("30"),
  )
}
