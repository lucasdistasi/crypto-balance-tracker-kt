package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.HomeInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.CryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.service.InsightsService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import retrieveCryptoInsights
import retrieveCryptosBalancesInsights
import retrieveDatesBalances
import retrievePlatformInsights
import retrievePlatformsBalancesInsights
import retrieveHomeInsights
import retrieveUserCryptosPlatformsInsights
import java.util.*

@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension::class)
@WebMvcTest(InsightsController::class)
class InsightsControllerMvcTest(
  @Autowired private val mockMvc: MockMvc
) {

  @MockkBean
  private lateinit var insightsServiceMock: InsightsService

  @Test
  fun `should retrieve home insights with status 200`() {
    val homeInsightsResponse = HomeInsightsResponse(
      Balances(FiatBalance("22822.29", "19927.78"), "0.25127936"),
      "199.92",
      CryptoInfo(
        coingeckoCryptoId = "bitcoin",
        symbol = "btc",
        image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
        price = Price("90824.40", "79305.30"),
        priceChange = PriceChange(10.0)
      )
    )

    every { insightsServiceMock.retrieveHomeInsightsResponse() } returns homeInsightsResponse

    mockMvc.retrieveHomeInsights()
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(jsonPath("$.balances.fiat.usd", `is`("22822.29")))
      .andExpect(jsonPath("$.balances.fiat.eur", `is`("19927.78")))
      .andExpect(jsonPath("$.balances.btc", `is`("0.25127936")))
      .andExpect(jsonPath("$.stablecoins", `is`("199.92")))
      .andExpect(jsonPath("$.top24hGainer.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.top24hGainer.symbol", `is`("btc")))
      .andExpect(jsonPath("$.top24hGainer.image", `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")))
      .andExpect(jsonPath("$.top24hGainer.price.usd", `is`("90824.40")))
      .andExpect(jsonPath("$.top24hGainer.price.eur", `is`("79305.30")))
      .andExpect(jsonPath("$.top24hGainer.priceChange.changePercentageIn24h", `is`(10.0)))
  }

  @Test
  fun `should retrieve dates balances with status 200`() {
    val dateBalanceResponse = DatesBalanceResponse(
      datesBalances = listOf(
        DateBalances("16 March 2024", Balances(FiatBalance("1000", "918.45"), "0.01438911")),
        DateBalances("17 March 2024", Balances(FiatBalance("1500", "1377.67"), "0.021583665"))
      ),
      change = BalanceChanges(50F, 50F, 49.99F),
      priceDifference = DifferencesChanges("500", "459.22", "0.007194555")
    )

    every { insightsServiceMock.retrieveDatesBalances(DateRange.LAST_DAY) } returns Optional.of(dateBalanceResponse)

    mockMvc.retrieveDatesBalances(DateRange.LAST_DAY)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(jsonPath("$.datesBalances[0].date", `is`("16 March 2024")))
      .andExpect(jsonPath("$.datesBalances[0].balances.fiat.usd", `is`("1000")))
      .andExpect(jsonPath("$.datesBalances[0].balances.fiat.eur", `is`("918.45")))
      .andExpect(jsonPath("$.datesBalances[0].balances.btc", `is`("0.01438911")))
      .andExpect(jsonPath("$.datesBalances[1].date", `is`("17 March 2024")))
      .andExpect(jsonPath("$.datesBalances[1].balances.fiat.usd", `is`("1500")))
      .andExpect(jsonPath("$.datesBalances[1].balances.fiat.eur", `is`("1377.67")))
      .andExpect(jsonPath("$.datesBalances[1].balances.btc", `is`("0.021583665")))
      .andExpect(jsonPath("$.change.usdChange", `is`(50.0)))
      .andExpect(jsonPath("$.change.eurChange", `is`(50.0)))
      .andExpect(jsonPath("$.change.btcChange", `is`(49.99)))
      .andExpect(jsonPath("$.priceDifference.usdDifference", `is`("500")))
      .andExpect(jsonPath("$.priceDifference.eurDifference", `is`("459.22")))
      .andExpect(jsonPath("$.priceDifference.btcDifference", `is`("0.007194555")))
  }

  @Test
  fun `should retrieve user cryptos insights for page with status 200`() {
    val page = 0
    val pageUserCryptosInsightsResponse = pageUserCryptosInsightsResponse()

    every { insightsServiceMock.retrieveUserCryptosInsights(page) } returns pageUserCryptosInsightsResponse

    mockMvc.retrieveUserCryptosPlatformsInsights(page)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(jsonPath("$.page", `is`(1)))
      .andExpect(jsonPath("$.totalPages", `is`(1)))
      .andExpect(jsonPath("$.hasNextPage", `is`(false)))
      .andExpect(jsonPath("$.balances.fiat.usd", `is`("4500.00")))
      .andExpect(jsonPath("$.balances.fiat.eur", `is`("4050.00")))
      .andExpect(jsonPath("$.balances.btc", `is`("0.15")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.symbol", `is`("btc")))
      .andExpect(
        jsonPath(
          "$.cryptos[0].cryptoInfo.image",
          `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")
        )
      )
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.price.usd", `is`("30000")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.price.eur", `is`("27000")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.price.btc", `is`("1")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.priceChange.changePercentageIn24h", `is`(10.00)))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.priceChange.changePercentageIn7d", `is`(-5.00)))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.priceChange.changePercentageIn30d", `is`(0.00)))
      .andExpect(jsonPath("$.cryptos[0].quantity", `is`("0.15")))
      .andExpect(jsonPath("$.cryptos[0].percentage", `is`(100.0)))
      .andExpect(jsonPath("$.cryptos[0].balances.fiat.usd", `is`("4500.00")))
      .andExpect(jsonPath("$.cryptos[0].balances.fiat.eur", `is`("4050.00")))
      .andExpect(jsonPath("$.cryptos[0].balances.btc", `is`("0.15")))
  }

  @Test
  fun `should fail with status 400 with 1 message when retrieving user cryptos platforms insights with invalid page`() {
    val page = -1

    mockMvc.retrieveUserCryptosPlatformsInsights(page)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Page must be greater than or equal to 0")))
  }

  @Test
  fun `should retrieve cryptos balances insights with status 200`() {
    val cryptosBalancesInsightsResponse = cryptosBalancesInsightsResponse()

    every { insightsServiceMock.retrieveCryptosBalancesInsights() } returns listOf(cryptosBalancesInsightsResponse)

    mockMvc.retrieveCryptosBalancesInsights()
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(jsonPath("$.[0].name", `is`("Bitcoin")))
      .andExpect(jsonPath("$.[0].balance", `is`("7500.00")))
      .andExpect(jsonPath("$.[0].percentage", `is`(100.0)))

  }

  @Test
  fun `should retrieve platforms balances insights with status 200`() {
    val platformsBalancesInsightsResponse = platformsBalancesInsightsResponse()

    every { insightsServiceMock.retrievePlatformsBalancesInsights() } returns listOf(platformsBalancesInsightsResponse)

    mockMvc.retrievePlatformsBalancesInsights()
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(jsonPath("$.[0].name", `is`("BINANCE")))
      .andExpect(jsonPath("$.[0].balance", `is`("7500.00")))
      .andExpect(jsonPath("$.[0].percentage", `is`(100.0)))
  }

  @Test
  fun `should retrieve crypto insights with status 200`() {
    val cryptoInsightResponse = cryptoInsightResponse()

    every { insightsServiceMock.retrieveCryptoInsights("bitcoin") } returns Optional.of(cryptoInsightResponse)

    mockMvc.retrieveCryptoInsights("bitcoin")
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(jsonPath("$.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.balances.fiat.usd", `is`("4500.00")))
      .andExpect(jsonPath("$.balances.fiat.eur", `is`("4050.00")))
      .andExpect(jsonPath("$.balances.btc", `is`("0.15")))
      .andExpect(jsonPath("$.platforms[0].quantity", `is`("0.15")))
      .andExpect(jsonPath("$.platforms[0].balances.fiat.usd", `is`("4500.00")))
      .andExpect(jsonPath("$.platforms[0].balances.fiat.eur", `is`("4050.00")))
      .andExpect(jsonPath("$.platforms[0].balances.btc", `is`("0.15")))
      .andExpect(jsonPath("$.platforms[0].percentage", `is`(100.0)))
      .andExpect(jsonPath("$.platforms[0].platformName", `is`("BINANCE")))
  }

  @Test
  fun `should retrieve platform insights with status 200`() {
    val platformInsightsResponse = platformInsightsResponse()

    every {
      insightsServiceMock.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")
    } returns Optional.of(platformInsightsResponse)

    mockMvc.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(jsonPath("$.platformName", `is`("BINANCE")))
      .andExpect(jsonPath("$.balances.fiat.usd", `is`("4500.00")))
      .andExpect(jsonPath("$.balances.fiat.eur", `is`("4050.00")))
      .andExpect(jsonPath("$.balances.btc", `is`("0.15")))
      .andExpect(jsonPath("$.cryptos[0].id", `is`("1f832f95-62e3-4d1b-a1e6-982d8c22f2bb")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.cryptos[0].cryptoInfo.symbol", `is`("btc")))
      .andExpect(
        jsonPath(
          "$.cryptos[0].cryptoInfo.image",
          `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")
        )
      )
      .andExpect(jsonPath("$.cryptos[0].quantity", `is`("0.15")))
      .andExpect(jsonPath("$.cryptos[0].balances.fiat.usd", `is`("4500.00")))
      .andExpect(jsonPath("$.cryptos[0].balances.fiat.eur", `is`("4050.00")))
      .andExpect(jsonPath("$.cryptos[0].balances.btc", `is`("0.15")))
      .andExpect(jsonPath("$.cryptos[0].percentage", `is`(100.0)))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should fail with status 400 with 1 message when retrieving platform insights with invalid id`(platformId: String) {
    mockMvc.retrievePlatformInsights(platformId)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`(PLATFORM_ID_UUID)))
  }

  private fun pageUserCryptosInsightsResponse() = PageUserCryptosInsightsResponse(
    page = 0,
    totalPages = 1,
    balances = Balances(FiatBalance("4500.00", "4050.00"), "0.15"),
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
        percentage = 100f,
        balances = Balances(FiatBalance("4500.00", "4050.00"), "0.15")
      )
    )
  )

  private fun cryptosBalancesInsightsResponse() = BalancesChartResponse("Bitcoin", "7500.00", 100F)

  private fun platformsBalancesInsightsResponse() = BalancesChartResponse("BINANCE", "7500.00", 100F)

  private fun cryptoInsightResponse() = CryptoInsightResponse(
    cryptoName = "Bitcoin",
    balances = Balances(FiatBalance("4500.00", "4050.00"), "0.15"),
    platforms = listOf(
      PlatformInsight(
        quantity = "0.15",
        balances = Balances(FiatBalance("4500.00", "4050.00"), "0.15"),
        percentage = 100f,
        platformName = "BINANCE"
      )
    )
  )

  private fun platformInsightsResponse() = PlatformInsightsResponse(
    platformName = "BINANCE",
    balances = Balances(FiatBalance("4500.00", "4050.00"), "0.15"),
    cryptos = listOf(
      CryptoInsights(
        id = "1f832f95-62e3-4d1b-a1e6-982d8c22f2bb",
        cryptoInfo = CryptoInfo(
          cryptoName = "Bitcoin",
          coingeckoCryptoId = "bitcoin",
          symbol = "btc",
          image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
        ),
        quantity = "0.15",
        percentage = 100f,
        balances = Balances(FiatBalance("4500.00", "4050.00"), "0.15")
      )
    )
  )
}
