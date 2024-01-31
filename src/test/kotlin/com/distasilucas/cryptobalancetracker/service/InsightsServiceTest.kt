package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.SortBy
import com.distasilucas.cryptobalancetracker.model.SortParams
import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.response.insights.*
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights
import getCryptoEntity
import getPlatformResponse
import getUserCrypto
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional

class InsightsServiceTest {

    private val platformServiceMock = mockk<PlatformService>()
    private val userCryptoServiceMock = mockk<UserCryptoService>()
    private val cryptoServiceMock = mockk<CryptoService>()

    private val insightsService = InsightsService(platformServiceMock, userCryptoServiceMock, cryptoServiceMock)

    @Test
    fun `should retrieve total balances insights`() {
        val cryptos = listOf("bitcoin", "tether", "ethereum", "litecoin")
        val userCryptos = userCryptos().filter { cryptos.contains(it.coingeckoCryptoId) }
        val cryptosEntities = cryptos().filter { cryptos.contains(it.id) }

        every { userCryptoServiceMock.findAll() } returns userCryptos
        every {
            cryptoServiceMock.findAllByIds(setOf("bitcoin", "tether", "ethereum", "litecoin"))
        } returns cryptosEntities

        val balances = insightsService.retrieveTotalBalancesInsights()

        assertThat(balances)
            .usingRecursiveComparison()
            .isEqualTo(
                Optional.of(
                    BalancesResponse(
                        totalUSDBalance = "7108.39",
                        totalBTCBalance = "0.25127935932",
                        totalEURBalance = "6484.23"
                    )
                )
            )
    }

    @Test
    fun `should retrieve empty for total balances insights`() {
        every { userCryptoServiceMock.findAll() } returns emptyList()

        val balances = insightsService.retrieveTotalBalancesInsights()

        assertThat(balances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.empty<BalancesResponse>())
    }

    @Test
    fun `should retrieve platform insights with one crypto`() {
        val platformResponse = getPlatformResponse()
        val userCryptos = getUserCrypto()
        val bitcoinCryptoEntity = getCryptoEntity()

        every {
            platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")
        } returns platformResponse
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
                        balances = BalancesResponse(
                            totalUSDBalance = "7500.00",
                            totalBTCBalance = "0.25",
                            totalEURBalance = "6750.00"
                        ),
                        cryptos = listOf(
                            CryptoInsights(
                                id = "123e4567-e89b-12d3-a456-426614174000",
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
                )
            )
    }

