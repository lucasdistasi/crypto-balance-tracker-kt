package com.distasilucas.cryptobalancetracker.controller

import balances
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.service.InsightsService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.util.Optional

class InsightsControllerTest {

  private val insightsServiceMock = mockk<InsightsService>()

  private val insightsController = InsightsController(insightsServiceMock)

  @Test
  fun `should retrieve total balances with status 200`() {
    val balances = balances()

    every { insightsServiceMock.retrieveTotalBalances() } returns balances

    val totalBalancesInsights = insightsController.retrieveTotalBalances()

    assertThat(totalBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(balances))
  }

  @Test
  fun `should retrieve zero for total balances when empty cryptos with status 200`() {
    every {
      insightsServiceMock.retrieveTotalBalances()
    } returns BalancesResponse("0", "0", "0")

    val totalBalancesInsights = insightsController.retrieveTotalBalances()

    assertThat(totalBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(BalancesResponse("0", "0", "0")))
  }

  @Test
  fun `should retrieve dates balances with status 200`() {
    val dateBalanceResponse = DatesBalanceResponse(
      datesBalances = listOf(
        DateBalances("16 March 2024", BalancesResponse("1000", "918.45", "0.01438911")),
        DateBalances("17 March 2024", BalancesResponse("1500", "1377.67", "0.021583665"))
      ),
      change = BalanceChanges(50F, 30F, 10F),
      priceDifference = DifferencesChanges("500", "459.22", "0.007194555")
    )

    every { insightsServiceMock.retrieveDatesBalances(DateRange.LAST_DAY) } returns dateBalanceResponse

    val datesBalances = insightsController.retrieveDatesBalances(DateRange.LAST_DAY)

    assertThat(datesBalances)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(dateBalanceResponse))
  }

  @Test
  fun `should retrieve cryptos insights with status 200`() {
    val pageUserCryptosInsightsResponse = PageUserCryptosInsightsResponse(
      page = 1,
      totalPages = 1,
      balances = balances(),
      cryptos = emptyList()
    )

    every {
      insightsServiceMock.retrieveUserCryptosInsights(0)
    } returns Optional.of(pageUserCryptosInsightsResponse)

    val userCryptosInsights = insightsController.retrieveUserCryptosInsights(0)

    assertThat(userCryptosInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(pageUserCryptosInsightsResponse))
  }

  @Test
  fun `should retrieve empty for cryptos insights with status 204`() {
    every {
      insightsServiceMock.retrieveUserCryptosInsights(0)
    } returns Optional.empty()

    val userCryptosInsights = insightsController.retrieveUserCryptosInsights(0)

    assertThat(userCryptosInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<PageUserCryptosInsightsResponse>())
  }

  @Test
  fun `should retrieve cryptos platforms insights with status 200`() {
    val pageUserCryptosInsightsResponse = PageUserCryptosInsightsResponse(
      page = 0,
      totalPages = 1,
      balances = balances(),
      cryptos = emptyList()
    )

    every {
      insightsServiceMock.retrieveUserCryptosPlatformsInsights(0)
    } returns Optional.of(pageUserCryptosInsightsResponse)

    val cryptosPlatformsInsights = insightsController.retrieveUserCryptosPlatformsInsights(0)

    assertThat(cryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(pageUserCryptosInsightsResponse))
  }

  @Test
  fun `should retrieve empty for cryptos platforms insights with status 204`() {
    every {
      insightsServiceMock.retrieveUserCryptosPlatformsInsights(0)
    } returns Optional.empty()

    val cryptosPlatformsInsights = insightsController.retrieveUserCryptosPlatformsInsights(0)

    assertThat(cryptosPlatformsInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<PageUserCryptosInsightsResponse>())
  }

  @Test
  fun `should retrieve cryptos balances insights with status 200`() {
    val cryptosBalancesInsightsResponse = CryptosBalancesInsightsResponse(
      balances = balances(),
      cryptos = emptyList()
    )

    every { insightsServiceMock.retrieveCryptosBalancesInsights() } returns cryptosBalancesInsightsResponse

    val cryptosBalancesInsights = insightsController.retrieveCryptosBalancesInsights()

    assertThat(cryptosBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(cryptosBalancesInsightsResponse))
  }

  @Test
  fun `should retrieve platforms balances insights with status 200`() {
    val platformsBalancesInsightsResponse = PlatformsBalancesInsightsResponse(
      balances = balances(),
      platforms = emptyList()
    )

    every { insightsServiceMock.retrievePlatformsBalancesInsights() } returns platformsBalancesInsightsResponse

    val platformsBalancesInsights = insightsController.retrievePlatformsBalancesInsights()

    assertThat(platformsBalancesInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(platformsBalancesInsightsResponse))
  }

  @Test
  fun `should retrieve crypto insights with status 200`() {
    val cryptoInsightResponse = CryptoInsightResponse(
      balances = balances(),
      cryptoName = "Bitcoin",
      platforms = emptyList()
    )

    every { insightsServiceMock.retrieveCryptoInsights("bitcoin") } returns cryptoInsightResponse

    val cryptoInsights = insightsController.retrieveCryptoInsights("bitcoin")

    assertThat(cryptoInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(cryptoInsightResponse))
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
    } returns platformInsightsResponse

    val platformInsights = insightsController.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

    assertThat(platformInsights)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(platformInsightsResponse))
  }
}
