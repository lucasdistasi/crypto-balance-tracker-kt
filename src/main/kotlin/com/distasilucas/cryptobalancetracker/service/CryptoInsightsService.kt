package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_BALANCES_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.SortBy
import com.distasilucas.cryptobalancetracker.model.SortParams
import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import kotlin.math.ceil

@Service
data class CryptoInsightsService(
  @Value("\${crypto.insights.max-single-items-count}")
  private val maxSingleItemsCount: Int,
  @Value("\${crypto.insights.elements-page}")
  private val cryptosPerPage: Int,
  private val platformService: PlatformService,
  private val userCryptoService: UserCryptoService,
  private val cryptoService: CryptoService,
  private val insightsService: InsightsService
) {

  private val logger = KotlinLogging.logger { }

  @Cacheable(cacheNames = [CRYPTO_INSIGHTS_CACHE], key = "#coingeckoCryptoId")
  fun retrieveCryptoInsights(coingeckoCryptoId: String): Optional<CryptoInsightResponse> {
    logger.info { "Retrieving insights for crypto with coingeckoCryptoId $coingeckoCryptoId" }

    val userCryptos = userCryptoService.findAllByCoingeckoCryptoId(coingeckoCryptoId)

    if (userCryptos.isEmpty()) return Optional.empty()

    val platformsIds = userCryptos.map { it.platformId }
    val platforms = platformService.findAllByIds(platformsIds)
    val crypto = cryptoService.retrieveCryptoInfoById(coingeckoCryptoId)
    val cryptoInfo = crypto.toCryptoInfo(
      price = Price(crypto.lastKnownPrice, crypto.lastKnownPriceInEUR),
      priceChange = PriceChange(crypto.changePercentageIn24h, crypto.changePercentageIn7d, crypto.changePercentageIn30d)
    )

    val platformUserCryptoQuantity = userCryptos.associateBy({ it.platformId }, { it.quantity })
    val totalCryptoQuantity = userCryptos.sumOf { it.quantity }
    val totalBalances =
      insightsService.getTotalBalances(listOf(crypto), mapOf(coingeckoCryptoId to totalCryptoQuantity))

    val platformInsights = platforms.map { platform ->
      val quantity = platformUserCryptoQuantity[platform.id]
      val cryptoTotalBalances = insightsService.getCryptoTotalBalances(crypto, quantity!!)

      PlatformInsight(
        quantity = quantity.toPlainString(),
        balances = cryptoTotalBalances,
        percentage = insightsService.calculatePercentage(totalBalances.usd(), cryptoTotalBalances.usd()),
        platformName = platform.name
      )
    }.sortedByDescending { it.percentage }

    return Optional.of(CryptoInsightResponse(cryptoInfo, totalBalances, platformInsights))
  }

  @Cacheable(cacheNames = [CRYPTOS_BALANCES_INSIGHTS_CACHE])
  fun retrieveCryptosBalancesInsights(): List<BalancesChartResponse> {
    logger.info { "Retrieving all cryptos balances insights" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) return emptyList()

    val userCryptoQuantity = insightsService.getUserCryptoQuantity(userCryptos)
    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val totalBalances = insightsService.getTotalBalances(cryptos, userCryptoQuantity)
    val cryptosMap = cryptos.associateBy { it.id }

    val cryptosInsights = userCryptoQuantity.map { (coingeckoCryptoId, quantity) ->
      val crypto = cryptosMap[coingeckoCryptoId]
      val cryptoBalances = insightsService.getCryptoTotalBalances(crypto!!, quantity)
      val percentage = insightsService.calculatePercentage(totalBalances.usd(), cryptoBalances.usd())

      BalancesChartResponse(crypto.name, cryptoBalances.usd(), percentage)
    }.sortedByDescending { it.percentage }

    return if (cryptosInsights.size > maxSingleItemsCount) {
      getCryptoInsightsWithOthers(totalBalances, cryptosInsights)
    } else {
      cryptosInsights
    }
  }

  fun retrieveUserCryptosInsights(
    page: Int,
    sortParams: SortParams = SortParams(SortBy.PERCENTAGE, SortType.DESC)
  ): PageUserCryptosInsightsResponse {
    logger.info { "Retrieving user cryptos insights for page $page" }

    // If one of the user cryptos happens to be at the end, and another of the same (i.e: bitcoin), at the start
    // using findAllByPage() will display the same crypto twice (in this example), and the idea of this insight
    // it's to display total balances and percentage for each individual crypto.
    // So I need to calculate everything from all the user cryptos.
    // Maybe create a query that returns the coingeckoCryptoId summing all balances for that crypto and
    // returning an array of the platforms for that crypto and then paginate the results
    // would be a better approach so I don't need to retrieve all.
    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      return PageUserCryptosInsightsResponse.EMPTY
    }

    val userCryptoQuantity = insightsService.getUserCryptoQuantity(userCryptos)
    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val totalBalances = insightsService.getTotalBalances(cryptos, userCryptoQuantity)
    val userCryptosQuantityPlatforms = mapUserCryptosQuantity(userCryptos)
    val cryptosMap = cryptos.associateBy { it.id }

    val userCryptoInsights = userCryptosQuantityPlatforms.map { (coingeckoCryptoId, cryptoQuantity) ->
      val crypto = cryptosMap[coingeckoCryptoId] ?: throw ApiException("User crypto $coingeckoCryptoId not found")
      val cryptoTotalBalances = insightsService.getCryptoTotalBalances(crypto, cryptoQuantity)
      val price = Price(crypto.lastKnownPrice, crypto.lastKnownPriceInEUR, crypto.lastKnownPriceInBTC)
      val priceChange =
        PriceChange(crypto.changePercentageIn24h, crypto.changePercentageIn7d, crypto.changePercentageIn30d)
      val cryptoInfo = crypto.toCryptoInfo(price, priceChange)

      UserCryptoInsights(
        cryptoInfo = cryptoInfo,
        quantity = cryptoQuantity.toPlainString(),
        percentage = insightsService.calculatePercentage(totalBalances.usd(), cryptoTotalBalances.usd()),
        balances = cryptoTotalBalances
      )
    }.sortedWith(sortParams.cryptosInsightsResponseComparator())

    val startIndex = page * cryptosPerPage

    if (startIndex > userCryptoInsights.size) {
      return PageUserCryptosInsightsResponse.EMPTY
    }

    val totalPages = ceil(userCryptoInsights.size.toDouble() / cryptosPerPage).toInt()
    val endIndex = if (isLastPage(page, totalPages)) userCryptoInsights.size else startIndex + cryptosPerPage
    val cryptosInsights = userCryptoInsights.subList(startIndex, endIndex)

    return PageUserCryptosInsightsResponse(page, totalPages, totalBalances, cryptosInsights)
  }

  private fun getCryptoInsightsWithOthers(
    totalBalances: Balances,
    balances: List<BalancesChartResponse>
  ): List<BalancesChartResponse> {
    val topCryptos = balances.take(maxSingleItemsCount)
    val others = balances.drop(maxSingleItemsCount)
    val othersBalances = others.sumOf { BigDecimal(it.balance) }.toPlainString()
    val othersTotalPercentage = insightsService.calculatePercentage(totalBalances.usd(), othersBalances)
    val othersCryptoInsights = BalancesChartResponse("Others", othersBalances, othersTotalPercentage)

    return topCryptos + othersCryptoInsights
  }

  private fun mapUserCryptosQuantity(userCryptos: List<UserCrypto>) =
    userCryptos.groupBy(UserCrypto::coingeckoCryptoId).mapValues { it.value.sumOf(UserCrypto::quantity) }

  private fun isLastPage(page: Int, totalPages: Int) = page + 1 >= totalPages
}