    @Test
    fun `should retrieve platform insights with multiple cryptos`() {
        val localDateTime = LocalDateTime.now()
        val platformResponse = getPlatformResponse()
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
        } returns platformResponse
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
                        balances = BalancesResponse(
                            totalUSDBalance = "7925.00",
                            totalBTCBalance = "0.266554",
                            totalEURBalance = "7147.00"
                        ),
                        cryptos = listOf(
                            CryptoInsights(
                                id = "123e4567-e89b-12d3-a456-426614174000",
                                cryptoName = "Bitcoin",
                                cryptoId = "bitcoin",
                                quantity = "0.25",
                                balances = BalancesResponse(
                                    totalUSDBalance = "7500.00",
                                    totalBTCBalance = "0.25",
                                    totalEURBalance = "6750.00"
                                ),
                                percentage = 94.64f
                            ),
                            CryptoInsights(
                                id = polkadotUserCrypto.id,
                                cryptoName = "Polkadot",
                                cryptoId = "polkadot",
                                quantity = "100",
                                balances = BalancesResponse(
                                    totalUSDBalance = "425.00",
                                    totalBTCBalance = "0.016554",
                                    totalEURBalance = "397.00"
                                ),
                                percentage = 5.36f
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `should retrieve empty if no cryptos are found for retrievePlatformInsights`() {
        every {
            userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")
        } returns emptyList()

        val platformInsights = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")

        assertThat(platformInsights)
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
                        balances = BalancesResponse(
                            totalUSDBalance = "7500.00",
                            totalBTCBalance = "0.25",
                            totalEURBalance = "6750.00"
                        ),
                        platforms = listOf(
                            PlatformInsight(
                                quantity = "0.25",
                                balances = BalancesResponse(
                                    totalUSDBalance = "7500.00",
                                    totalBTCBalance = "0.25",
                                    totalEURBalance = "6750.00"
                                ),
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
                        balances = BalancesResponse(
                            totalUSDBalance = "8536.50",
                            totalBTCBalance = "0.28455",
                            totalEURBalance = "7682.85"
                        ),
                        platforms = listOf(
                            PlatformInsight(
                                quantity = "0.25",
                                balances = BalancesResponse(
                                    totalUSDBalance = "7500.00",
                                    totalBTCBalance = "0.25",
                                    totalEURBalance = "6750.00"
                                ),
                                percentage = 87.86f,
                                platformName = "BINANCE"
                            ),
                            PlatformInsight(
                                quantity = "0.03455",
                                balances = BalancesResponse(
                                    totalUSDBalance = "1036.50",
                                    totalBTCBalance = "0.03455",
                                    totalEURBalance = "932.85"
                                ),
                                percentage = 12.14f,
                                platformName = "COINBASE"
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `should retrieve empty if no cryptos are found for retrieveCryptoInsights`() {
        every {
            userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")
        } returns emptyList()

        val cryptoInsightResponse = insightsService.retrieveCryptoInsights("bitcoin")

        assertThat(cryptoInsightResponse)
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
                Optional.of(
                    PlatformsBalancesInsightsResponse(
                        balances = BalancesResponse(
                            totalUSDBalance = "7108.39",
                            totalBTCBalance = "0.25127935932",
                            totalEURBalance = "6484.23"
                        ),
                        platforms = listOf(
                            PlatformsInsights(
                                platformName = "BINANCE",
                                balances = BalancesResponse(
                                    totalUSDBalance = "5120.45",
                                    totalBTCBalance = "0.1740889256",
                                    totalEURBalance = "4629.06"
                                ),
                                percentage = 72.03f
                            ),
                            PlatformsInsights(
                                platformName = "COINBASE",
                                balances = BalancesResponse(
                                    totalUSDBalance = "1987.93",
                                    totalBTCBalance = "0.07719043372",
                                    totalEURBalance = "1855.17"
                                ),
                                percentage = 27.97f
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `should retrieve empty if no cryptos are found for retrievePlatformBalancesInsights`() {
        every { userCryptoServiceMock.findAll() } returns emptyList()

        val platformBalancesInsightsResponse = insightsService.retrievePlatformsBalancesInsights()

        assertThat(platformBalancesInsightsResponse)
            .usingRecursiveComparison()
            .isEqualTo(Optional.empty<PlatformsBalancesInsightsResponse>())
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
                Optional.of(
                    CryptosBalancesInsightsResponse(
                        balances = BalancesResponse(
                            totalUSDBalance = "7108.39",
                            totalBTCBalance = "0.25127935932",
                            totalEURBalance = "6484.23"
                        ),
                        cryptos = listOf(
                            CryptoInsights(
                                cryptoName = "Bitcoin",
                                cryptoId = "bitcoin",
                                quantity = "0.15",
                                balances = BalancesResponse(
                                    totalUSDBalance = "4500.00",
                                    totalBTCBalance = "0.15",
                                    totalEURBalance = "4050.00"
                                ),
                                percentage = 63.31f
                            ),
                            CryptoInsights(
                                cryptoName = "Ethereum",
                                cryptoId = "ethereum",
                                quantity = "1.372",
                                balances = BalancesResponse(
                                    totalUSDBalance = "2219.13",
                                    totalBTCBalance = "0.08616648432",
                                    totalEURBalance = "2070.86"
                                ),
                                percentage = 31.22f
                            ),
                            CryptoInsights(
                                cryptoName = "Tether",
                                cryptoId = "tether",
                                quantity = "200",
                                balances = BalancesResponse(
                                    totalUSDBalance = "199.92",
                                    totalBTCBalance = "0.00776",
                                    totalEURBalance = "186.62"
                                ),
                                percentage = 2.81f
                            ),
                            CryptoInsights(
                                cryptoName = "Litecoin",
                                cryptoId = "litecoin",
                                quantity = "3.125",
                                balances = BalancesResponse(
                                    totalUSDBalance = "189.34",
                                    totalBTCBalance = "0.007352875",
                                    totalEURBalance = "176.75"
                                ),
                                percentage = 2.66f
                            )
                        )
                    )
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
                Optional.of(
                    CryptosBalancesInsightsResponse(
                        balances = BalancesResponse(
                            totalUSDBalance = "8373.63",
                            totalBTCBalance = "0.29959591932",
                            totalEURBalance = "7663.61"
                        ),
                        cryptos = listOf(
                            CryptoInsights(
                                cryptoName = "Bitcoin",
                                cryptoId = "bitcoin",
                                quantity = "0.15",
                                balances = BalancesResponse(
                                    totalUSDBalance = "4500.00",
                                    totalBTCBalance = "0.15",
                                    totalEURBalance = "4050.00"
                                ),
                                percentage = 53.74f
                            ),
                            CryptoInsights(
                                cryptoName = "Ethereum",
                                cryptoId = "ethereum",
                                quantity = "1.372",
                                balances = BalancesResponse(
                                    totalUSDBalance = "2219.13",
                                    totalBTCBalance = "0.08616648432",
                                    totalEURBalance = "2070.86"
                                ),
                                percentage = 26.5f
                            ),
                            CryptoInsights(
                                cryptoName = "Avalanche",
                                cryptoId = "avalanche-2",
                                quantity = "25",
                                balances = BalancesResponse(
                                    totalUSDBalance = "232.50",
                                    totalBTCBalance = "0.008879",
                                    totalEURBalance = "216.75"
                                ),
                                percentage = 2.78f
                            ),
                            CryptoInsights(
                                cryptoName = "BNB",
                                cryptoId = "binancecoin",
                                quantity = "1",
                                balances = BalancesResponse(
                                    totalUSDBalance = "211.79",
                                    totalBTCBalance = "0.00811016",
                                    totalEURBalance = "197.80"
                                ),
                                percentage = 2.53f
                            ),
                            CryptoInsights(
                                cryptoName = "Chainlink",
                                cryptoId = "chainlink",
                                quantity = "35",
                                balances = BalancesResponse(
                                    totalUSDBalance = "209.65",
                                    totalBTCBalance = "0.0080031",
                                    totalEURBalance = "195.30"
                                ),
                                percentage = 2.5f
                            ),
                            CryptoInsights(
                                cryptoName = "Tether",
                                cryptoId = "tether",
                                quantity = "200",
                                balances = BalancesResponse(
                                    totalUSDBalance = "199.92",
                                    totalBTCBalance = "0.00776",
                                    totalEURBalance = "186.62"
                                ),
                                percentage = 2.39f
                            ),
                            CryptoInsights(
                                cryptoName = "Litecoin",
                                cryptoId = "litecoin",
                                quantity = "3.125",
                                balances = BalancesResponse(
                                    totalUSDBalance = "189.34",
                                    totalBTCBalance = "0.007352875",
                                    totalEURBalance = "176.75"
                                ),
                                percentage = 2.26f
                            ),
                            CryptoInsights(
                                cryptoName = "Solana",
                                cryptoId = "solana",
                                quantity = "10",
                                balances = BalancesResponse(
                                    totalUSDBalance = "180.40",
                                    totalBTCBalance = "0.0068809",
                                    totalEURBalance = "168.20"
                                ),
                                percentage = 2.15f
                            ),
                            CryptoInsights(
                                cryptoName = "Polkadot",
                                cryptoId = "polkadot",
                                quantity = "40",
                                balances = BalancesResponse(
                                    totalUSDBalance = "160.40",
                                    totalBTCBalance = "0.0061208",
                                    totalEURBalance = "149.20"
                                ),
                                percentage = 1.92f
                            ),
                            CryptoInsights(
                                cryptoName = "Uniswap",
                                cryptoId = "uniswap",
                                quantity = "30",
                                balances = BalancesResponse(
                                    totalUSDBalance = "127.50",
                                    totalBTCBalance = "0.0048591",
                                    totalEURBalance = "118.80"
                                ),
                                percentage = 1.52f
                            ),
                            CryptoInsights(
                                cryptoName = "Polygon",
                                cryptoId = "matic-network",
                                quantity = "100",
                                balances = BalancesResponse(
                                    totalUSDBalance = "51.00",
                                    totalBTCBalance = "0.001947",
                                    totalEURBalance = "47.54"
                                ),
                                percentage = 0.61f
                            ),
                            CryptoInsights(
                                cryptoName = "Cardano",
                                cryptoId = "cardano",
                                quantity = "150",
                                balances = BalancesResponse(
                                    totalUSDBalance = "37.34",
                                    totalBTCBalance = "0.001425",
                                    totalEURBalance = "34.80"
                                ),
                                percentage = 0.45f
                            ),
                            CryptoInsights(
                                cryptoName = "Others",
                                balances = BalancesResponse(
                                    totalUSDBalance = "54.66",
                                    totalBTCBalance = "0.0020915",
                                    totalEURBalance = "50.99"
                                ),
                                percentage = 0.65f
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `should retrieve empty if no cryptos are found for retrieveCryptosBalancesInsights`() {
        every { userCryptoServiceMock.findAll() } returns emptyList()

        val cryptosBalancesInsightsResponse = insightsService.retrieveCryptosBalancesInsights()

        assertThat(cryptosBalancesInsightsResponse)
            .usingRecursiveComparison()
            .isEqualTo(Optional.empty<CryptosBalancesInsightsResponse>())
    }

    @Test
    fun `should retrieve user cryptos insights`() {
        val cryptos = listOf("bitcoin", "litecoin")
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

        every { cryptoServiceMock.findAllByIds(setOf("litecoin", "bitcoin")) } returns cryptosEntities
        every {
            platformServiceMock.findAllByIds(
                setOf(
                    "a76b400e-8ffc-42d6-bf47-db866eb20153",
                    "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                )
            )
        } returns listOf(binancePlatform, coinbasePlatform)
        every { userCryptoServiceMock.findAll() } returns userCryptos

        val userCryptosInsights = insightsService.retrieveUserCryptosInsights(0)

        assertThat(userCryptosInsights)
            .usingRecursiveComparison()
            .isEqualTo(
                Optional.of(
                    PageUserCryptosInsightsResponse(
                        page = 1,
                        totalPages = 1,
                        hasNextPage = false,
                        balances = BalancesResponse(
                            totalUSDBalance = "4689.34",
                            totalBTCBalance = "0.157352875",
                            totalEURBalance = "4226.75"
                        ),
                        cryptos = listOf(
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    id = "676fb38a-556e-11ee-b56e-325096b39f47",
                                    cryptoName = "Bitcoin",
                                    coingeckoCryptoId = "bitcoin",
                                    symbol = "btc",
                                    image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                ),
                                quantity = "0.15",
                                percentage = 95.96f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "4500.00",
                                    totalBTCBalance = "0.15",
                                    totalEURBalance = "4050.00"
                                ),
                                marketCapRank = 1,
                                marketData = MarketData(
                                    circulatingSupply = "19000000",
                                    maxSupply = "21000000",
                                    currentPrice = CurrentPrice(
                                        usd = "30000",
                                        eur = "27000",
                                        btc = "1"
                                    ),
                                    marketCap = "813208997089",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("10.00"),
                                            changePercentageIn7d = BigDecimal("-5.00"),
                                            changePercentageIn30d = BigDecimal("0.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    id = "676fb70e-556e-11ee-8c2c-325096b39f47",
                                    cryptoName = "Litecoin",
                                    coingeckoCryptoId = "litecoin",
                                    symbol = "ltc",
                                    image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                ),
                                quantity = "3.125",
                                percentage = 4.04f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "189.34",
                                    totalBTCBalance = "0.007352875",
                                    totalEURBalance = "176.75"
                                ),
                                marketCapRank = 19,
                                marketData = MarketData(
                                    circulatingSupply = "73638701",
                                    maxSupply = "84000000",
                                    currentPrice = CurrentPrice(
                                        usd = "60.59",
                                        eur = "56.56",
                                        btc = "0.00235292"
                                    ),
                                    marketCap = "5259205267",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("6.00"),
                                            changePercentageIn7d = BigDecimal("-2.00"),
                                            changePercentageIn30d = BigDecimal("12.00")
                                    )
                                ),
                                platforms = listOf("COINBASE")
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `should retrieve user cryptos insights sorted by market cap rank ascending`() {
        val cryptos = listOf("bitcoin", "litecoin", "binancecoin")
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

        every { cryptoServiceMock.findAllByIds(setOf("binancecoin", "litecoin", "bitcoin")) } returns cryptosEntities
        every {
            platformServiceMock.findAllByIds(
                    setOf(
                            "a76b400e-8ffc-42d6-bf47-db866eb20153",
                            "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                    )
            )
        } returns listOf(binancePlatform, coinbasePlatform)
        every { userCryptoServiceMock.findAll() } returns userCryptos

        val userCryptosInsights = insightsService.retrieveUserCryptosInsights(0, SortParams(SortBy.MARKET_CAP_RANK, SortType.ASC))

        assertThat(userCryptosInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                PageUserCryptosInsightsResponse(
                                        page = 1,
                                        totalPages = 1,
                                        hasNextPage = false,
                                        balances = BalancesResponse(
                                                totalUSDBalance = "4901.13",
                                                totalBTCBalance = "0.165463035",
                                                totalEURBalance = "4424.55"
                                        ),
                                        cryptos = listOf(
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb38a-556e-11ee-b56e-325096b39f47",
                                                                cryptoName = "Bitcoin",
                                                                coingeckoCryptoId = "bitcoin",
                                                                symbol = "btc",
                                                                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        quantity = "0.15",
                                                        percentage = 91.82f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "4500.00",
                                                                totalBTCBalance = "0.15",
                                                                totalEURBalance = "4050.00"
                                                        ),
                                                        marketCapRank = 1,
                                                        marketData = MarketData(
                                                                circulatingSupply = "19000000",
                                                                maxSupply = "21000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "30000",
                                                                        eur = "27000",
                                                                        btc = "1"
                                                                ),
                                                                marketCap = "813208997089",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("10.00"),
                                                                        changePercentageIn7d = BigDecimal("-5.00"),
                                                                        changePercentageIn30d = BigDecimal("0.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb768-556e-11ee-8b42-325096b39f47",
                                                                cryptoName = "BNB",
                                                                coingeckoCryptoId = "binancecoin",
                                                                symbol = "bnb",
                                                                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                                        ),
                                                        quantity = "1",
                                                        percentage = 4.32f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "211.79",
                                                                totalBTCBalance = "0.00811016",
                                                                totalEURBalance = "197.80"
                                                        ),
                                                        marketCapRank = 4,
                                                        marketData = MarketData(
                                                                circulatingSupply = "153856150",
                                                                maxSupply = "200000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "211.79",
                                                                        eur = "197.80",
                                                                        btc = "0.00811016"
                                                                ),
                                                                marketCap = "48318686968",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb70e-556e-11ee-8c2c-325096b39f47",
                                                                cryptoName = "Litecoin",
                                                                coingeckoCryptoId = "litecoin",
                                                                symbol = "ltc",
                                                                image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        quantity = "3.125",
                                                        percentage = 3.86f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "189.34",
                                                                totalBTCBalance = "0.007352875",
                                                                totalEURBalance = "176.75"
                                                        ),
                                                        marketCapRank = 19,
                                                        marketData = MarketData(
                                                                circulatingSupply = "73638701",
                                                                maxSupply = "84000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "60.59",
                                                                        eur = "56.56",
                                                                        btc = "0.00235292"
                                                                ),
                                                                marketCap = "5259205267",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("COINBASE")
                                                )
                                        )
                                )
                        )
                )
    }

    @Test
    fun `should retrieve user cryptos insights sorted by current price descending`() {
        val cryptos = listOf("bitcoin", "litecoin", "binancecoin")
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

        every { cryptoServiceMock.findAllByIds(setOf("binancecoin", "litecoin", "bitcoin")) } returns cryptosEntities
        every {
            platformServiceMock.findAllByIds(
                    setOf(
                            "a76b400e-8ffc-42d6-bf47-db866eb20153",
                            "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                    )
            )
        } returns listOf(binancePlatform, coinbasePlatform)
        every { userCryptoServiceMock.findAll() } returns userCryptos

        val userCryptosInsights = insightsService.retrieveUserCryptosInsights(0, SortParams(SortBy.CURRENT_PRICE, SortType.DESC))

        assertThat(userCryptosInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                PageUserCryptosInsightsResponse(
                                        page = 1,
                                        totalPages = 1,
                                        hasNextPage = false,
                                        balances = BalancesResponse(
                                                totalUSDBalance = "4901.13",
                                                totalBTCBalance = "0.165463035",
                                                totalEURBalance = "4424.55"
                                        ),
                                        cryptos = listOf(
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb38a-556e-11ee-b56e-325096b39f47",
                                                                cryptoName = "Bitcoin",
                                                                coingeckoCryptoId = "bitcoin",
                                                                symbol = "btc",
                                                                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        quantity = "0.15",
                                                        percentage = 91.82f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "4500.00",
                                                                totalBTCBalance = "0.15",
                                                                totalEURBalance = "4050.00"
                                                        ),
                                                        marketCapRank = 1,
                                                        marketData = MarketData(
                                                                circulatingSupply = "19000000",
                                                                maxSupply = "21000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "30000",
                                                                        eur = "27000",
                                                                        btc = "1"
                                                                ),
                                                                marketCap = "813208997089",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("10.00"),
                                                                        changePercentageIn7d = BigDecimal("-5.00"),
                                                                        changePercentageIn30d = BigDecimal("0.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb768-556e-11ee-8b42-325096b39f47",
                                                                cryptoName = "BNB",
                                                                coingeckoCryptoId = "binancecoin",
                                                                symbol = "bnb",
                                                                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                                        ),
                                                        quantity = "1",
                                                        percentage = 4.32f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "211.79",
                                                                totalBTCBalance = "0.00811016",
                                                                totalEURBalance = "197.80"
                                                        ),
                                                        marketCapRank = 4,
                                                        marketData = MarketData(
                                                                circulatingSupply = "153856150",
                                                                maxSupply = "200000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "211.79",
                                                                        eur = "197.80",
                                                                        btc = "0.00811016"
                                                                ),
                                                                marketCap = "48318686968",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb70e-556e-11ee-8c2c-325096b39f47",
                                                                cryptoName = "Litecoin",
                                                                coingeckoCryptoId = "litecoin",
                                                                symbol = "ltc",
                                                                image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        quantity = "3.125",
                                                        percentage = 3.86f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "189.34",
                                                                totalBTCBalance = "0.007352875",
                                                                totalEURBalance = "176.75"
                                                        ),
                                                        marketCapRank = 19,
                                                        marketData = MarketData(
                                                                circulatingSupply = "73638701",
                                                                maxSupply = "84000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "60.59",
                                                                        eur = "56.56",
                                                                        btc = "0.00235292"
                                                                ),
                                                                marketCap = "5259205267",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("COINBASE")
                                                )
                                        )
                                )
                        )
                )
    }

    @Test
    fun `should retrieve user cryptos insights sorted by max supply descending`() {
        val cryptos = listOf("bitcoin", "litecoin", "binancecoin")
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

        every { cryptoServiceMock.findAllByIds(setOf("binancecoin", "litecoin", "bitcoin")) } returns cryptosEntities
        every {
            platformServiceMock.findAllByIds(
                    setOf(
                            "a76b400e-8ffc-42d6-bf47-db866eb20153",
                            "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                    )
            )
        } returns listOf(binancePlatform, coinbasePlatform)
        every { userCryptoServiceMock.findAll() } returns userCryptos

        val userCryptosInsights = insightsService.retrieveUserCryptosInsights(0, SortParams(SortBy.MAX_SUPPLY, SortType.DESC))

        assertThat(userCryptosInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                PageUserCryptosInsightsResponse(
                                        page = 1,
                                        totalPages = 1,
                                        hasNextPage = false,
                                        balances = BalancesResponse(
                                                totalUSDBalance = "4901.13",
                                                totalBTCBalance = "0.165463035",
                                                totalEURBalance = "4424.55"
                                        ),
                                        cryptos = listOf(
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb768-556e-11ee-8b42-325096b39f47",
                                                                cryptoName = "BNB",
                                                                coingeckoCryptoId = "binancecoin",
                                                                symbol = "bnb",
                                                                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                                        ),
                                                        quantity = "1",
                                                        percentage = 4.32f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "211.79",
                                                                totalBTCBalance = "0.00811016",
                                                                totalEURBalance = "197.80"
                                                        ),
                                                        marketCapRank = 4,
                                                        marketData = MarketData(
                                                                circulatingSupply = "153856150",
                                                                maxSupply = "200000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "211.79",
                                                                        eur = "197.80",
                                                                        btc = "0.00811016"
                                                                ),
                                                                marketCap = "48318686968",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb70e-556e-11ee-8c2c-325096b39f47",
                                                                cryptoName = "Litecoin",
                                                                coingeckoCryptoId = "litecoin",
                                                                symbol = "ltc",
                                                                image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        quantity = "3.125",
                                                        percentage = 3.86f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "189.34",
                                                                totalBTCBalance = "0.007352875",
                                                                totalEURBalance = "176.75"
                                                        ),
                                                        marketCapRank = 19,
                                                        marketData = MarketData(
                                                                circulatingSupply = "73638701",
                                                                maxSupply = "84000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "60.59",
                                                                        eur = "56.56",
                                                                        btc = "0.00235292"
                                                                ),
                                                                marketCap = "5259205267",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("COINBASE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb38a-556e-11ee-b56e-325096b39f47",
                                                                cryptoName = "Bitcoin",
                                                                coingeckoCryptoId = "bitcoin",
                                                                symbol = "btc",
                                                                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        quantity = "0.15",
                                                        percentage = 91.82f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "4500.00",
                                                                totalBTCBalance = "0.15",
                                                                totalEURBalance = "4050.00"
                                                        ),
                                                        marketCapRank = 1,
                                                        marketData = MarketData(
                                                                circulatingSupply = "19000000",
                                                                maxSupply = "21000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "30000",
                                                                        eur = "27000",
                                                                        btc = "1"
                                                                ),
                                                                marketCap = "813208997089",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("10.00"),
                                                                        changePercentageIn7d = BigDecimal("-5.00"),
                                                                        changePercentageIn30d = BigDecimal("0.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                )
                                        )
                                )
                        )
                )
    }

    @Test
    fun `should retrieve user cryptos insights sorted by 24h change descending`() {
        val cryptos = listOf("bitcoin", "litecoin", "binancecoin")
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

        every { cryptoServiceMock.findAllByIds(setOf("binancecoin", "litecoin", "bitcoin")) } returns cryptosEntities
        every {
            platformServiceMock.findAllByIds(
                    setOf(
                            "a76b400e-8ffc-42d6-bf47-db866eb20153",
                            "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                    )
            )
        } returns listOf(binancePlatform, coinbasePlatform)
        every { userCryptoServiceMock.findAll() } returns userCryptos

        val userCryptosInsights = insightsService.retrieveUserCryptosInsights(0, SortParams(SortBy.CHANGE_PRICE_IN_24H, SortType.DESC))

        assertThat(userCryptosInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                PageUserCryptosInsightsResponse(
                                        page = 1,
                                        totalPages = 1,
                                        hasNextPage = false,
                                        balances = BalancesResponse(
                                                totalUSDBalance = "4901.13",
                                                totalBTCBalance = "0.165463035",
                                                totalEURBalance = "4424.55"
                                        ),
                                        cryptos = listOf(
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb38a-556e-11ee-b56e-325096b39f47",
                                                                cryptoName = "Bitcoin",
                                                                coingeckoCryptoId = "bitcoin",
                                                                symbol = "btc",
                                                                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        quantity = "0.15",
                                                        percentage = 91.82f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "4500.00",
                                                                totalBTCBalance = "0.15",
                                                                totalEURBalance = "4050.00"
                                                        ),
                                                        marketCapRank = 1,
                                                        marketData = MarketData(
                                                                circulatingSupply = "19000000",
                                                                maxSupply = "21000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "30000",
                                                                        eur = "27000",
                                                                        btc = "1"
                                                                ),
                                                                marketCap = "813208997089",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("10.00"),
                                                                        changePercentageIn7d = BigDecimal("-5.00"),
                                                                        changePercentageIn30d = BigDecimal("0.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb70e-556e-11ee-8c2c-325096b39f47",
                                                                cryptoName = "Litecoin",
                                                                coingeckoCryptoId = "litecoin",
                                                                symbol = "ltc",
                                                                image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        quantity = "3.125",
                                                        percentage = 3.86f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "189.34",
                                                                totalBTCBalance = "0.007352875",
                                                                totalEURBalance = "176.75"
                                                        ),
                                                        marketCapRank = 19,
                                                        marketData = MarketData(
                                                                circulatingSupply = "73638701",
                                                                maxSupply = "84000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "60.59",
                                                                        eur = "56.56",
                                                                        btc = "0.00235292"
                                                                ),
                                                                marketCap = "5259205267",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("COINBASE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb768-556e-11ee-8b42-325096b39f47",
                                                                cryptoName = "BNB",
                                                                coingeckoCryptoId = "binancecoin",
                                                                symbol = "bnb",
                                                                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                                        ),
                                                        quantity = "1",
                                                        percentage = 4.32f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "211.79",
                                                                totalBTCBalance = "0.00811016",
                                                                totalEURBalance = "197.80"
                                                        ),
                                                        marketCapRank = 4,
                                                        marketData = MarketData(
                                                                circulatingSupply = "153856150",
                                                                maxSupply = "200000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "211.79",
                                                                        eur = "197.80",
                                                                        btc = "0.00811016"
                                                                ),
                                                                marketCap = "48318686968",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                )
                                        )
                                )
                        )
                )
    }

    @Test
    fun `should retrieve user cryptos insights sorted by 7D change ascending`() {
        val cryptos = listOf("bitcoin", "litecoin", "binancecoin")
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

        every { cryptoServiceMock.findAllByIds(setOf("binancecoin", "litecoin", "bitcoin")) } returns cryptosEntities
        every {
            platformServiceMock.findAllByIds(
                    setOf(
                            "a76b400e-8ffc-42d6-bf47-db866eb20153",
                            "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                    )
            )
        } returns listOf(binancePlatform, coinbasePlatform)
        every { userCryptoServiceMock.findAll() } returns userCryptos

        val userCryptosInsights = insightsService.retrieveUserCryptosInsights(0, SortParams(SortBy.CHANGE_PRICE_IN_7D, SortType.ASC))

        assertThat(userCryptosInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                PageUserCryptosInsightsResponse(
                                        page = 1,
                                        totalPages = 1,
                                        hasNextPage = false,
                                        balances = BalancesResponse(
                                                totalUSDBalance = "4901.13",
                                                totalBTCBalance = "0.165463035",
                                                totalEURBalance = "4424.55"
                                        ),
                                        cryptos = listOf(
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb38a-556e-11ee-b56e-325096b39f47",
                                                                cryptoName = "Bitcoin",
                                                                coingeckoCryptoId = "bitcoin",
                                                                symbol = "btc",
                                                                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        quantity = "0.15",
                                                        percentage = 91.82f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "4500.00",
                                                                totalBTCBalance = "0.15",
                                                                totalEURBalance = "4050.00"
                                                        ),
                                                        marketCapRank = 1,
                                                        marketData = MarketData(
                                                                circulatingSupply = "19000000",
                                                                maxSupply = "21000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "30000",
                                                                        eur = "27000",
                                                                        btc = "1"
                                                                ),
                                                                marketCap = "813208997089",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("10.00"),
                                                                        changePercentageIn7d = BigDecimal("-5.00"),
                                                                        changePercentageIn30d = BigDecimal("0.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb70e-556e-11ee-8c2c-325096b39f47",
                                                                cryptoName = "Litecoin",
                                                                coingeckoCryptoId = "litecoin",
                                                                symbol = "ltc",
                                                                image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        quantity = "3.125",
                                                        percentage = 3.86f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "189.34",
                                                                totalBTCBalance = "0.007352875",
                                                                totalEURBalance = "176.75"
                                                        ),
                                                        marketCapRank = 19,
                                                        marketData = MarketData(
                                                                circulatingSupply = "73638701",
                                                                maxSupply = "84000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "60.59",
                                                                        eur = "56.56",
                                                                        btc = "0.00235292"
                                                                ),
                                                                marketCap = "5259205267",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("COINBASE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb768-556e-11ee-8b42-325096b39f47",
                                                                cryptoName = "BNB",
                                                                coingeckoCryptoId = "binancecoin",
                                                                symbol = "bnb",
                                                                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                                        ),
                                                        quantity = "1",
                                                        percentage = 4.32f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "211.79",
                                                                totalBTCBalance = "0.00811016",
                                                                totalEURBalance = "197.80"
                                                        ),
                                                        marketCapRank = 4,
                                                        marketData = MarketData(
                                                                circulatingSupply = "153856150",
                                                                maxSupply = "200000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "211.79",
                                                                        eur = "197.80",
                                                                        btc = "0.00811016"
                                                                ),
                                                                marketCap = "48318686968",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                )
                                        )
                                )
                        )
                )
    }

    @Test
    fun `should retrieve user cryptos insights sorted by 30D change descending`() {
        val cryptos = listOf("bitcoin", "litecoin", "binancecoin")
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

        every { cryptoServiceMock.findAllByIds(setOf("binancecoin", "litecoin", "bitcoin")) } returns cryptosEntities
        every {
            platformServiceMock.findAllByIds(
                    setOf(
                            "a76b400e-8ffc-42d6-bf47-db866eb20153",
                            "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                    )
            )
        } returns listOf(binancePlatform, coinbasePlatform)
        every { userCryptoServiceMock.findAll() } returns userCryptos

        val userCryptosInsights = insightsService.retrieveUserCryptosInsights(0, SortParams(SortBy.CHANGE_PRICE_IN_30D, SortType.DESC))

        assertThat(userCryptosInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                PageUserCryptosInsightsResponse(
                                        page = 1,
                                        totalPages = 1,
                                        hasNextPage = false,
                                        balances = BalancesResponse(
                                                totalUSDBalance = "4901.13",
                                                totalBTCBalance = "0.165463035",
                                                totalEURBalance = "4424.55"
                                        ),
                                        cryptos = listOf(
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb70e-556e-11ee-8c2c-325096b39f47",
                                                                cryptoName = "Litecoin",
                                                                coingeckoCryptoId = "litecoin",
                                                                symbol = "ltc",
                                                                image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        quantity = "3.125",
                                                        percentage = 3.86f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "189.34",
                                                                totalBTCBalance = "0.007352875",
                                                                totalEURBalance = "176.75"
                                                        ),
                                                        marketCapRank = 19,
                                                        marketData = MarketData(
                                                                circulatingSupply = "73638701",
                                                                maxSupply = "84000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "60.59",
                                                                        eur = "56.56",
                                                                        btc = "0.00235292"
                                                                ),
                                                                marketCap = "5259205267",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("COINBASE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb768-556e-11ee-8b42-325096b39f47",
                                                                cryptoName = "BNB",
                                                                coingeckoCryptoId = "binancecoin",
                                                                symbol = "bnb",
                                                                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                                        ),
                                                        quantity = "1",
                                                        percentage = 4.32f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "211.79",
                                                                totalBTCBalance = "0.00811016",
                                                                totalEURBalance = "197.80"
                                                        ),
                                                        marketCapRank = 4,
                                                        marketData = MarketData(
                                                                circulatingSupply = "153856150",
                                                                maxSupply = "200000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "211.79",
                                                                        eur = "197.80",
                                                                        btc = "0.00811016"
                                                                ),
                                                                marketCap = "48318686968",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("6.00"),
                                                                        changePercentageIn7d = BigDecimal("-2.00"),
                                                                        changePercentageIn30d = BigDecimal("12.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                ),
                                                UserCryptosInsights(
                                                        cryptoInfo = CryptoInfo(
                                                                id = "676fb38a-556e-11ee-b56e-325096b39f47",
                                                                cryptoName = "Bitcoin",
                                                                coingeckoCryptoId = "bitcoin",
                                                                symbol = "btc",
                                                                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        quantity = "0.15",
                                                        percentage = 91.82f,
                                                        balances = BalancesResponse(
                                                                totalUSDBalance = "4500.00",
                                                                totalBTCBalance = "0.15",
                                                                totalEURBalance = "4050.00"
                                                        ),
                                                        marketCapRank = 1,
                                                        marketData = MarketData(
                                                                circulatingSupply = "19000000",
                                                                maxSupply = "21000000",
                                                                currentPrice = CurrentPrice(
                                                                        usd = "30000",
                                                                        eur = "27000",
                                                                        btc = "1"
                                                                ),
                                                                marketCap = "813208997089",
                                                                priceChange = PriceChange(
                                                                        changePercentageIn24h = BigDecimal("10.00"),
                                                                        changePercentageIn7d = BigDecimal("-5.00"),
                                                                        changePercentageIn30d = BigDecimal("0.00")
                                                                )
                                                        ),
                                                        platforms = listOf("BINANCE")
                                                )
                                        )
                                )
                        )
                )
    }

    @Test
    fun `should retrieve empty if no user cryptos are found for retrieveUserCryptosInsights`() {
        every { userCryptoServiceMock.findAll() } returns emptyList()

        val userCryptosInsights = insightsService.retrieveUserCryptosInsights(0)

        assertThat(userCryptosInsights)
            .usingRecursiveComparison()
            .isEqualTo(Optional.empty<PageUserCryptosInsightsResponse>())
    }

    @Test
    fun `should retrieve user cryptos platforms insights`() {
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

        val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0)

        assertThat(userCryptosPlatformsInsights)
            .usingRecursiveComparison()
            .isEqualTo(
                Optional.of(
                    PageUserCryptosInsightsResponse(
                        page = 1,
                        totalPages = 1,
                        hasNextPage = false,
                        balances = BalancesResponse(
                            totalUSDBalance = "6919.05",
                            totalBTCBalance = "0.24392648432",
                            totalEURBalance = "6307.48"
                        ),
                        cryptos = listOf(
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Bitcoin",
                                    coingeckoCryptoId = "bitcoin",
                                    symbol = "btc",
                                    image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                ),
                                quantity = "0.15",
                                percentage = 65.04f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "4500.00",
                                    totalBTCBalance = "0.15",
                                    totalEURBalance = "4050.00"
                                ),
                                marketCapRank = 1,
                                marketData = MarketData(
                                    circulatingSupply = "19000000",
                                    maxSupply = "21000000",
                                    currentPrice = CurrentPrice(
                                        usd = "30000",
                                        eur = "27000",
                                        btc = "1"
                                    ),
                                    marketCap = "813208997089",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("10.00"),
                                            changePercentageIn7d = BigDecimal("-5.00"),
                                            changePercentageIn30d = BigDecimal("0.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Ethereum",
                                    coingeckoCryptoId = "ethereum",
                                    symbol = "eth",
                                    image = "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880"
                                ),
                                quantity = "1.372",
                                percentage = 32.07f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "2219.13",
                                    totalBTCBalance = "0.08616648432",
                                    totalEURBalance = "2070.86"
                                ),
                                marketCapRank = 2,
                                marketData = MarketData(
                                    circulatingSupply = "120220572",
                                    maxSupply = "0",
                                    currentPrice = CurrentPrice(
                                        usd = "1617.44",
                                        eur = "1509.37",
                                        btc = "0.06280356"
                                    ),
                                    marketCap = "298219864117",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("10.00"),
                                            changePercentageIn7d = BigDecimal("-5.00"),
                                            changePercentageIn30d = BigDecimal("2.00")
                                    )
                                ),
                                platforms = listOf("BINANCE", "COINBASE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Tether",
                                    coingeckoCryptoId = "tether",
                                    symbol = "usdt",
                                    image = "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663"
                                ),
                                quantity = "200",
                                percentage = 2.89f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "199.92",
                                    totalBTCBalance = "0.00776",
                                    totalEURBalance = "186.62"
                                ),
                                marketCapRank = 3,
                                marketData = MarketData(
                                    circulatingSupply = "83016246102",
                                    maxSupply = "0",
                                    currentPrice = CurrentPrice(
                                        usd = "0.999618",
                                        eur = "0.933095",
                                        btc = "0.0000388"
                                    ),
                                    marketCap = "95085861049",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("0.00"),
                                            changePercentageIn7d = BigDecimal("0.00"),
                                            changePercentageIn30d = BigDecimal("0.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `should retrieve user cryptos platforms insights with next page`() {
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

        val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0)

        assertTrue(userCryptosPlatformsInsights.isPresent)
        assertThat(userCryptosPlatformsInsights.get().cryptos.size).isEqualTo(10)
        assertThat(userCryptosPlatformsInsights)
            .usingRecursiveComparison()
            .isEqualTo(
                Optional.of(
                    PageUserCryptosInsightsResponse(
                        page = 1,
                        totalPages = 2,
                        hasNextPage = true,
                        balances = BalancesResponse(
                            totalUSDBalance = "8373.63",
                            totalBTCBalance = "0.29959591932",
                            totalEURBalance = "7663.61"
                        ),
                        cryptos = listOf(
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Bitcoin",
                                    coingeckoCryptoId = "bitcoin",
                                    symbol = "btc",
                                    image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                ),
                                quantity = "0.15",
                                percentage = 53.74f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "4500.00",
                                    totalBTCBalance = "0.15",
                                    totalEURBalance = "4050.00"
                                ),
                                marketCapRank = 1,
                                marketData = MarketData(
                                    circulatingSupply = "19000000",
                                    maxSupply = "21000000",
                                    currentPrice = CurrentPrice(
                                        usd = "30000",
                                        eur = "27000",
                                        btc = "1"
                                    ),
                                    marketCap = "813208997089",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("10.00"),
                                            changePercentageIn7d = BigDecimal("-5.00"),
                                            changePercentageIn30d = BigDecimal("0.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Ethereum",
                                    coingeckoCryptoId = "ethereum",
                                    symbol = "eth",
                                    image = "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880"
                                ),
                                quantity = "1.372",
                                percentage = 26.5f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "2219.13",
                                    totalBTCBalance = "0.08616648432",
                                    totalEURBalance = "2070.86"
                                ),
                                marketCapRank = 2,
                                marketData = MarketData(
                                    circulatingSupply = "120220572",
                                    maxSupply = "0",
                                    currentPrice = CurrentPrice(
                                        usd = "1617.44",
                                        eur = "1509.37",
                                        btc = "0.06280356"
                                    ),
                                    marketCap = "298219864117",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("10.00"),
                                            changePercentageIn7d = BigDecimal("-5.00"),
                                            changePercentageIn30d = BigDecimal("2.00")
                                    )
                                ),
                                platforms = listOf("BINANCE", "COINBASE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Avalanche",
                                    coingeckoCryptoId = "avalanche-2",
                                    symbol = "avax",
                                    image = "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574"
                                ),
                                quantity = "25",
                                percentage = 2.78f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "232.50",
                                    totalBTCBalance = "0.008879",
                                    totalEURBalance = "216.75"
                                ),
                                marketCapRank = 10,
                                marketData = MarketData(
                                    circulatingSupply = "353804673",
                                    maxSupply = "720000000",
                                    currentPrice = CurrentPrice(
                                        usd = "9.3",
                                        eur = "8.67",
                                        btc = "0.00035516"
                                    ),
                                    marketCap = "11953262327",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("4.00"),
                                            changePercentageIn7d = BigDecimal("1.00"),
                                            changePercentageIn30d = BigDecimal("8.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "BNB",
                                    coingeckoCryptoId = "binancecoin",
                                    symbol = "bnb",
                                    image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                ),
                                quantity = "1",
                                percentage = 2.53f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "211.79",
                                    totalBTCBalance = "0.00811016",
                                    totalEURBalance = "197.80"
                                ),
                                marketCapRank = 4,
                                marketData = MarketData(
                                    circulatingSupply = "153856150",
                                    maxSupply = "200000000",
                                    currentPrice = CurrentPrice(
                                        usd = "211.79",
                                        eur = "197.80",
                                        btc = "0.00811016"
                                    ),
                                    marketCap = "48318686968",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("6.00"),
                                            changePercentageIn7d = BigDecimal("-2.00"),
                                            changePercentageIn30d = BigDecimal("12.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Chainlink",
                                    coingeckoCryptoId = "chainlink",
                                    symbol = "link",
                                    image = "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1547034700"
                                ),
                                quantity = "35",
                                percentage = 2.5f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "209.65",
                                    totalBTCBalance = "0.0080031",
                                    totalEURBalance = "195.30"
                                ),
                                marketCapRank = 16,
                                marketData = MarketData(
                                    circulatingSupply = "538099971",
                                    maxSupply = "1000000000",
                                    currentPrice = CurrentPrice(
                                        usd = "5.99",
                                        eur = "5.58",
                                        btc = "0.00022866"
                                    ),
                                    marketCap = "9021587267",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("4.00"),
                                            changePercentageIn7d = BigDecimal("-1.00"),
                                            changePercentageIn30d = BigDecimal("8.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Tether",
                                    coingeckoCryptoId = "tether",
                                    symbol = "usdt",
                                    image = "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663"
                                ),
                                quantity = "200",
                                percentage = 2.39f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "199.92",
                                    totalBTCBalance = "0.00776",
                                    totalEURBalance = "186.62"
                                ),
                                marketCapRank = 3,
                                marketData = MarketData(
                                    circulatingSupply = "83016246102",
                                    maxSupply = "0",
                                    currentPrice = CurrentPrice(
                                        usd = "0.999618",
                                        eur = "0.933095",
                                        btc = "0.0000388"
                                    ),
                                    marketCap = "95085861049",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("0.00"),
                                            changePercentageIn7d = BigDecimal("0.00"),
                                            changePercentageIn30d = BigDecimal("0.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Litecoin",
                                    coingeckoCryptoId = "litecoin",
                                    symbol = "ltc",
                                    image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                ),
                                quantity = "3.125",
                                percentage = 2.26f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "189.34",
                                    totalBTCBalance = "0.007352875",
                                    totalEURBalance = "176.75"
                                ),
                                marketCapRank = 19,
                                marketData = MarketData(
                                    circulatingSupply = "73638701",
                                    maxSupply = "84000000",
                                    currentPrice = CurrentPrice(
                                        usd = "60.59",
                                        eur = "56.56",
                                        btc = "0.00235292"
                                    ),
                                    marketCap = "5259205267",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("6.00"),
                                            changePercentageIn7d = BigDecimal("-2.00"),
                                            changePercentageIn30d = BigDecimal("12.00")
                                    )
                                ),
                                platforms = listOf("COINBASE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Solana",
                                    coingeckoCryptoId = "solana",
                                    symbol = "sol",
                                    image = "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422"
                                ),
                                quantity = "10",
                                percentage = 2.15f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "180.40",
                                    totalBTCBalance = "0.0068809",
                                    totalEURBalance = "168.20"
                                ),
                                marketCapRank = 5,
                                marketData = MarketData(
                                    circulatingSupply = "410905807",
                                    maxSupply = "0",
                                    currentPrice = CurrentPrice(
                                        usd = "18.04",
                                        eur = "16.82",
                                        btc = "0.00068809"
                                    ),
                                    marketCap = "40090766907",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("4.00"),
                                            changePercentageIn7d = BigDecimal("1.00"),
                                            changePercentageIn30d = BigDecimal("-2.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Polkadot",
                                    coingeckoCryptoId = "polkadot",
                                    symbol = "dot",
                                    image = "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644"
                                ),
                                quantity = "40",
                                percentage = 1.92f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "160.40",
                                    totalBTCBalance = "0.0061208",
                                    totalEURBalance = "149.20"
                                ),
                                marketCapRank = 13,
                                marketData = MarketData(
                                    circulatingSupply = "1274258350",
                                    maxSupply = "0",
                                    currentPrice = CurrentPrice(
                                        usd = "4.01",
                                        eur = "3.73",
                                        btc = "0.00015302"
                                    ),
                                    marketCap = "8993575127",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("4.00"),
                                            changePercentageIn7d = BigDecimal("-1.00"),
                                            changePercentageIn30d = BigDecimal("2.00")
                                    )
                                ),
                                platforms = listOf("COINBASE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Uniswap",
                                    coingeckoCryptoId = "uniswap",
                                    symbol = "uni",
                                    image = "https://assets.coingecko.com/coins/images/12504/large/uni.jpg?1687143398"
                                ),
                                quantity = "30",
                                percentage = 1.52f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "127.50",
                                    totalBTCBalance = "0.0048591",
                                    totalEURBalance = "118.80"
                                ),
                                marketCapRank = 22,
                                marketData = MarketData(
                                    circulatingSupply = "753766667",
                                    maxSupply = "1000000000",
                                    currentPrice = CurrentPrice(
                                        usd = "4.25",
                                        eur = "3.96",
                                        btc = "0.00016197"
                                    ),
                                    marketCap = "4772322900",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("2.00"),
                                            changePercentageIn7d = BigDecimal("-1.00"),
                                            changePercentageIn30d = BigDecimal("3.00")
                                    )
                                ),
                                platforms = listOf("COINBASE")
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `should retrieve user cryptos platforms insights for second page`() {
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

        val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(1)

        assertThat(userCryptosPlatformsInsights)
            .usingRecursiveComparison()
            .isEqualTo(
                Optional.of(
                    PageUserCryptosInsightsResponse(
                        page = 2,
                        totalPages = 2,
                        hasNextPage = false,
                        balances = BalancesResponse(
                            totalUSDBalance = "8373.63",
                            totalBTCBalance = "0.29959591932",
                            totalEURBalance = "7663.61"
                        ),
                        cryptos = listOf(
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Polygon",
                                    coingeckoCryptoId = "matic-network",
                                    symbol = "matic",
                                    image = "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png?1624446912"
                                ),
                                quantity = "100",
                                percentage = 0.61f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "51.00",
                                    totalBTCBalance = "0.001947",
                                    totalEURBalance = "47.54"
                                ),
                                marketCapRank = 16,
                                marketData = MarketData(
                                    circulatingSupply = "9319469069",
                                    maxSupply = "10000000000",
                                    currentPrice = CurrentPrice(
                                        usd = "0.509995",
                                        eur = "0.475407",
                                        btc = "0.00001947"
                                    ),
                                    marketCap = "7001911961",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("14.00"),
                                            changePercentageIn7d = BigDecimal("-10.00"),
                                            changePercentageIn30d = BigDecimal("2.00")
                                    )
                                ),
                                platforms = listOf("COINBASE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Cardano",
                                    coingeckoCryptoId = "cardano",
                                    symbol = "ada",
                                    image = "https://assets.coingecko.com/coins/images/975/large/cardano.png?1547034860"
                                ),
                                quantity = "150",
                                percentage = 0.45f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "37.34",
                                    totalBTCBalance = "0.001425",
                                    totalEURBalance = "34.80"
                                ),
                                marketCapRank = 9,
                                marketData = MarketData(
                                    circulatingSupply = "35045020830",
                                    maxSupply = "45000000000",
                                    currentPrice = CurrentPrice(
                                        usd = "0.248915",
                                        eur = "0.231985",
                                        btc = "0.0000095"
                                    ),
                                    marketCap = "29348197308",
                                    priceChange = PriceChange(
                                            changePercentageIn24h = BigDecimal("7.00"),
                                            changePercentageIn7d = BigDecimal("1.00"),
                                            changePercentageIn30d = BigDecimal("-2.00")
                                    )
                                ),
                                platforms = listOf("BINANCE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "Dogecoin",
                                    coingeckoCryptoId = "dogecoin",
                                    symbol = "doge",
                                    image = "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1547792256"
                                ),
                                quantity = "500",
                                percentage = 0.37f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "30.74",
                                    totalBTCBalance = "0.001175",
                                    totalEURBalance = "28.66"
                                ),
                                marketCapRank = 11,
                                marketData = MarketData(
                                    circulatingSupply = "140978466383",
                                    maxSupply = "0",
                                    currentPrice = CurrentPrice(
                                        usd = "0.061481",
                                        eur = "0.057319",
                                        btc = "0.00000235"
                                    ),
                                    marketCap = "11195832359",
                                    priceChange = PriceChange(
                                        changePercentageIn24h = BigDecimal("-4.00"),
                                        changePercentageIn7d = BigDecimal("-1.00"),
                                        changePercentageIn30d = BigDecimal("-8.00")
                                    )
                                ),
                                platforms = listOf("COINBASE")
                            ),
                            UserCryptosInsights(
                                cryptoInfo = CryptoInfo(
                                    cryptoName = "XRP",
                                    coingeckoCryptoId = "ripple",
                                    symbol = "xrp",
                                    image = "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1605778731"
                                ),
                                quantity = "50",
                                percentage = 0.29f,
                                balances = BalancesResponse(
                                    totalUSDBalance = "23.92",
                                    totalBTCBalance = "0.0009165",
                                    totalEURBalance = "22.33"
                                ),
                                marketCapRank = 6,
                                marketData = MarketData(
                                    circulatingSupply = "53083046512",
                                    maxSupply = "100000000000",
                                    currentPrice = CurrentPrice(
                                        usd = "0.478363",
                                        eur = "0.446699",
                                        btc = "0.00001833"
                                    ),
                                    marketCap = "29348197308",
                                    priceChange = PriceChange(
                                        changePercentageIn24h = BigDecimal("2.00"),
                                        changePercentageIn7d = BigDecimal("3.00"),
                                        changePercentageIn30d = BigDecimal("-5.00")
                                    )
                                ),
                                platforms = listOf("COINBASE")
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `should retrieve empty if no user cryptos are found for retrieveUserCryptosPlatformsInsights`() {
        every { userCryptoServiceMock.findAll() } returns emptyList()

        val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0)

        assertThat(userCryptosPlatformsInsights)
            .usingRecursiveComparison()
            .isEqualTo(Optional.empty<PageUserCryptosInsightsResponse>())
    }

    @Test
    fun `should retrieve empty if no user cryptos are found for page for retrieveUserCryptosPlatformsInsights`() {
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

        val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(1)

        assertThat(userCryptosPlatformsInsights)
            .usingRecursiveComparison()
            .isEqualTo(Optional.empty<PageUserCryptosInsightsResponse>())
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
}