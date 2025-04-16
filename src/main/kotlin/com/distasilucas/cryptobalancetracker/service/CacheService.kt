package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.ALL_PLATFORMS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_BALANCES_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_CRYPTOS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.DATES_BALANCES_CACHE
import com.distasilucas.cryptobalancetracker.constants.GOAL_RESPONSE_GOAL_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PAGE_GOALS_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_BALANCES_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_PLATFORMS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_RESPONSE_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.TOTAL_BALANCES_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class CacheService(private val cacheManager: CacheManager) {

  private val logger = KotlinLogging.logger { }

  fun invalidate(firstCache: CacheType, vararg caches: CacheType) {
    invalidate(firstCache)
    caches.forEach { invalidate(it) }
  }

  private fun invalidate(cache: CacheType) {
    when (cache) {
      CacheType.USER_CRYPTOS_CACHES -> invalidateUserCryptosCaches()
      CacheType.CRYPTOS_CACHES -> invalidateCryptosCache()
      CacheType.PLATFORMS_CACHES -> invalidatePlatformsCaches()
      CacheType.GOALS_CACHES -> invalidateGoalsCaches()
      CacheType.PRICE_TARGETS_CACHES -> invalidatePriceTargetCaches()
      CacheType.INSIGHTS_CACHES -> invalidateInsightsCache()
    }
  }

  private fun invalidateUserCryptosCaches() {
    logger.info { "Invalidating user cryptos caches" }

    cacheManager.getCache(USER_CRYPTOS_CACHE)!!.invalidate()
    cacheManager.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE)!!.invalidate()
    cacheManager.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE)!!.invalidate()
    cacheManager.getCache(USER_CRYPTO_ID_CACHE)!!.invalidate()
    cacheManager.getCache(USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE)!!.invalidate()
  }

  private fun invalidatePlatformsCaches() {
    logger.info { "Invalidating platforms caches" }

    cacheManager.getCache(PLATFORMS_PLATFORMS_IDS_CACHE)!!.invalidate()
    cacheManager.getCache(ALL_PLATFORMS_CACHE)!!.invalidate()
    cacheManager.getCache(PLATFORM_PLATFORM_ID_CACHE)!!.invalidate()
  }

  private fun invalidateCryptosCache() {
    logger.info { "Invalidating cryptos cache" }

    cacheManager.getCache(CRYPTOS_CRYPTOS_IDS_CACHE)!!.invalidate()
  }

  private fun invalidateGoalsCaches() {
    logger.info { "Invalidating goals caches" }

    cacheManager.getCache(GOAL_RESPONSE_GOAL_ID_CACHE)!!.invalidate()
    cacheManager.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE)!!.invalidate()
  }

  private fun invalidatePriceTargetCaches() {
    logger.info { "Invalidating price target caches" }

    cacheManager.getCache(PRICE_TARGET_ID_CACHE)!!.invalidate()
    cacheManager.getCache(PRICE_TARGET_RESPONSE_ID_CACHE)!!.invalidate()
    cacheManager.getCache(PRICE_TARGET_RESPONSE_PAGE_CACHE)!!.invalidate()
  }

  private fun invalidateInsightsCache() {
    logger.info { "Invalidating insights caches" }

    cacheManager.getCache(TOTAL_BALANCES_CACHE)!!.invalidate()
    cacheManager.getCache(DATES_BALANCES_CACHE)!!.invalidate()
    cacheManager.getCache(PLATFORM_INSIGHTS_CACHE)!!.invalidate()
    cacheManager.getCache(CRYPTO_INSIGHTS_CACHE)!!.invalidate()
    cacheManager.getCache(PLATFORMS_BALANCES_INSIGHTS_CACHE)!!.invalidate()
    cacheManager.getCache(CRYPTOS_BALANCES_INSIGHTS_CACHE)!!.invalidate()
  }
}

enum class CacheType {
  USER_CRYPTOS_CACHES,
  CRYPTOS_CACHES,
  PLATFORMS_CACHES,
  GOALS_CACHES,
  PRICE_TARGETS_CACHES,
  INSIGHTS_CACHES
}
