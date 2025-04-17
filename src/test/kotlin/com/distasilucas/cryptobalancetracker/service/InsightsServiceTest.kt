package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.model.response.insights.TotalBalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.CryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import getCryptoEntity
import getPlatformEntity
import getUserCrypto
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.stream.IntStream

class InsightsServiceTest {

  private val platformServiceMock = mockk<PlatformService>()
  private val userCryptoServiceMock = mockk<UserCryptoService>()
  private val cryptoServiceMock = mockk<CryptoService>()
  private val dateBalanceRepositoryMock = mockk<DateBalanceRepository>()
  private val clockMock = mockk<Clock>()

  private val insightsService = InsightsService(12, platformServiceMock, userCryptoServiceMock, cryptoServiceMock,
    dateBalanceRepositoryMock, clockMock)

  @Test
  fun `should retrieve total balances insights`() {
    val cryptos = listOf("bitcoin", "tether", "ethereum", "litecoin")
    val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
    val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum", "litecoin"))
    } returns cryptosEntities

    val balances = insightsService.retrieveTotalBalances()

    assertThat(balances)
      .usingRecursiveComparison()
      .isEqualTo(
        TotalBalancesResponse(
          fiat = FiatBalance("7108.39", "6484.23"),
          btc = "0.25127936",
          stablecoins = "199.92"
        )
      )
  }

  @Test
  fun `should retrieve empty for total balances insights`() {
    every { userCryptoServiceMock.findAll() } returns emptyList()

    val balances = insightsService.retrieveTotalBalances()

    assertThat(balances)
      .usingRecursiveComparison()
      .isEqualTo(TotalBalancesResponse.EMPTY)
  }

  @Test
  fun `should retrieve dates balances for LAST_DAY`() {
    val now = LocalDate.of(2024, 3, 17)
    val dateBalances = listOf(
      DateBalance(date = now.minusDays(1), usdBalance = "1000", eurBalance = "918.45", btcBalance = "0.01438911"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1377.67", btcBalance = "0.021583665")
    )
    val from = now.minusDays(1).toString()
    val to = now.toString()

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every {
      dateBalanceRepositoryMock.findDateBalancesByInclusiveDateBetween(from, to)
    } returns dateBalances

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.LAST_DAY)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          DatesBalanceResponse(
            datesBalances = listOf(
              DateBalances("16 March 2024", Balances(FiatBalance("1000", "918.45"), "0.01438911")),
              DateBalances("17 March 2024", Balances(FiatBalance("1500", "1377.67"), "0.021583665"))
            ),
            change = BalanceChanges(50F, 50F, 50F),
            priceDifference = DifferencesChanges("500", "459.22", "0.007194555")
          )
        )
      )
  }

  @Test
  fun `should retrieve dates balances for THREE_DAYS`() {
    val now = LocalDate.of(2024, 3, 17)
    val dateBalances = listOf(
      DateBalance(date = now.minusDays(2), usdBalance = "1200", eurBalance = "1102.14", btcBalance = "0.020689655"),
      DateBalance(date = now.minusDays(1), usdBalance = "1000", eurBalance = "918.85", btcBalance = "0.017241379"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1378.27", btcBalance = "0.025862069")
    )
    val from = now.minusDays(2).toString()
    val to = now.toString()

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { dateBalanceRepositoryMock.findDateBalancesByInclusiveDateBetween(from, to) } returns dateBalances

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.THREE_DAYS)

    assertTrue(datesBalances.isPresent)
    assertEquals(3, datesBalances.get().datesBalances.size)
    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          DatesBalanceResponse(
            datesBalances = listOf(
              DateBalances("15 March 2024", Balances(FiatBalance("1200", "1102.14"), "0.020689655")),
              DateBalances("16 March 2024", Balances(FiatBalance("1000", "918.85"), "0.017241379")),
              DateBalances("17 March 2024", Balances(FiatBalance("1500", "1378.27"), "0.025862069"))
            ),
            change = BalanceChanges(25F, 25.05F, 25F),
            priceDifference = DifferencesChanges("300", "276.13", "0.005172414")
          )
        )
      )
  }

  @Test
  fun `should retrieve dates balances for ONE_WEEK`() {
    val now = LocalDate.of(2024, 3, 17)
    val dateBalances = listOf(
      DateBalance(date = now.minusDays(3), usdBalance = "1200", eurBalance = "1102.62", btcBalance = "0.020689655"),
      DateBalance(date = now.minusDays(2), usdBalance = "900", eurBalance = "823.63", btcBalance = "0.015789474"),
      DateBalance(date = now.minusDays(1), usdBalance = "1000", eurBalance = "913", btcBalance = "0.016806723"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1377.67", btcBalance = "0.025862069")
    )
    val from = now.minusDays(6).toString()
    val to = now.toString()

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every {
      dateBalanceRepositoryMock.findDateBalancesByInclusiveDateBetween(from, to)
    } returns dateBalances

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.ONE_WEEK)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          DatesBalanceResponse(
            datesBalances = listOf(
              DateBalances("14 March 2024", Balances(FiatBalance("1200", "1102.62"), "0.020689655")),
              DateBalances("15 March 2024", Balances(FiatBalance("900", "823.63"), "0.015789474")),
              DateBalances("16 March 2024", Balances(FiatBalance("1000", "913"), "0.016806723")),
              DateBalances("17 March 2024", Balances(FiatBalance("1500", "1377.67"), "0.025862069"))
            ),
            change = BalanceChanges(25F, 24.95F, 25F),
            priceDifference = DifferencesChanges("300", "275.05", "0.005172414")
          )
        )
      )
  }

  @Test
  fun `should retrieve dates balances for ONE_MONTH`() {
    val now = LocalDate.of(2024, 3, 17)
    val dateBalances = listOf(
      DateBalance(date = now.minusDays(6), usdBalance = "1200", eurBalance = "1102.14", btcBalance = "0.020530368"),
      DateBalance(date = now.minusDays(4), usdBalance = "900", eurBalance = "826.61", btcBalance = "0.015544041"),
      DateBalance(date = now.minusDays(2), usdBalance = "1000", eurBalance = "918.45", btcBalance = "0.016906171"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1377.67", btcBalance = "0.025862069")
    )
    val dates = getMockDates(now.minusMonths(1), now, 2)

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { dateBalanceRepositoryMock.findAllByDateIn(dates) } returns dateBalances

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.ONE_MONTH)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          DatesBalanceResponse(
            datesBalances = listOf(
              DateBalances("11 March 2024", Balances(FiatBalance("1200", "1102.14"), "0.020530368")),
              DateBalances("13 March 2024", Balances(FiatBalance("900", "826.61"), "0.015544041")),
              DateBalances("15 March 2024", Balances(FiatBalance("1000", "918.45"), "0.016906171")),
              DateBalances("17 March 2024", Balances(FiatBalance("1500", "1377.67"), "0.025862069"))
            ),
            change = BalanceChanges(25F, 25F, 25.97F),
            priceDifference = DifferencesChanges("300", "275.53", "0.005331701")
          )
        )
      )
  }

  @Test
  fun `should retrieve dates balances for THREE_MONTHS`() {
    val now = LocalDate.of(2024, 3, 17)
    val dateBalances = listOf(
      DateBalance(date = now.minusDays(24), usdBalance = "1150", eurBalance = "1067.03", btcBalance = "0.019827586"),
      DateBalance(date = now.minusDays(18), usdBalance = "1200", eurBalance = "1108.50", btcBalance = "0.020512821"),
      DateBalance(date = now.minusDays(12), usdBalance = "900", eurBalance = "830.38", btcBalance = "0.015319149"),
      DateBalance(date = now.minusDays(6), usdBalance = "1000", eurBalance = "921.15", btcBalance = "0.016949153"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1372.73", btcBalance = "0.025")
    )
    val dates = getMockDates(now.minusMonths(3), now, 6)

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { dateBalanceRepositoryMock.findAllByDateIn(dates) } returns dateBalances

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.THREE_MONTHS)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          DatesBalanceResponse(
            datesBalances = listOf(
              DateBalances("22 February 2024", Balances(FiatBalance("1150", "1067.03"), "0.019827586")),
              DateBalances("28 February 2024", Balances(FiatBalance("1200", "1108.50"), "0.020512821")),
              DateBalances("5 March 2024", Balances(FiatBalance("900", "830.38"), "0.015319149")),
              DateBalances("11 March 2024", Balances(FiatBalance("1000", "921.15"), "0.016949153")),
              DateBalances("17 March 2024", Balances(FiatBalance("1500", "1372.73"), "0.025"))
            ),
            change = BalanceChanges(30.43F, 28.65F, 26.09F),
            priceDifference = DifferencesChanges("350", "305.70", "0.005172414")
          )
        )
      )
  }

  @Test
  fun `should retrieve dates balances for SIX_MONTHS`() {
    val now = LocalDate.of(2024, 3, 17)
    val dateBalances = listOf(
      DateBalance(date = now.minusDays(50), usdBalance = "1150", eurBalance = "1057.14", btcBalance = "0.019827586"),
      DateBalance(date = now.minusDays(40), usdBalance = "1150", eurBalance = "1060.30", btcBalance = "0.019311503"),
      DateBalance(date = now.minusDays(30), usdBalance = "1200", eurBalance = "1113.18", btcBalance = "0.020689655"),
      DateBalance(date = now.minusDays(20), usdBalance = "900", eurBalance = "840.46", btcBalance = "0.015062762"),
      DateBalance(date = now.minusDays(10), usdBalance = "1000", eurBalance = "923.75", btcBalance = "0.016666667"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1381.73", btcBalance = "0.025062657")
    )
    val dates = getMockDates(now.minusMonths(6), now, 10)

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { dateBalanceRepositoryMock.findAllByDateIn(dates) } returns dateBalances

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.SIX_MONTHS)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          DatesBalanceResponse(
            datesBalances = listOf(
              DateBalances("27 January 2024", Balances(FiatBalance("1150", "1057.14"), "0.019827586")),
              DateBalances("6 February 2024", Balances(FiatBalance("1150", "1060.30"), "0.019311503")),
              DateBalances("16 February 2024", Balances(FiatBalance("1200", "1113.18"), "0.020689655")),
              DateBalances("26 February 2024", Balances(FiatBalance("900", "840.46"), "0.015062762")),
              DateBalances("7 March 2024", Balances(FiatBalance("1000", "923.75"), "0.016666667")),
              DateBalances("17 March 2024", Balances(FiatBalance("1500", "1381.73"), "0.025062657"))
            ),
            change = BalanceChanges(30.43F, 30.7F, 26.4F),
            priceDifference = DifferencesChanges("350", "324.59", "0.005235071")
          )
        )
      )
  }

  @Test
  fun `should retrieve dates balances for ONE_YEAR`() {
    val now = LocalDate.of(2024, 3, 17)
    val dateBalances = listOf(
      DateBalance(date = now.minusMonths(5), usdBalance = "1150", eurBalance = "1024.36", btcBalance = "0.020909091"),
      DateBalance(date = now.minusMonths(4), usdBalance = "1150", eurBalance = "1054.66", btcBalance = "0.020720721"),
      DateBalance(date = now.minusMonths(3), usdBalance = "1200", eurBalance = "1101.42", btcBalance = "0.021428571"),
      DateBalance(date = now.minusMonths(2), usdBalance = "900", eurBalance = "827.33", btcBalance = "0.015929204"),
      DateBalance(date = now.minusMonths(1), usdBalance = "1000", eurBalance = "928.25", btcBalance = "0.01754386"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1378.20", btcBalance = "0.025862069")
    )
    val dates = getMockDates(now)

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { dateBalanceRepositoryMock.findAllByDateIn(dates) } returns dateBalances

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.ONE_YEAR)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          DatesBalanceResponse(
            datesBalances = listOf(
              DateBalances("17 October 2023", Balances(FiatBalance("1150", "1024.36"), "0.020909091")),
              DateBalances("17 November 2023", Balances(FiatBalance("1150", "1054.66"), "0.020720721")),
              DateBalances("17 December 2023", Balances(FiatBalance("1200", "1101.42"), "0.021428571")),
              DateBalances("17 January 2024", Balances(FiatBalance("900", "827.33"), "0.015929204")),
              DateBalances("17 February 2024", Balances(FiatBalance("1000", "928.25"), "0.01754386")),
              DateBalances("17 March 2024", Balances(FiatBalance("1500", "1378.20"), "0.025862069"))
            ),
            change = BalanceChanges(30.43F, 34.54F, 23.69F),
            priceDifference = DifferencesChanges("350", "353.84", "0.004952978")
          )
        )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = ["ONE_MONTH", "THREE_MONTHS", "SIX_MONTHS", "ONE_YEAR"])
  fun `should retrieve last twelve days balances for ONE_MONTH, THREE_MONTHS, SIX_MONTHS and ONE_YEAR`(dateRange: String) {
    val now = LocalDate.of(2024, 3, 17)
    val dateBalances = listOf(
      DateBalance(date = now.minusDays(4), usdBalance = "900", eurBalance = "826.92", btcBalance = "0.015397776"),
      DateBalance(date = now.minusDays(2), usdBalance = "1000", eurBalance = "918.80", btcBalance = "0.016949153"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1378.20", btcBalance = "0.025359256")
    )
    val lastTwelvesDaysBalances = listOf(
      DateBalance(date = now.minusDays(3), usdBalance = "950", eurBalance = "872.86", btcBalance = "0.016225448"),
      DateBalance(date = now.minusDays(2), usdBalance = "1000", eurBalance = "918.80", btcBalance = "0.016949153"),
      DateBalance(date = now.minusDays(1), usdBalance = "900", eurBalance = "826.92", btcBalance = "0.015397776"),
      DateBalance(date = now, usdBalance = "1500", eurBalance = "1378.20", btcBalance = "0.025359256")
    )
    val from = now.minusDays(12).toString()
    val to = now.toString()

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { dateBalanceRepositoryMock.findAllByDateIn(any()) } returns dateBalances
    every { dateBalanceRepositoryMock.findDateBalancesByInclusiveDateBetween(from, to) } returns lastTwelvesDaysBalances

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.valueOf(dateRange))

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          DatesBalanceResponse(
            datesBalances = listOf(
              DateBalances("14 March 2024", Balances(FiatBalance("950", "872.86"), "0.016225448")),
              DateBalances("15 March 2024", Balances(FiatBalance("1000", "918.80"), "0.016949153")),
              DateBalances("16 March 2024", Balances(FiatBalance("900", "826.92"), "0.015397776")),
              DateBalances("17 March 2024", Balances(FiatBalance("1500", "1378.20"), "0.025359256"))
            ),
            change = BalanceChanges(57.89F, 57.89F, 56.29F),
            priceDifference = DifferencesChanges("550", "505.34", "0.009133808")
          )
        )
      )
  }

  @Test
  fun `should retrieve null for dates balances`() {
    val now = LocalDate.of(2024, 3, 17)
    val from = now.minusDays(6).toString()
    val to = now.toString()

    every { clockMock.instant() } returns now.atStartOfDay().toInstant(ZoneOffset.UTC)
    every { clockMock.zone } returns now.atStartOfDay().atZone(ZoneId.of("UTC")).zone
    every { dateBalanceRepositoryMock.findDateBalancesByInclusiveDateBetween(from, to) } returns emptyList()

    val datesBalances = insightsService.retrieveDatesBalances(DateRange.ONE_WEEK)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(Optional.empty<DatesBalanceResponse>())
  }

  @Test
  fun `should retrieve platform insights with one crypto`() {
    val platformEntity = getPlatformEntity()
    val userCryptos = getUserCrypto()
    val bitcoinCryptoEntity = getCryptoEntity()

    every {
      platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
    } returns platformEntity
    every {
      userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")
    } returns listOf(userCryptos)
    every {
      cryptoServiceMock.findAllByIds(listOf("bitcoin"))
    } returns listOf(bitcoinCryptoEntity)

    val platformInsightsResponse = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

    assertThat(platformInsightsResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          PlatformInsightsResponse(
            platformName = "BINANCE",
            balances = Balances(FiatBalance("7500.00", "6750.00"), "0.25"),
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
                balances = Balances(FiatBalance("7500.00", "6750.00"), "0.25")
              )
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
      changePercentageIn24h = BigDecimal("-2.75"),
      changePercentageIn7d = BigDecimal("10.25"),
      changePercentageIn30d = BigDecimal("-5.15"),
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

    val platformInsightsResponse = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

    assertThat(platformInsightsResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          PlatformInsightsResponse(
            platformName = "BINANCE",
            balances = Balances(FiatBalance("7925.00", "7147.00"), "0.266554"),
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
                percentage = 94.64f,
                balances = Balances(FiatBalance("7500.00", "6750.00"), "0.25")
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
                percentage = 5.36f,
                balances = Balances(FiatBalance("425.00", "397.00"), "0.016554")
              )
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve null if no cryptos are found for retrievePlatformInsights`() {
    every {
      userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")
    } returns emptyList()

    val platformInsights = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

    assertThat(platformInsights)
      .usingRecursiveComparison()
      .isEqualTo(Optional.empty<PlatformInsightsResponse>())
  }

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

    val cryptoInsightsResponse = insightsService.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsightsResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          CryptoInsightResponse(
            cryptoName = "Bitcoin",
            balances = Balances(FiatBalance("7500.00", "6750.00"), "0.25"),
            platforms = listOf(
              PlatformInsight(
                quantity = "0.25",
                balances = Balances(FiatBalance("7500.00", "6750.00"), "0.25"),
                percentage = 100f,
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
        listOf(
          "123e4567-e89b-12d3-a456-426614174111",
          "fa3db02d-4d43-416a-951b-e7ea3a4fe386"
        )
      )
    } returns listOf(binancePlatform, coinbasePlatform)
    every {
      cryptoServiceMock.retrieveCryptoInfoById("bitcoin")
    } returns bitcoinCryptoEntity

    val cryptoInsightResponse = insightsService.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsightResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        Optional.of(
          CryptoInsightResponse(
            cryptoName = "Bitcoin",
            balances = Balances(FiatBalance("8536.50", "7682.85"), "0.28455"),
            platforms = listOf(
              PlatformInsight(
                quantity = "0.25",
                balances = Balances(FiatBalance("7500.00", "6750.00"), "0.25"),
                percentage = 87.86f,
                platformName = "BINANCE"
              ),
              PlatformInsight(
                quantity = "0.03455",
                balances = Balances(FiatBalance("1036.50", "932.85"), "0.03455"),
                percentage = 12.14f,
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

    val cryptoInsightResponse = insightsService.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsightResponse)
      .usingRecursiveComparison()
      .isEqualTo(Optional.empty<CryptoInsightResponse>())
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

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      platformServiceMock.findAllByIds(
        setOf(
          "163b1731-7a24-4e23-ac90-dc95ad8cb9e8",
          "a76b400e-8ffc-42d6-bf47-db866eb20153"
        )
      )
    } returns listOf(binancePlatform, coinbasePlatform)
    every {
      cryptoServiceMock.findAllByIds(
        setOf(
          "bitcoin",
          "tether",
          "ethereum",
          "litecoin"
        )
      )
    } returns cryptosEntities

    val platformBalancesInsightsResponse = insightsService.retrievePlatformsBalancesInsights()

    assertThat(platformBalancesInsightsResponse)
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

    val platformBalancesInsightsResponse = insightsService.retrievePlatformsBalancesInsights()

    assertThat(platformBalancesInsightsResponse)
      .usingRecursiveComparison()
      .isEqualTo(emptyList<BalancesChartResponse>())
  }

  @Test
  fun `should retrieve cryptos balances insights`() {
    val cryptos = listOf("bitcoin", "tether", "ethereum", "litecoin")
    val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
    val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      cryptoServiceMock.findAllByIds(
        setOf(
          "bitcoin",
          "tether",
          "ethereum",
          "litecoin"
        )
      )
    } returns cryptosEntities

    val cryptosBalancesInsightsResponse = insightsService.retrieveCryptosBalancesInsights()

    assertThat(cryptosBalancesInsightsResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          BalancesChartResponse("Bitcoin", "4500.00", 63.31F),
          BalancesChartResponse("Ethereum", "2219.13", 31.22F),
          BalancesChartResponse("Tether", "199.92", 2.81F),
          BalancesChartResponse("Litecoin", "189.34", 2.66F),
        )
      )
  }

  @Test
  fun `should retrieve cryptos balances insights with others`() {
    every { userCryptoServiceMock.findAll() } returns userCryptos()
    every {
      cryptoServiceMock.findAllByIds(
        setOf(
          "bitcoin",
          "tether",
          "ethereum",
          "litecoin",
          "binancecoin",
          "ripple",
          "cardano",
          "polkadot",
          "solana",
          "matic-network",
          "chainlink",
          "dogecoin",
          "avalanche-2",
          "uniswap"
        )
      )
    } returns cryptos()

    val cryptosBalancesInsightsResponse = insightsService.retrieveCryptosBalancesInsights()

    assertThat(cryptosBalancesInsightsResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        listOf(
          BalancesChartResponse("Bitcoin", "4500.00", 53.74F),
          BalancesChartResponse("Ethereum", "2219.13", 26.5F),
          BalancesChartResponse("Avalanche", "232.50", 2.78F),
          BalancesChartResponse("BNB", "211.79", 2.53F),
          BalancesChartResponse("Chainlink", "209.65", 2.5F),
          BalancesChartResponse("Tether", "199.92", 2.39F),
          BalancesChartResponse("Litecoin", "189.34", 2.26F),
          BalancesChartResponse("Solana", "180.40", 2.15F),
          BalancesChartResponse("Polkadot", "160.40", 1.92F),
          BalancesChartResponse("Uniswap", "127.50", 1.52F),
          BalancesChartResponse("Polygon", "51.00", 0.61F),
          BalancesChartResponse("Cardano", "37.34", 0.45F),
          BalancesChartResponse("Others", "54.66", 0.65F),
        )
      )
  }

  @Test
  fun `should retrieve empty if no cryptos are found for retrieveCryptosBalancesInsights`() {
    every { userCryptoServiceMock.findAll() } returns emptyList()

    val cryptosBalancesInsightsResponse = insightsService.retrieveCryptosBalancesInsights()

    assertThat(cryptosBalancesInsightsResponse)
      .usingRecursiveComparison()
      .isEqualTo(emptyList<BalancesChartResponse>())
  }

  @Test
  fun `should retrieve user cryptos insights`() {
    val cryptos = listOf("bitcoin", "ethereum", "tether")
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

    every { cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum")) } returns cryptosEntities
    every {
      platformServiceMock.findAllByIds(
        setOf(
          "163b1731-7a24-4e23-ac90-dc95ad8cb9e8",
          "a76b400e-8ffc-42d6-bf47-db866eb20153"
        )
      )
    } returns listOf(binancePlatform, coinbasePlatform)
    every { userCryptoServiceMock.findAll() } returns userCryptos

    val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(0)

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
                  changePercentageIn24h = BigDecimal("10.00"),
                  changePercentageIn7d = BigDecimal("-5.00"),
                  changePercentageIn30d = BigDecimal("0.00")
                )
              ),
              quantity = "0.15",
              percentage = 65.04f,
              balances = Balances(FiatBalance("4500.00", "4050.00"), "0.15")
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
                  changePercentageIn24h = BigDecimal("10.00"),
                  changePercentageIn7d = BigDecimal("-5.00"),
                  changePercentageIn30d = BigDecimal("2.00")
                )
              ),
              quantity = "1.372",
              percentage = 32.07f,
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
                  changePercentageIn24h = BigDecimal("0.00"),
                  changePercentageIn7d = BigDecimal("0.00"),
                  changePercentageIn30d = BigDecimal("0.00")
                )
              ),
              quantity = "200",
              percentage = 2.89f,
              balances = Balances(FiatBalance("199.92", "186.62"), "0.00776")
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve user cryptos insights with next page`() {
    val binancePlatform = Platform(
      id = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8",
      name = "BINANCE"
    )
    val coinbasePlatform = Platform(
      id = "a76b400e-8ffc-42d6-bf47-db866eb20153",
      name = "COINBASE"
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
          "cardano",
          "polkadot",
          "solana",
          "matic-network",
          "chainlink",
          "dogecoin",
          "avalanche-2",
          "uniswap"
        )
      )
    } returns cryptos()
    every {
      platformServiceMock.findAllByIds(
        setOf(
          "163b1731-7a24-4e23-ac90-dc95ad8cb9e8",
          "a76b400e-8ffc-42d6-bf47-db866eb20153"
        )
      )
    } returns listOf(binancePlatform, coinbasePlatform)
    every { userCryptoServiceMock.findAll() } returns userCryptos()

    val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(0)

    assertThat(userCryptosPlatformsInsights.cryptos.size).isEqualTo(10)
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
                  changePercentageIn24h = BigDecimal("10.00"),
                  changePercentageIn7d = BigDecimal("-5.00"),
                  changePercentageIn30d = BigDecimal("0.00")
                )
              ),
              quantity = "0.15",
              percentage = 53.74f,
              balances = Balances(FiatBalance("4500.00", "4050.00"), "0.15")
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
                  changePercentageIn24h = BigDecimal("10.00"),
                  changePercentageIn7d = BigDecimal("-5.00"),
                  changePercentageIn30d = BigDecimal("2.00")
                )
              ),
              quantity = "1.372",
              percentage = 26.5f,
              balances = Balances(FiatBalance("2219.13", "2070.86"), "0.08616648")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Avalanche",
                coingeckoCryptoId = "avalanche-2",
                symbol = "avax",
                image = "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574",
                price = Price(
                  usd = "9.3",
                  eur = "8.67",
                  btc = "0.00035516"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = BigDecimal("4.00"),
                  changePercentageIn7d = BigDecimal("1.00"),
                  changePercentageIn30d = BigDecimal("8.00")
                )
              ),
              quantity = "25",
              percentage = 2.78f,
              balances = Balances(FiatBalance("232.50", "216.75"), "0.008879")
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
                  changePercentageIn24h = BigDecimal("6.00"),
                  changePercentageIn7d = BigDecimal("-2.00"),
                  changePercentageIn30d = BigDecimal("12.00")
                )
              ),
              quantity = "1",
              percentage = 2.53f,
              balances = Balances(FiatBalance("211.79", "197.80"), "0.00811016")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Chainlink",
                coingeckoCryptoId = "chainlink",
                symbol = "link",
                image = "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1547034700",
                price = Price(
                  usd = "5.99",
                  eur = "5.58",
                  btc = "0.00022866"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = BigDecimal("4.00"),
                  changePercentageIn7d = BigDecimal("-1.00"),
                  changePercentageIn30d = BigDecimal("8.00")
                )
              ),
              quantity = "35",
              percentage = 2.5f,
              balances = Balances(FiatBalance("209.65", "195.30"), "0.0080031")
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
                  changePercentageIn24h = BigDecimal("0.00"),
                  changePercentageIn7d = BigDecimal("0.00"),
                  changePercentageIn30d = BigDecimal("0.00")
                )
              ),
              quantity = "200",
              percentage = 2.39f,
              balances = Balances(FiatBalance("199.92", "186.62"), "0.00776")
            ),
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
                  changePercentageIn24h = BigDecimal("6.00"),
                  changePercentageIn7d = BigDecimal("-2.00"),
                  changePercentageIn30d = BigDecimal("12.00")
                )
              ),
              quantity = "3.125",
              percentage = 2.26f,
              balances = Balances(FiatBalance("189.34", "176.75"), "0.00735288")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Solana",
                coingeckoCryptoId = "solana",
                symbol = "sol",
                image = "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422",
                price = Price(
                  usd = "18.04",
                  eur = "16.82",
                  btc = "0.00068809"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = BigDecimal("4.00"),
                  changePercentageIn7d = BigDecimal("1.00"),
                  changePercentageIn30d = BigDecimal("-2.00")
                )
              ),
              quantity = "10",
              percentage = 2.15f,
              balances = Balances(FiatBalance("180.40", "168.20"), "0.0068809")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Polkadot",
                coingeckoCryptoId = "polkadot",
                symbol = "dot",
                image = "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644",
                price = Price(
                  usd = "4.01",
                  eur = "3.73",
                  btc = "0.00015302"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = BigDecimal("4.00"),
                  changePercentageIn7d = BigDecimal("-1.00"),
                  changePercentageIn30d = BigDecimal("2.00")
                )
              ),
              quantity = "40",
              percentage = 1.92f,
              balances = Balances(FiatBalance("160.40", "149.20"), "0.0061208")
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Uniswap",
                coingeckoCryptoId = "uniswap",
                symbol = "uni",
                image = "https://assets.coingecko.com/coins/images/12504/large/uni.jpg?1687143398",
                price = Price(
                  usd = "4.25",
                  eur = "3.96",
                  btc = "0.00016197"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = BigDecimal("2.00"),
                  changePercentageIn7d = BigDecimal("-1.00"),
                  changePercentageIn30d = BigDecimal("3.00")
                )
              ),
              quantity = "30",
              percentage = 1.52f,
              balances = Balances(FiatBalance("127.50", "118.80"), "0.0048591")
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve user cryptos insights for second page`() {
    val binancePlatform = Platform(
      id = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8",
      name = "BINANCE"
    )
    val coinbasePlatform = Platform(
      id = "a76b400e-8ffc-42d6-bf47-db866eb20153",
      name = "COINBASE"
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
          "cardano",
          "polkadot",
          "solana",
          "matic-network",
          "chainlink",
          "dogecoin",
          "avalanche-2",
          "uniswap"
        )
      )
    } returns cryptos()
    every {
      platformServiceMock.findAllByIds(
        setOf(
          "163b1731-7a24-4e23-ac90-dc95ad8cb9e8",
          "a76b400e-8ffc-42d6-bf47-db866eb20153"
        )
      )
    } returns listOf(binancePlatform, coinbasePlatform)
    every { userCryptoServiceMock.findAll() } returns userCryptos()

    val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(1)

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
                cryptoName = "Polygon",
                coingeckoCryptoId = "matic-network",
                symbol = "matic",
                image = "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png?1624446912",
                price = Price(
                  usd = "0.509995",
                  eur = "0.475407",
                  btc = "0.00001947"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = BigDecimal("14.00"),
                  changePercentageIn7d = BigDecimal("-10.00"),
                  changePercentageIn30d = BigDecimal("2.00")
                )
              ),
              quantity = "100",
              percentage = 0.61f,
              balances = Balances(FiatBalance("51.00", "47.54"), "0.001947")
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
                  changePercentageIn24h = BigDecimal("7.00"),
                  changePercentageIn7d = BigDecimal("1.00"),
                  changePercentageIn30d = BigDecimal("-2.00")
                )
              ),
              quantity = "150",
              percentage = 0.45f,
              balances = Balances(FiatBalance("37.34", "34.80"), "0.001425"),
            ),
            UserCryptoInsights(
              cryptoInfo = CryptoInfo(
                cryptoName = "Dogecoin",
                coingeckoCryptoId = "dogecoin",
                symbol = "doge",
                image = "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1547792256",
                price = Price(
                  usd = "0.061481",
                  eur = "0.057319",
                  btc = "0.00000235"
                ),
                priceChange = PriceChange(
                  changePercentageIn24h = BigDecimal("-4.00"),
                  changePercentageIn7d = BigDecimal("-1.00"),
                  changePercentageIn30d = BigDecimal("-8.00")
                )
              ),
              quantity = "500",
              percentage = 0.37f,
              balances = Balances(FiatBalance("30.74", "28.66"), "0.001175")
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
                  changePercentageIn24h = BigDecimal("2.00"),
                  changePercentageIn7d = BigDecimal("3.00"),
                  changePercentageIn30d = BigDecimal("-5.00")
                )
              ),
              quantity = "50",
              percentage = 0.29f,
              balances = Balances(FiatBalance("23.92", "22.33"), "0.0009165")
            )
          )
        )
      )
  }

  @Test
  fun `should retrieve null if no user cryptos are found for retrieveUserCryptosInsights`() {
    every { userCryptoServiceMock.findAll() } returns emptyList()

    val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(0)

    assertThat(userCryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(PageUserCryptosInsightsResponse.EMPTY)
  }

  @Test
  fun `should retrieve null if no user cryptos are found for page for retrieveUserCryptosInsights`() {
    val cryptos = listOf("bitcoin", "ethereum", "tether")
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

    every { cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum")) } returns cryptosEntities
    every {
      platformServiceMock.findAllByIds(
        setOf(
          "163b1731-7a24-4e23-ac90-dc95ad8cb9e8",
          "a76b400e-8ffc-42d6-bf47-db866eb20153"
        )
      )
    } returns listOf(binancePlatform, coinbasePlatform)
    every { userCryptoServiceMock.findAll() } returns userCryptos

    val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(1)

    assertThat(userCryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(PageUserCryptosInsightsResponse.EMPTY)
  }

  private fun userCryptos(): List<UserCrypto> {
    return listOf(
      UserCrypto(
        id = "676fb38a-556e-11ee-b56e-325096b39f47",
        coingeckoCryptoId = "bitcoin",
        quantity = BigDecimal("0.15"),
        platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
      ),
      UserCrypto(
        id = "676fb600-556e-11ee-83b6-325096b39f47",
        coingeckoCryptoId = "tether",
        quantity = BigDecimal("200"),
        platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
      ),
      UserCrypto(
        id = "676fb696-556e-11ee-aa1c-325096b39f47",
        coingeckoCryptoId = "ethereum",
        quantity = BigDecimal("0.26"),
        platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
      ),
      UserCrypto(
        id = "676fba74-556e-11ee-9bff-325096b39f47",
        coingeckoCryptoId = "ethereum",
        quantity = BigDecimal("1.112"),
        platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
      ),
      UserCrypto(
        id = "676fb70e-556e-11ee-8c2c-325096b39f47",
        coingeckoCryptoId = "litecoin",
        quantity = BigDecimal("3.125"),
        platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
      ),
      UserCrypto(
        id = "676fb768-556e-11ee-8b42-325096b39f47",
        coingeckoCryptoId = "binancecoin",
        quantity = BigDecimal("1"),
        platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
      ),
      UserCrypto(
        id = "676fb7c2-556e-11ee-9800-325096b39f47",
        coingeckoCryptoId = "ripple",
        quantity = BigDecimal("50"),
        platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
      ),
      UserCrypto(
        id = "676fb83a-556e-11ee-9731-325096b39f47",
        coingeckoCryptoId = "cardano",
        quantity = BigDecimal("150"),
        platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
      ),
      UserCrypto(
        id = "676fb89e-556e-11ee-b0b8-325096b39f47",
        coingeckoCryptoId = "polkadot",
        quantity = BigDecimal("40"),
        platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
      ),
      UserCrypto(
        id = "676fb8e4-556e-11ee-883e-325096b39f47",
        coingeckoCryptoId = "solana",
        quantity = BigDecimal("10"),
        platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
      ),
      UserCrypto(
        id = "676fb92a-556e-11ee-9de1-325096b39f47",
        coingeckoCryptoId = "matic-network",
        quantity = BigDecimal("100"),
        platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
      ),
      UserCrypto(
        id = "676fb966-556e-11ee-81d6-325096b39f47",
        coingeckoCryptoId = "chainlink",
        quantity = BigDecimal("35"),
        platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
      ),
      UserCrypto(
        id = "676fb9ac-556e-11ee-b4fa-325096b39f47",
        coingeckoCryptoId = "dogecoin",
        quantity = BigDecimal("500"),
        platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
      ),
      UserCrypto(
        id = "676fb9f2-556e-11ee-a929-325096b39f47",
        coingeckoCryptoId = "avalanche-2",
        quantity = BigDecimal("25"),
        platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
      ),
      UserCrypto(
        id = "676fba2e-556e-11ee-a181-325096b39f47",
        coingeckoCryptoId = "uniswap",
        quantity = BigDecimal("30"),
        platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
      )
    )
  }

  private fun cryptos(): List<Crypto> {
    val localDateTime = LocalDateTime.now()

    return listOf(
      getCryptoEntity(),
      Crypto(
        id = "tether",
        name = "Tether",
        ticker = "usdt",
        image = "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663",
        circulatingSupply = BigDecimal("83016246102"),
        lastKnownPrice = BigDecimal("0.999618"),
        lastKnownPriceInBTC = BigDecimal("0.0000388"),
        lastKnownPriceInEUR = BigDecimal("0.933095"),
        marketCapRank = 3,
        marketCap = BigDecimal("95085861049"),
        changePercentageIn24h = BigDecimal("0.00"),
        changePercentageIn7d = BigDecimal("0.00"),
        changePercentageIn30d = BigDecimal("0.00"),
        maxSupply = BigDecimal.ZERO,
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "ethereum",
        name = "Ethereum",
        ticker = "eth",
        image = "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
        circulatingSupply = BigDecimal("120220572"),
        lastKnownPrice = BigDecimal("1617.44"),
        lastKnownPriceInBTC = BigDecimal("0.06280356"),
        lastKnownPriceInEUR = BigDecimal("1509.37"),
        maxSupply = BigDecimal.ZERO,
        marketCapRank = 2,
        marketCap = BigDecimal("298219864117"),
        changePercentageIn24h = BigDecimal("10.00"),
        changePercentageIn7d = BigDecimal("-5.00"),
        changePercentageIn30d = BigDecimal("2.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "litecoin",
        name = "Litecoin",
        ticker = "ltc",
        image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580",
        circulatingSupply = BigDecimal("73638701"),
        lastKnownPrice = BigDecimal("60.59"),
        lastKnownPriceInBTC = BigDecimal("0.00235292"),
        lastKnownPriceInEUR = BigDecimal("56.56"),
        maxSupply = BigDecimal("84000000"),
        marketCapRank = 19,
        marketCap = BigDecimal("5259205267"),
        changePercentageIn24h = BigDecimal("6.00"),
        changePercentageIn7d = BigDecimal("-2.00"),
        changePercentageIn30d = BigDecimal("12.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "binancecoin",
        name = "BNB",
        ticker = "bnb",
        image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850",
        circulatingSupply = BigDecimal("153856150"),
        lastKnownPrice = BigDecimal("211.79"),
        lastKnownPriceInBTC = BigDecimal("0.00811016"),
        lastKnownPriceInEUR = BigDecimal("197.80"),
        maxSupply = BigDecimal("200000000"),
        marketCapRank = 4,
        marketCap = BigDecimal("48318686968"),
        changePercentageIn24h = BigDecimal("6.00"),
        changePercentageIn7d = BigDecimal("-2.00"),
        changePercentageIn30d = BigDecimal("12.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "ripple",
        name = "XRP",
        ticker = "xrp",
        image = "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1605778731",
        circulatingSupply = BigDecimal("53083046512"),
        lastKnownPrice = BigDecimal("0.478363"),
        lastKnownPriceInBTC = BigDecimal("0.00001833"),
        lastKnownPriceInEUR = BigDecimal("0.446699"),
        maxSupply = BigDecimal("100000000000"),
        marketCapRank = 6,
        marketCap = BigDecimal("29348197308"),
        changePercentageIn24h = BigDecimal("2.00"),
        changePercentageIn7d = BigDecimal("3.00"),
        changePercentageIn30d = BigDecimal("-5.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "cardano",
        name = "Cardano",
        ticker = "ada",
        image = "https://assets.coingecko.com/coins/images/975/large/cardano.png?1547034860",
        circulatingSupply = BigDecimal("35045020830"),
        lastKnownPrice = BigDecimal("0.248915"),
        lastKnownPriceInBTC = BigDecimal("0.0000095"),
        lastKnownPriceInEUR = BigDecimal("0.231985"),
        maxSupply = BigDecimal("45000000000"),
        marketCapRank = 9,
        marketCap = BigDecimal("29348197308"),
        changePercentageIn24h = BigDecimal("7.00"),
        changePercentageIn7d = BigDecimal("1.00"),
        changePercentageIn30d = BigDecimal("-2.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "polkadot",
        name = "Polkadot",
        ticker = "dot",
        image = "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644",
        circulatingSupply = BigDecimal("1274258350"),
        lastKnownPrice = BigDecimal("4.01"),
        lastKnownPriceInBTC = BigDecimal("0.00015302"),
        lastKnownPriceInEUR = BigDecimal("3.73"),
        maxSupply = BigDecimal.ZERO,
        marketCapRank = 13,
        marketCap = BigDecimal("8993575127"),
        changePercentageIn24h = BigDecimal("4.00"),
        changePercentageIn7d = BigDecimal("-1.00"),
        changePercentageIn30d = BigDecimal("2.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "solana",
        name = "Solana",
        ticker = "sol",
        image = "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422",
        circulatingSupply = BigDecimal("410905807"),
        lastKnownPrice = BigDecimal("18.04"),
        lastKnownPriceInBTC = BigDecimal("0.00068809"),
        lastKnownPriceInEUR = BigDecimal("16.82"),
        maxSupply = BigDecimal.ZERO,
        marketCapRank = 5,
        marketCap = BigDecimal("40090766907"),
        changePercentageIn24h = BigDecimal("4.00"),
        changePercentageIn7d = BigDecimal("1.00"),
        changePercentageIn30d = BigDecimal("-2.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "matic-network",
        name = "Polygon",
        ticker = "matic",
        image = "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png?1624446912",
        circulatingSupply = BigDecimal("9319469069"),
        lastKnownPrice = BigDecimal("0.509995"),
        lastKnownPriceInBTC = BigDecimal("0.00001947"),
        lastKnownPriceInEUR = BigDecimal("0.475407"),
        maxSupply = BigDecimal("10000000000"),
        marketCapRank = 16,
        marketCap = BigDecimal("7001911961"),
        changePercentageIn24h = BigDecimal("14.00"),
        changePercentageIn7d = BigDecimal("-10.00"),
        changePercentageIn30d = BigDecimal("2.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "chainlink",
        name = "Chainlink",
        ticker = "link",
        image = "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1547034700",
        circulatingSupply = BigDecimal("538099971"),
        lastKnownPrice = BigDecimal("5.99"),
        lastKnownPriceInBTC = BigDecimal("0.00022866"),
        lastKnownPriceInEUR = BigDecimal("5.58"),
        maxSupply = BigDecimal("1000000000"),
        marketCapRank = 16,
        marketCap = BigDecimal("9021587267"),
        changePercentageIn24h = BigDecimal("4.00"),
        changePercentageIn7d = BigDecimal("-1.00"),
        changePercentageIn30d = BigDecimal("8.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "dogecoin",
        name = "Dogecoin",
        ticker = "doge",
        image = "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1547792256",
        circulatingSupply = BigDecimal("140978466383"),
        lastKnownPrice = BigDecimal("0.061481"),
        lastKnownPriceInBTC = BigDecimal("0.00000235"),
        lastKnownPriceInEUR = BigDecimal("0.057319"),
        maxSupply = BigDecimal.ZERO,
        marketCapRank = 11,
        marketCap = BigDecimal("11195832359"),
        changePercentageIn24h = BigDecimal("-4.00"),
        changePercentageIn7d = BigDecimal("-1.00"),
        changePercentageIn30d = BigDecimal("-8.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "avalanche-2",
        name = "Avalanche",
        ticker = "avax",
        image = "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574",
        circulatingSupply = BigDecimal("353804673"),
        lastKnownPrice = BigDecimal("9.3"),
        lastKnownPriceInBTC = BigDecimal("0.00035516"),
        lastKnownPriceInEUR = BigDecimal("8.67"),
        maxSupply = BigDecimal("720000000"),
        marketCapRank = 10,
        marketCap = BigDecimal("11953262327"),
        changePercentageIn24h = BigDecimal("4.00"),
        changePercentageIn7d = BigDecimal("1.00"),
        changePercentageIn30d = BigDecimal("8.00"),
        lastUpdatedAt = localDateTime
      ),
      Crypto(
        id = "uniswap",
        name = "Uniswap",
        ticker = "uni",
        image = "https://assets.coingecko.com/coins/images/12504/large/uni.jpg?1687143398",
        circulatingSupply = BigDecimal("753766667"),
        lastKnownPrice = BigDecimal("4.25"),
        lastKnownPriceInBTC = BigDecimal("0.00016197"),
        lastKnownPriceInEUR = BigDecimal("3.96"),
        maxSupply = BigDecimal("1000000000"),
        marketCapRank = 22,
        marketCap = BigDecimal("4772322900"),
        changePercentageIn24h = BigDecimal("2.00"),
        changePercentageIn7d = BigDecimal("-1.00"),
        changePercentageIn30d = BigDecimal("3.00"),
        lastUpdatedAt = localDateTime
      )
    )
  }

  private fun getMockDates(from: LocalDate, to: LocalDate, daysSubtraction: Int): List<String> {
    var mutableTo = to
    val dates: MutableList<String> = ArrayList()

    while (from.isBefore(mutableTo)) {
      dates.add(mutableTo.toString())
      mutableTo = mutableTo.minusDays(daysSubtraction.toLong())
    }

    return dates
  }

  private fun getMockDates(now: LocalDate): List<String> {
    val dates: MutableList<String> = ArrayList()
    dates.add(now.toString())

    IntStream.range(1, 12)
      .forEach { n: Int -> dates.add(now.minusMonths(n.toLong()).toString()) }

    return dates
  }
}
