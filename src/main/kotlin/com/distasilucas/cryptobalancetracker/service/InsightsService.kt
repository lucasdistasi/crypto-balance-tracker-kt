package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.CurrentPrice
import com.distasilucas.cryptobalancetracker.model.response.insights.MarketData
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Optional
import kotlin.math.ceil

private const val ELEMENTS_PER_PAGE = 10.0
private const val INT_ELEMENTS_PER_PAGE = ELEMENTS_PER_PAGE.toInt()

@Service
class InsightsService(
    private val platformService: PlatformService,
    private val userCryptoService: UserCryptoService,
    private val cryptoService: CryptoService
) {

    private val logger = KotlinLogging.logger { }

    fun retrieveTotalBalancesInsights(): Optional<BalancesResponse> {
        logger.info { "Retrieving total balances" }

        val userCryptos = userCryptoService.findAll()

        if (userCryptos.isEmpty()) {
            return Optional.empty()
        }

        val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
        val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
        val cryptos = cryptoService.findAllByIds(cryptosIds)
        val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

        return Optional.of(totalBalances)
    }

    fun retrievePlatformInsights(platformId: String): Optional<PlatformInsightsResponse> {
        logger.info { "Retrieving insights for platform with id $platformId" }

        val userCryptos = userCryptoService.findAllByPlatformId(platformId)

        if (userCryptos.isEmpty()) {
            return Optional.empty()
        }

        val platformResponse = platformService.retrievePlatformById(platformId)
        val cryptosIds = userCryptos.map { it.coingeckoCryptoId }
        val cryptos = cryptoService.findAllByIds(cryptosIds)
        val userCryptosQuantity = getUserCryptoQuantity(userCryptos)
        val totalBalances = getTotalBalances(cryptos, userCryptosQuantity)

        val cryptosInsights = cryptos.map { crypto ->
            val quantity = userCryptosQuantity[crypto.id]
            val cryptoTotalBalances = getCryptoTotalBalances(crypto, quantity!!)

            val cryptoInsight = CryptoInsights(
                cryptoName = crypto.name,
                cryptoId = crypto.id,
                quantity = quantity.toPlainString(),
                balances = cryptoTotalBalances,
                percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoTotalBalances.totalUSDBalance)
            )

            cryptoInsight
        }.sortedByDescending { it.percentage }

        return Optional.of(
            PlatformInsightsResponse(
                platformName = platformResponse.name,
                balances = totalBalances,
                cryptos = cryptosInsights
            )
        )
    }

    fun retrieveCryptoInsights(coingeckoCryptoId: String): Optional<CryptoInsightResponse> {
        logger.info { "Retrieving insights for crypto with coingeckoCryptoId $coingeckoCryptoId" }

        val userCryptos = userCryptoService.findAllByCoingeckoCryptoId(coingeckoCryptoId)

        if (userCryptos.isEmpty()) {
            return Optional.empty()
        }

        val platformsIds = userCryptos.map { it.platformId }
        val platforms = platformService.findAllByIds(platformsIds)
        val crypto = cryptoService.retrieveCryptoInfoById(coingeckoCryptoId)

        val platformUserCryptoQuantity = userCryptos.associateBy({ it.platformId }, { it.quantity })
        val totalCryptoQuantity = userCryptos.map { it.quantity }.fold(BigDecimal.ZERO, BigDecimal::add)
        val totalBalances = getTotalBalances(listOf(crypto), mapOf(coingeckoCryptoId to totalCryptoQuantity))

        val platformInsights = platforms.map { platform ->
            val quantity = platformUserCryptoQuantity[platform.id]
            val cryptoTotalBalances = getCryptoTotalBalances(crypto, quantity!!)

            val platformInsight = PlatformInsight(
                quantity = quantity.toPlainString(),
                balances = cryptoTotalBalances,
                percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoTotalBalances.totalUSDBalance),
                platformName = platform.name
            )

            platformInsight
        }.sortedByDescending { it.percentage }

        return Optional.of(
            CryptoInsightResponse(
                cryptoName = crypto.name,
                balances = totalBalances,
                platforms = platformInsights
            )
        )
    }

    fun retrievePlatformsBalancesInsights(): Optional<PlatformsBalancesInsightsResponse> {
        logger.info { "Retrieving all platforms balances insights" }

        val userCryptos = userCryptoService.findAll()

        if (userCryptos.isEmpty()) {
            return Optional.empty()
        }

        val platformsIds = userCryptos.map { it.platformId }.toSet()
        val platforms = platformService.findAllByIds(platformsIds)
        val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
        val platformsUserCryptos = getPlatformsUserCryptos(userCryptos, platforms)
        val cryptosIds = platformsUserCryptos.values.flatMap { it.map { it.coingeckoCryptoId } }.toSet()
        val cryptos = cryptoService.findAllByIds(cryptosIds)
        val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

        val platformsInsights = platformsUserCryptos.map { (platformName, userCryptos) ->
            var totalUSDBalance = BigDecimal.ZERO
            var totalBTCBalance = BigDecimal.ZERO
            var totalEURBalance = BigDecimal.ZERO

            userCryptos.forEach { crypto ->
                val balance = getCryptoTotalBalances(cryptos.first { it.id == crypto.coingeckoCryptoId }, crypto.quantity)
                totalUSDBalance = totalUSDBalance.plus(BigDecimal(balance.totalUSDBalance))
                totalBTCBalance = totalBTCBalance.plus(BigDecimal(balance.totalBTCBalance))
                totalEURBalance = totalEURBalance.plus(BigDecimal(balance.totalEURBalance))
            }

            val platformInsight = PlatformsInsights(
                platformName = platformName,
                balances = BalancesResponse(
                    totalUSDBalance = totalUSDBalance.toPlainString(),
                    totalBTCBalance = totalBTCBalance.toPlainString(),
                    totalEURBalance = totalEURBalance.toPlainString()
                ),
                percentage = calculatePercentage(totalBalances.totalUSDBalance, totalUSDBalance.toPlainString())
            )

            platformInsight
        }.sortedByDescending { it.percentage }

        return Optional.of(
            PlatformsBalancesInsightsResponse(
                balances = totalBalances,
                platforms = platformsInsights
            )
        )
    }

    fun retrieveCryptosBalancesInsights(): Optional<CryptosBalancesInsightsResponse> {
        logger.info { "Retrieving all cryptos balances insights" }

        val userCryptos = userCryptoService.findAll()

        if (userCryptos.isEmpty()) {
            return Optional.empty()
        }

        val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
        val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
        val cryptos = cryptoService.findAllByIds(cryptosIds)
        val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

        val cryptosInsights = userCryptoQuantity.map { (coingeckoCryptoId, quantity) ->
            val crypto = cryptos.first { it.id == coingeckoCryptoId }
            val cryptoBalances = getCryptoTotalBalances(crypto, quantity)

            CryptoInsights(
                cryptoName = crypto.name,
                cryptoId = coingeckoCryptoId,
                quantity = quantity.toPlainString(),
                balances = cryptoBalances,
                percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoBalances.totalUSDBalance)
            )
        }.sortedByDescending { it.percentage }

        return Optional.of(
            CryptosBalancesInsightsResponse(
                balances = totalBalances,
                cryptos = if (cryptosInsights.size > 12)
                    getCryptoInsightsWithOthers(totalBalances, cryptosInsights) else cryptosInsights
            )
        )
    }

    fun retrieveUserCryptosInsights(page: Int): Optional<PageUserCryptosInsightsResponse> {
        logger.info { "Retrieving user cryptos insights for page $page" }

        val userCryptosPage = userCryptoService.findAllByPage(page)

        if (userCryptosPage.isEmpty) {
            return Optional.empty()
        }

        val cryptosIds = userCryptosPage.map { it.coingeckoCryptoId }.toSet()
        val platformsIds = userCryptosPage.map { it.platformId }.toSet()
        val cryptos = cryptoService.findAllByIds(cryptosIds)
        val platforms = platformService.findAllByIds(platformsIds)

        // Not the best because I'm paginating, but I need total balances to calculate individual percentages
        val userCryptos = userCryptoService.findAll()
        val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
        val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

        val cryptosInsights = userCryptosPage.content.map {
            val crypto = cryptos.first { crypto -> crypto.id == it.coingeckoCryptoId }
            val platform = platforms.first { platform -> platform.id == it.platformId }
            val balances = getCryptoTotalBalances(crypto, it.quantity)

            UserCryptosInsights(
                cryptoInfo = CryptoInfo(
                    id = it.id,
                    cryptoName = crypto.name,
                    coingeckoCryptoId = crypto.id,
                    symbol = crypto.ticker,
                    image = crypto.image
                ),
                quantity = it.quantity.toPlainString(),
                percentage = calculatePercentage(totalBalances.totalUSDBalance, balances.totalUSDBalance),
                balances = balances,
                marketData = MarketData(
                    circulatingSupply = crypto.circulatingSupply.toPlainString(),
                    maxSupply = crypto.maxSupply.toPlainString(),
                    currentPrice = CurrentPrice(
                        usd = crypto.lastKnownPrice.toPlainString(),
                        eur = crypto.lastKnownPriceInEUR.toPlainString(),
                        btc = crypto.lastKnownPriceInBTC.toPlainString()
                    )
                ),
                platforms = listOf(platform.name)
            )
        }

        return Optional.of(
            PageUserCryptosInsightsResponse(
                page = page,
                totalPages = userCryptosPage.totalPages,
                balances = totalBalances,
                cryptos = cryptosInsights
            )
        )
    }

    fun retrieveUserCryptosPlatformsInsights(page: Int): Optional<PageUserCryptosInsightsResponse> {
        logger.info { "Retrieving user cryptos in platforms insights for page $page" }

        // If one of the user cryptos happens to be at the end, and another of the same (i.e: bitcoin), at the start
        // using findAllByPage() will display the same crypto twice (in this example), and the idea of this insight
        // it's to display total balances and percentage for each individual crypto.
        // So I need to calculate everything from all the user cryptos.
        // Maybe create a query that returns the coingeckoCryptoId summing all balances for that crypto and
        // returning an array of the platforms for that crypto and then paginate the results
        // would be a better approach so I don't need to retrieve all.
        val userCryptos = userCryptoService.findAll()

        if (userCryptos.isEmpty()) {
            return Optional.empty()
        }

        val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
        val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
        val cryptos = cryptoService.findAllByIds(cryptosIds)
        val platformsIds = userCryptos.map { it.platformId }.toSet()
        val platforms = platformService.findAllByIds(platformsIds)
        val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)
        val userCryptosQuantityPlatforms = getUserCryptosQuantityPlatforms(userCryptos, platforms)

        val userCryptosInsights = userCryptosQuantityPlatforms.map {
            val (cryptoTotalQuantity, cryptoPlatforms) = it.value
            val crypto = cryptos.first { crypto -> crypto.id === it.key }
            val cryptoTotalBalances = getCryptoTotalBalances(crypto, cryptoTotalQuantity)

            UserCryptosInsights(
                cryptoInfo = CryptoInfo(
                    cryptoName = crypto.name,
                    coingeckoCryptoId = crypto.id,
                    symbol = crypto.ticker,
                    image = crypto.image
                ),
                quantity = cryptoTotalQuantity.toPlainString(),
                percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoTotalBalances.totalUSDBalance),
                balances = cryptoTotalBalances,
                marketData = MarketData(
                    circulatingSupply = crypto.circulatingSupply.toPlainString(),
                    maxSupply = crypto.maxSupply.toPlainString(),
                    currentPrice = CurrentPrice(
                        usd = crypto.lastKnownPrice.toPlainString(),
                        eur = crypto.lastKnownPriceInEUR.toPlainString(),
                        btc = crypto.lastKnownPriceInBTC.toPlainString()
                    )
                ),
                platforms = cryptoPlatforms
            )
        }.sortedByDescending { it.percentage }

        val startIndex = page * INT_ELEMENTS_PER_PAGE

        if (startIndex > userCryptosInsights.size) {
            return Optional.empty()
        }

        val totalPages = ceil(userCryptos.size.toDouble() / ELEMENTS_PER_PAGE).toInt()
        val endIndex = if (isLastPage(page, totalPages)) userCryptosInsights.size else startIndex + INT_ELEMENTS_PER_PAGE
        val cryptosInsights = userCryptosInsights.subList(startIndex, endIndex)

        return Optional.of(
            PageUserCryptosInsightsResponse(
                page = page,
                totalPages = totalPages,
                balances = totalBalances,
                cryptos = cryptosInsights
            )
        )
    }

    private fun getTotalBalances(cryptos: List<Crypto>, userCryptoQuantity: Map<String, BigDecimal>): BalancesResponse {
        var totalUSDBalance = BigDecimal.ZERO
        var totalBTCBalance = BigDecimal.ZERO
        var totalEURBalance = BigDecimal.ZERO

        userCryptoQuantity.forEach { (coingeckoCryptoId, quantity) ->
            val crypto = cryptos.first { it.id.equals(coingeckoCryptoId, true) }
            val lastKnownPrice = crypto.lastKnownPrice
            val lastKnownPriceInBTC = crypto.lastKnownPriceInBTC
            val lastKnownPriceInEUR = crypto.lastKnownPriceInEUR

            totalUSDBalance = totalUSDBalance.plus(lastKnownPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP))
            totalBTCBalance = totalBTCBalance.plus(lastKnownPriceInBTC.multiply(quantity)).stripTrailingZeros()
            totalEURBalance = totalEURBalance.plus(lastKnownPriceInEUR.multiply(quantity).setScale(2, RoundingMode.HALF_UP))
        }

        return BalancesResponse(
            totalUSDBalance = totalUSDBalance.toPlainString(),
            totalBTCBalance = totalBTCBalance.setScale(12, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString(),
            totalEURBalance = totalEURBalance.toPlainString()
        )
    }

    private fun getCryptoTotalBalances(crypto: Crypto, quantity: BigDecimal): BalancesResponse {
        return BalancesResponse(
            totalUSDBalance = crypto.lastKnownPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString(),
            totalBTCBalance = crypto.lastKnownPriceInBTC.multiply(quantity).setScale(12, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString(),
            totalEURBalance = crypto.lastKnownPriceInEUR.multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString()
        )
    }

    private fun calculatePercentage(totalUSDBalance: String, cryptoBalance: String) = BigDecimal(cryptoBalance)
        .multiply(BigDecimal("100"))
        .divide(BigDecimal(totalUSDBalance), 2, RoundingMode.HALF_UP)
        .toFloat()

    private fun getUserCryptoQuantity(userCryptos: List<UserCrypto>): Map<String, BigDecimal> {
        val userCryptoQuantity = HashMap<String, BigDecimal>()

        userCryptos.forEach { userCrypto ->
            if (userCryptoQuantity.containsKey(userCrypto.coingeckoCryptoId)) {
                val quantity = userCryptoQuantity[userCrypto.coingeckoCryptoId]
                userCryptoQuantity[userCrypto.coingeckoCryptoId] = quantity!!.plus(userCrypto.quantity)
            } else {
                userCryptoQuantity[userCrypto.coingeckoCryptoId] = userCrypto.quantity
            }
        }

        return userCryptoQuantity
    }

    private fun getPlatformsUserCryptos(
        userCryptos: List<UserCrypto>,
        platforms: List<Platform>
    ): Map<String, List<UserCrypto>> {
        val platformsUserCryptos = HashMap<String, List<UserCrypto>>()

        userCryptos.forEach { userCrypto ->
            val platform = platforms.first { it.id == userCrypto.platformId }

            if (platformsUserCryptos.containsKey(platform.name)) {
                val cryptos = platformsUserCryptos[platform.name]
                platformsUserCryptos[platform.name] = cryptos!!.plus(userCrypto)
            } else {
                platformsUserCryptos[platform.name] = listOf(userCrypto)
            }
        }

        return platformsUserCryptos
    }

    private fun getCryptoInsightsWithOthers(
        totalBalances: BalancesResponse,
        cryptosInsights: List<CryptoInsights>
    ): List<CryptoInsights> {
        var cryptosInsightsWithOthers: List<CryptoInsights> = ArrayList()
        val topCryptos = cryptosInsights.subList(0, 12)
        val others = cryptosInsights.subList(12, cryptosInsights.size)

        var totalUSDBalance = BigDecimal.ZERO
        var totalBTCBalance = BigDecimal.ZERO
        var totalEURBalance = BigDecimal.ZERO
        others.forEach {
            totalUSDBalance = totalUSDBalance.plus(BigDecimal(it.balances.totalUSDBalance))
            totalBTCBalance = totalBTCBalance.plus(BigDecimal(it.balances.totalBTCBalance))
            totalEURBalance = totalEURBalance.plus(BigDecimal(it.balances.totalEURBalance))
        }
        val othersTotalPercentage = calculatePercentage(totalBalances.totalUSDBalance, totalUSDBalance.toPlainString())

        val othersCryptoInsights = CryptoInsights(
            cryptoName = "Others",
            balances = BalancesResponse(
                totalUSDBalance = totalUSDBalance.toPlainString(),
                totalBTCBalance = totalBTCBalance.toPlainString(),
                totalEURBalance = totalEURBalance.toPlainString()
            ),
            percentage = othersTotalPercentage
        )

        cryptosInsightsWithOthers = cryptosInsightsWithOthers.plus(topCryptos).plus(othersCryptoInsights)

        return cryptosInsightsWithOthers
    }

    private fun getUserCryptosQuantityPlatforms(
        userCryptos: List<UserCrypto>,
        platforms: List<Platform>
    ): Map<String, Pair<BigDecimal, List<String>>> {
        val map = HashMap<String, Pair<BigDecimal, List<String>>>()

        userCryptos.forEach { userCrypto ->
            val platformName = platforms.first { it.id === userCrypto.platformId }.name

            if (map.containsKey(userCrypto.coingeckoCryptoId)) {
                val crypto = map[userCrypto.coingeckoCryptoId]
                val actualQuantity = crypto!!.first
                val actualPlatforms = crypto.second

                val newQuantity = actualQuantity.plus(userCrypto.quantity)
                val newPlatforms = actualPlatforms.plus(platformName)

                map[userCrypto.coingeckoCryptoId] = Pair(newQuantity, newPlatforms)
            } else {
                map[userCrypto.coingeckoCryptoId] = Pair(userCrypto.quantity, listOf(platformName))
            }
        }

        return map
    }

    private fun isLastPage(page: Int, totalPages: Int) = page + 1 >= totalPages
}