package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.HomeInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import cryptos
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus
import userCryptos
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.stream.IntStream

class InsightsServiceTest {

  private val userCryptoServiceMock = mockk<UserCryptoService>()
  private val cryptoServiceMock = mockk<CryptoService>()
  private val dateBalanceRepositoryMock = mockk<DateBalanceRepository>()
  private val clockMock = mockk<Clock>()

  private val insightsService = InsightsService(userCryptoServiceMock, cryptoServiceMock, dateBalanceRepositoryMock, clockMock)

  @Test
  fun `should retrieve total balances insights`() {
    val cryptos = listOf("bitcoin", "tether", "ethereum", "litecoin")
    val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
    val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }
    val bitcoin = cryptosEntities.first { it.id == "bitcoin" }

    every { userCryptoServiceMock.findAll() } returns userCryptos
    every {
      cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum", "litecoin"))
    } returns cryptosEntities
    every {
      cryptoServiceMock.findTopGainer24h(setOf("bitcoin", "tether", "ethereum", "litecoin"))
    } returns bitcoin

    val homeInsightResponse = insightsService.retrieveHomeInsightsResponse()

    assertThat(homeInsightResponse)
      .usingRecursiveComparison()
      .isEqualTo(
        HomeInsightsResponse(
          Balances(FiatBalance("7108.40", "6484.22"), "0.25127936"),
          "199.92",
          CryptoInfo(
            coingeckoCryptoId = "bitcoin",
            symbol = "btc",
            image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
            price = Price("30000", "27000"),
            priceChange = PriceChange(10.00)
          )
        )
      )
  }

  @Test
  fun `should throw ApiException when calling retrieveHomeInsightsResponse`() {
    every { userCryptoServiceMock.findAll() } returns emptyList()

    val exception = assertThrows<ApiException> { insightsService.retrieveHomeInsightsResponse() }

    assertEquals(HttpStatus.NOT_FOUND, exception.httpStatusCode)
    assertEquals("No user cryptos were found", exception.message)
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
