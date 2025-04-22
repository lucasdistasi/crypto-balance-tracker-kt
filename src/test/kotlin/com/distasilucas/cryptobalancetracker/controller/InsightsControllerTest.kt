package com.distasilucas.cryptobalancetracker.controller

import balances
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.HomeInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.service.InsightsService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.math.BigDecimal
import java.util.*

class InsightsControllerTest {

  private val insightsServiceMock = mockk<InsightsService>()

  private val insightsController = InsightsController(insightsServiceMock)

  @Test
  fun `should retrieve total balances with status 200`() {
    val homeInsightsResponse = HomeInsightsResponse(
      Balances(FiatBalance("22822.29", "19927.78"), "0.25127936"),
      "199.92",
      CryptoInfo(
        coingeckoCryptoId = "bitcoin",
        symbol = "btc",
        image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
        price = Price("90824.40", "79305.30"),
        priceChange = PriceChange(10.00)
      )
    )

    every { insightsServiceMock.retrieveHomeInsightsResponse() } returns homeInsightsResponse

    val homeInsights = insightsController.retrieveHomeInsights()

    assertThat(homeInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(homeInsightsResponse))
  }

  @Test
  fun `should retrieve dates balances with status 200`() {
    val dateBalanceResponse = DatesBalanceResponse(
      datesBalances = listOf(
        DateBalances("16 March 2024", Balances(FiatBalance("1000", "918.45"), "0.01438911")),
        DateBalances("17 March 2024", Balances(FiatBalance("1500", "1377.67"), "0.021583665"))
      ),
      change = BalanceChanges(50F, 30F, 10F),
      priceDifference = DifferencesChanges("500", "459.22", "0.007194555")
    )

    every { insightsServiceMock.retrieveDatesBalances(DateRange.LAST_DAY) } returns Optional.of(dateBalanceResponse)

    val datesBalances = insightsController.retrieveDatesBalances(DateRange.LAST_DAY)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(dateBalanceResponse))
  }

  @Test
  fun `should retrieve cryptos insights with status 200`() {
    val pageUserCryptosInsightsResponse = PageUserCryptosInsightsResponse(
      page = 0,
      totalPages = 1,
      balances = balances(),
      cryptos = emptyList()
    )

    every { insightsServiceMock.retrieveUserCryptosInsights(0) } returns pageUserCryptosInsightsResponse

    val cryptosPlatformsInsights = insightsController.retrieveUserCryptosInsights(0)

    assertThat(cryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(pageUserCryptosInsightsResponse))
  }

  @Test
  fun `should retrieve empty for cryptos insights with status 204`() {
    every { insightsServiceMock.retrieveUserCryptosInsights(0) } returns PageUserCryptosInsightsResponse.EMPTY

    val cryptosPlatformsInsights = insightsController.retrieveUserCryptosInsights(0)

    assertThat(cryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<PageUserCryptosInsightsResponse>())
  }

  @Test
  fun `should retrieve cryptos balances insights with status 200`() {
    val cryptosBalancesInsightsResponse = listOf(BalancesChartResponse("Bitcoin", "50000.00", 100F))

    every { insightsServiceMock.retrieveCryptosBalancesInsights() } returns cryptosBalancesInsightsResponse

    val cryptosBalancesInsights = insightsController.retrieveCryptosBalancesInsights()

    assertThat(cryptosBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(cryptosBalancesInsightsResponse))
  }

  @Test
  fun `should retrieve platforms balances insights with status 200`() {
    val platformBalancesInsightsResponse = listOf(BalancesChartResponse("BINANCE", "50000.00", 100F))

    every { insightsServiceMock.retrievePlatformsBalancesInsights() } returns platformBalancesInsightsResponse

    val platformsBalancesInsights = insightsController.retrievePlatformsBalancesInsights()

    assertThat(platformsBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(platformBalancesInsightsResponse))
  }

  @Test
  fun `should retrieve crypto insights with status 200`() {
    val cryptoInsightResponse = CryptoInsightResponse(
      balances = balances(),
      cryptoName = "Bitcoin",
      platforms = emptyList()
    )

    every { insightsServiceMock.retrieveCryptoInsights("bitcoin") } returns Optional.of(cryptoInsightResponse)

    val cryptoInsights = insightsController.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(cryptoInsightResponse))
  }

  @Test
  fun `should retrieve crypto insights with status 204`() {
    every {
      insightsServiceMock.retrieveCryptoInsights("bitcoin")
    } returns Optional.empty()

    val cryptoInsights = insightsController.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<CryptoInsightResponse>())
  }

  @Test
  fun `should retrieve platform insights with status 200`() {
    val platformInsightsResponse = PlatformInsightsResponse(
      platformName = "BINANCE",
      balances = balances(),
      cryptos = emptyList()
    )

    every {
      insightsServiceMock.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")
    } returns Optional.of(platformInsightsResponse)

    val platformInsights = insightsController.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

    assertThat(platformInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(platformInsightsResponse))
  }

  @Test
  fun `should retrieve platform insights with status 204`() {
    every {
      insightsServiceMock.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")
    } returns Optional.empty()

    val platformInsights = insightsController.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

    assertThat(platformInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<PlatformInsightsResponse>())
  }
}
