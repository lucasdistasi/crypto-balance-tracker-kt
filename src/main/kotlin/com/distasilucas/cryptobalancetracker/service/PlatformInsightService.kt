package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_BALANCES_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.CryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
data class PlatformInsightService(
  private val platformService: PlatformService,
  private val userCryptoService: UserCryptoService,
  private val cryptoService: CryptoService,
  private val insightsService: InsightsService
) {

  private val logger = KotlinLogging.logger { }

  @Cacheable(cacheNames = [PLATFORM_INSIGHTS_CACHE], key = "#platformId")
  fun retrievePlatformInsights(platformId: String): PlatformInsightsResponse {
    logger.info { "Retrieving insights for platform with id $platformId" }

    val userCryptosInPlatform = userCryptoService.findAllByPlatformId(platformId)

    if (userCryptosInPlatform.isEmpty()) {
      throw ApiException(HttpStatus.NO_CONTENT, "There is no user cryptos in platform")
    }

    val platform = platformService.retrievePlatformById(platformId)
    val cryptosIds = userCryptosInPlatform.map { it.coingeckoCryptoId }
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val userCryptosQuantity = insightsService.getUserCryptoQuantity(userCryptosInPlatform)
    val totalBalances = insightsService.getTotalBalances(cryptos, userCryptosQuantity)
    val cryptosMap = cryptos.associateBy { it.id }

    val cryptosInsights = userCryptosInPlatform.map { userCrypto ->
      val crypto = cryptosMap[userCrypto.coingeckoCryptoId] ?: throw ApiException("User crypto not found")
      val quantity = userCryptosQuantity[userCrypto.coingeckoCryptoId]
      val cryptoTotalBalances = insightsService.getCryptoTotalBalances(crypto, quantity!!)

      CryptoInsights(
        id = userCrypto.id,
        cryptoInfo = crypto.toCryptoInfo(),
        quantity = quantity.toPlainString(),
        percentage = insightsService.calculatePercentage(totalBalances.usd(), cryptoTotalBalances.usd()),
        balances = cryptoTotalBalances
      )
    }.sortedByDescending { it.percentage }

    return PlatformInsightsResponse(platform.name, totalBalances, cryptosInsights)
  }

  @Cacheable(cacheNames = [PLATFORMS_BALANCES_INSIGHTS_CACHE])
  fun retrievePlatformsBalancesInsights(): List<BalancesChartResponse> {
    logger.info { "Retrieving all platforms balances insights" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) return emptyList()

    val platformsIds = userCryptos.map { it.platformId }.toSet()
    val platforms = platformService.findAllByIds(platformsIds)
    val userCryptoQuantity = insightsService.getUserCryptoQuantity(userCryptos)
    val platformsUserCryptos = getPlatformsUserCryptos(platforms, userCryptos)
    val cryptosIds = platformsUserCryptos.values.flatMap { it.map { it.coingeckoCryptoId } }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val totalBalances = insightsService.getTotalBalances(cryptos, userCryptoQuantity)
    val cryptosMap = cryptos.associateBy { it.id }

    return platformsUserCryptos.map { (platformName, userCryptos) ->
      var totalUSDBalance = BigDecimal.ZERO

      userCryptos.forEach { userCrypto ->
        val crypto = cryptosMap[userCrypto.coingeckoCryptoId] ?: throw ApiException("User crypto ${userCrypto.id} not found")
        val balance = insightsService.getCryptoTotalBalances(crypto, userCrypto.quantity)
        totalUSDBalance = totalUSDBalance.plus(BigDecimal(balance.usd()))
      }

      val percentage = insightsService.calculatePercentage(totalBalances.usd(), totalUSDBalance.toPlainString())

      BalancesChartResponse(platformName, totalUSDBalance, percentage)
    }.sortedByDescending { it.percentage }
  }

  // Map <platform name, List<user cryptos>>
  private fun getPlatformsUserCryptos(
    platforms: List<Platform>,
    userCryptos: List<UserCrypto>
  ): Map<String, List<UserCrypto>> {
    val platformsMap = platforms.associateBy { it.id }

    return userCryptos.groupBy { crypto ->
      platformsMap[crypto.platformId]?.name ?: throw ApiException("Platform with id ${crypto.platformId} not found")
    }
  }
}
