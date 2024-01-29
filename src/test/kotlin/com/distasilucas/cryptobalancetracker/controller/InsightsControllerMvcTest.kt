package com.distasilucas.cryptobalancetracker.controller

import balances
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.model.response.insights.*
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights
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
import retrievePlatformInsights
import retrievePlatformsBalancesInsights
import retrieveTotalBalancesInsights
import retrieveUserCryptosInsights
import retrieveUserCryptosPlatformsInsights
import java.math.BigDecimal
import java.util.Optional

@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension::class)
@WebMvcTest(InsightsController::class)
class InsightsControllerMvcTest(
    @Autowired private val mockMvc: MockMvc
) {

    @MockkBean
    private lateinit var insightsServiceMock: InsightsService

    @Test
    fun `should retrieve total balances with status 200`() {
        every { insightsServiceMock.retrieveTotalBalancesInsights() } returns Optional.of(balances())

        mockMvc.retrieveTotalBalancesInsights()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.totalUSDBalance", `is`("100")))
            .andExpect(jsonPath("$.totalBTCBalance", `is`("0.1")))
            .andExpect(jsonPath("$.totalEURBalance", `is`("70")))
    }

    @Test
    fun `should retrieve user cryptos insights for page with status 200`() {
        val page = 0
        val pageUserCryptosInsightsResponse = pageUserCryptosInsightsResponse(
            "676fb38a-556e-11ee-b56e-325096b39f47", listOf("BINANCE")
        )

        every {
            insightsServiceMock.retrieveUserCryptosInsights(page)
        } returns Optional.of(pageUserCryptosInsightsResponse)

        mockMvc.retrieveUserCryptosInsights(page)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.page", `is`(1)))
            .andExpect(jsonPath("$.totalPages", `is`(1)))
            .andExpect(jsonPath("$.hasNextPage", `is`(false)))
            .andExpect(jsonPath("$.balances.totalUSDBalance", `is`("4500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", `is`("0.15")))
            .andExpect(jsonPath("$.balances.totalEURBalance", `is`("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].marketCapRank", `is`(1)))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.id", `is`("676fb38a-556e-11ee-b56e-325096b39f47")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoName", `is`("Bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoId", `is`("bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.symbol", `is`("btc")))
            .andExpect(jsonPath(
                    "$.cryptos[0].cryptoInfo.image",
                    `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"))
            )
            .andExpect(jsonPath("$.cryptos[0].quantity", `is`("0.15")))
            .andExpect(jsonPath("$.cryptos[0].percentage", `is`(100.0)))
            .andExpect(jsonPath("$.cryptos[0].balances.totalUSDBalance", `is`("4500.00")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalBTCBalance", `is`("0.15")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalEURBalance", `is`("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].marketData.circulatingSupply", `is`("19000000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.maxSupply", `is`("21000000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.usd", `is`("30000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.eur", `is`("27000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.btc", `is`("1")))
            .andExpect(jsonPath("$.cryptos[0].marketData.marketCap", `is`("813208997089")))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn24h", `is`(10.00)))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn7d", `is`(-5.00)))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn30d", `is`(0.00)))
            .andExpect(jsonPath("$.cryptos[0].platforms", `is`(listOf("BINANCE"))))
    }

    @Test
    fun `should fail with status 400 with 1 message when retrieving user cryptos insights with invalid page`() {
        val page = -1

        mockMvc.retrieveUserCryptosInsights(page)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
            .andExpect(jsonPath("$[0].status", `is`(400)))
            .andExpect(jsonPath("$[0].detail", `is`("Page must be greater than or equal to 0")))
    }

    @Test
    fun `should retrieve user cryptos platforms insights for page with status 200`() {
        val page = 0
        val pageUserCryptosInsightsResponse = pageUserCryptosInsightsResponse(platforms = listOf("BINANCE", "COINBASE"))

        every {
            insightsServiceMock.retrieveUserCryptosPlatformsInsights(page)
        } returns Optional.of(pageUserCryptosInsightsResponse)

        mockMvc.retrieveUserCryptosPlatformsInsights(page)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.page", `is`(1)))
            .andExpect(jsonPath("$.totalPages", `is`(1)))
            .andExpect(jsonPath("$.hasNextPage", `is`(false)))
            .andExpect(jsonPath("$.balances.totalUSDBalance", `is`("4500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", `is`("0.15")))
            .andExpect(jsonPath("$.balances.totalEURBalance", `is`("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].marketCapRank", `is`(1)))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoName", `is`("Bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoId", `is`("bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.symbol", `is`("btc")))
            .andExpect(jsonPath(
                    "$.cryptos[0].cryptoInfo.image",
                    `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"))
            )
            .andExpect(jsonPath("$.cryptos[0].quantity", `is`("0.15")))
            .andExpect(jsonPath("$.cryptos[0].percentage", `is`(100.0)))
            .andExpect(jsonPath("$.cryptos[0].balances.totalUSDBalance", `is`("4500.00")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalBTCBalance", `is`("0.15")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalEURBalance", `is`("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].marketData.circulatingSupply", `is`("19000000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.maxSupply", `is`("21000000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.usd", `is`("30000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.eur", `is`("27000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.btc", `is`("1")))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn24h", `is`(10.00)))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn7d", `is`(-5.00)))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn30d", `is`(0.00)))
            .andExpect(jsonPath("$.cryptos[0].platforms", `is`(listOf("BINANCE", "COINBASE"))))
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

        every {
            insightsServiceMock.retrieveCryptosBalancesInsights()
        } returns Optional.of(cryptosBalancesInsightsResponse)

        mockMvc.retrieveCryptosBalancesInsights()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.balances.totalUSDBalance", `is`("7500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", `is`("0.25")))
            .andExpect(jsonPath("$.balances.totalEURBalance", `is`("6750.00")))
            .andExpect(jsonPath("$.cryptos[0].cryptoName", `is`("Bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoId", `is`("bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].quantity", `is`("0.25")))
            .andExpect(
                jsonPath(
                    "$.cryptos[0].balances.totalUSDBalance",
                    `is`("7500.00")
                )
            )
            .andExpect(jsonPath("$.cryptos[0].balances.totalBTCBalance", `is`("0.25")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalEURBalance", `is`("6750.00")))
            .andExpect(jsonPath("$.cryptos[0].percentage", `is`(100.0)))

    }

    @Test
    fun `should retrieve platforms balances insights with status 200`() {
        val platformsBalancesInsightsResponse = platformsBalancesInsightsResponse()

        every {
            insightsServiceMock.retrievePlatformsBalancesInsights()
        } returns Optional.of(platformsBalancesInsightsResponse)

        mockMvc.retrievePlatformsBalancesInsights()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.balances.totalUSDBalance", `is`("7500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", `is`("0.25")))
            .andExpect(jsonPath("$.balances.totalEURBalance", `is`("6750.00")))
            .andExpect(jsonPath("$.platforms[0].platformName", `is`("BINANCE")))
            .andExpect(jsonPath("$.platforms[0].balances.totalUSDBalance", `is`("7500.00")))
            .andExpect(jsonPath("$.platforms[0].balances.totalBTCBalance", `is`("0.25")))
            .andExpect(jsonPath("$.platforms[0].balances.totalEURBalance", `is`("6750.00")))
            .andExpect(jsonPath("$.platforms[0].percentage", `is`(100.0)))
    }

    @Test
    fun `should retrieve crypto insights with status 200`() {
        val cryptoInsightResponse = cryptoInsightResponse()

        every {
            insightsServiceMock.retrieveCryptoInsights("bitcoin")
        } returns Optional.of(cryptoInsightResponse)

        mockMvc.retrieveCryptoInsights("bitcoin")
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.cryptoName", `is`("Bitcoin")))
            .andExpect(jsonPath("$.balances.totalUSDBalance", `is`("4500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", `is`("0.15")))
            .andExpect(jsonPath("$.balances.totalEURBalance", `is`("4050.00")))
            .andExpect(jsonPath("$.platforms[0].quantity", `is`("0.15")))
            .andExpect(jsonPath("$.platforms[0].balances.totalUSDBalance", `is`("4500.00")))
            .andExpect(jsonPath("$.platforms[0].balances.totalBTCBalance", `is`("0.15")))
            .andExpect(jsonPath("$.platforms[0].balances.totalEURBalance", `is`("4050.00")))
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
            .andExpect(jsonPath("$.balances.totalUSDBalance", `is`("4500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", `is`("0.15")))
            .andExpect(jsonPath("$.balances.totalEURBalance", `is`("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].id", `is`("1f832f95-62e3-4d1b-a1e6-982d8c22f2bb")))
            .andExpect(jsonPath("$.cryptos[0].cryptoName", `is`("Bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoId", `is`("bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].quantity", `is`("0.15")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalUSDBalance", `is`("4500.00")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalBTCBalance", `is`("0.15")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalEURBalance", `is`("4050.00")))
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

    private fun pageUserCryptosInsightsResponse(
        id: String? = null,
        platforms: List<String>
    ) = PageUserCryptosInsightsResponse(
        page = 0,
        totalPages = 1,
        balances = BalancesResponse(
            totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
        ),
        cryptos = listOf(
            UserCryptosInsights(
                cryptoInfo = CryptoInfo(
                    id = id,
                    cryptoName = "Bitcoin",
                    coingeckoCryptoId = "bitcoin",
                    symbol = "btc",
                    image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                ),
                quantity = "0.15",
                percentage = 100f,
                balances = BalancesResponse(
                    totalUSDBalance = "4500.00",
                    totalBTCBalance = "0.15",
                    totalEURBalance = "4050.00"
                ),
                marketCapRank = 1,
                marketData = MarketData(
                    circulatingSupply = "19000000",
                    maxSupply = "21000000",
                    marketCap = "813208997089",
                    currentPrice = CurrentPrice(
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
                platforms = platforms
            )
        )
    )

    private fun cryptosBalancesInsightsResponse() = CryptosBalancesInsightsResponse(
        balances = BalancesResponse(
            totalUSDBalance = "7500.00",
            totalBTCBalance = "0.25",
            totalEURBalance = "6750.00"
        ),
        cryptos = listOf(
            CryptoInsights(
                id = "1f832f95-62e3-4d1b-a1e6-982d8c22f2bb",
                cryptoName = "Bitcoin",
                cryptoId = "bitcoin",
                quantity = "0.25",
                balances = BalancesResponse(
                    totalUSDBalance = "7500.00",
                    totalBTCBalance = "0.25",
                    totalEURBalance = "6750.00"
                ),
                percentage = 100f
            )
        )
    )

    private fun platformsBalancesInsightsResponse() = PlatformsBalancesInsightsResponse(
        balances = BalancesResponse(
            totalUSDBalance = "7500.00",
            totalBTCBalance = "0.25",
            totalEURBalance = "6750.00"
        ),
        platforms = listOf(
            PlatformsInsights(
                platformName = "BINANCE",
                balances = BalancesResponse(
                    totalUSDBalance = "7500.00",
                    totalBTCBalance = "0.25",
                    totalEURBalance = "6750.00"
                ),
                percentage = 100f
            )
        )
    )

    private fun cryptoInsightResponse() = CryptoInsightResponse(
        cryptoName = "Bitcoin",
        balances = BalancesResponse(
            totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
        ),
        platforms = listOf(
            PlatformInsight(
                quantity = "0.15",
                balances = BalancesResponse(
                    totalUSDBalance = "4500.00",
                    totalBTCBalance = "0.15",
                    totalEURBalance = "4050.00"
                ),
                percentage = 100f,
                platformName = "BINANCE"
            )
        )
    )

    private fun platformInsightsResponse() = PlatformInsightsResponse(
        platformName = "BINANCE",
        balances = BalancesResponse(
            totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
        ),
        cryptos = listOf(
            CryptoInsights(
                id = "1f832f95-62e3-4d1b-a1e6-982d8c22f2bb",
                cryptoName = "Bitcoin",
                cryptoId = "bitcoin",
                quantity = "0.15",
                balances = BalancesResponse(
                    totalUSDBalance = "4500.00",
                    totalBTCBalance = "0.15",
                    totalEURBalance = "4050.00"
                ),
                percentage = 100f
            )
        )
    )

}