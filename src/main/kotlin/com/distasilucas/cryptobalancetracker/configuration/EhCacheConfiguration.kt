package com.distasilucas.cryptobalancetracker.configuration

import com.distasilucas.cryptobalancetracker.constants.ALL_PLATFORMS_CACHE
import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_BALANCES_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_CRYPTOS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_INFO_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.DATES_BALANCES_CACHE
import com.distasilucas.cryptobalancetracker.constants.GOAL_RESPONSE_GOAL_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.LATEST_TRANSACTIONS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PAGE_GOALS_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_BALANCES_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_PLATFORMS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_RESPONSE_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.TOTAL_BALANCES_CACHE
import com.distasilucas.cryptobalancetracker.constants.TRANSACTIONS_INFO_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.TransactionsInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import org.ehcache.jsr107.Eh107Configuration
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.util.CastUtils
import java.time.Duration
import javax.cache.CacheManager
import javax.cache.Caching
import javax.cache.configuration.Configuration as CacheConfiguration

@Configuration
class EhCacheConfiguration {

  @Bean
  fun ehcacheManager(): CacheManager {
    val cacheManager = Caching.getCachingProvider().cacheManager

    getAllCaches().forEach { (name, config) -> cacheManager.createCache(name, config as CacheConfiguration<Any,Any>) }

    return cacheManager
  }

  private fun getAllCaches(): Map<String, CacheConfiguration<*, *>> {
    val coingeckoCryptoList = CastUtils.cast<Class<List<CoingeckoCrypto>>>(MutableList::class.java)
    val userCryptoList = CastUtils.cast<Class<List<UserCrypto>>>(MutableList::class.java)
    val stringCollection = CastUtils.cast<Class<Collection<String>>>(Collection::class.java)
    val platformList = CastUtils.cast<Class<List<Platform>>>(MutableList::class.java)
    val cryptoList = CastUtils.cast<Class<List<Crypto>>>(MutableList::class.java)
    val transactionsPage = CastUtils.cast<Class<Page<Transaction>>>(Page::class.java)

    return mapOf(
      COINGECKO_CRYPTOS_CACHE to getCacheConfig(SimpleKey::class.java, coingeckoCryptoList, Duration.ofDays(3)),
      CRYPTO_INFO_CACHE to getCacheConfig(String::class.java, CoingeckoCryptoInfo::class.java, Duration.ofMinutes(10)),
      USER_CRYPTOS_CACHE to getCacheConfig(SimpleKey::class.java, userCryptoList),
      USER_CRYPTOS_PLATFORM_ID_CACHE to getCacheConfig(String::class.java, userCryptoList),
      USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE to getCacheConfig(String::class.java, userCryptoList),
      USER_CRYPTO_ID_CACHE to getCacheConfig(String::class.java, UserCrypto::class.java),
      USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE to getCacheConfig(String::class.java, UserCryptoResponse::class.java),
      USER_CRYPTOS_RESPONSE_PAGE_CACHE to getCacheConfig(Int::class.javaObjectType, PageUserCryptoResponse::class.java),
      PLATFORMS_PLATFORMS_IDS_CACHE to getCacheConfig(stringCollection, platformList),
      CRYPTO_COINGECKO_CRYPTO_ID_CACHE to getCacheConfig(String::class.java, Crypto::class.java, Duration.ofMinutes(2)),
      CRYPTOS_CRYPTOS_IDS_CACHE to getCacheConfig(stringCollection, cryptoList, Duration.ofMinutes(2)),
      ALL_PLATFORMS_CACHE to getCacheConfig(SimpleKey::class.java, platformList, Duration.ofDays(10)),
      PLATFORM_PLATFORM_ID_CACHE to getCacheConfig(String::class.java, Platform::class.java, Duration.ofDays(10)),
      PRICE_TARGET_ID_CACHE to getCacheConfig(String::class.java, PriceTarget::class.java),
      PRICE_TARGET_RESPONSE_ID_CACHE to getCacheConfig(String::class.java, PriceTargetResponse::class.java),
      PRICE_TARGET_RESPONSE_PAGE_CACHE to getCacheConfig(Int::class.javaObjectType, PagePriceTargetResponse::class.java),
      GOAL_RESPONSE_GOAL_ID_CACHE to getCacheConfig(String::class.java, GoalResponse::class.java),
      PAGE_GOALS_RESPONSE_PAGE_CACHE to getCacheConfig(Int::class.javaObjectType, PageGoalResponse::class.java),
      TOTAL_BALANCES_CACHE to getCacheConfig(SimpleKey::class.java, BalancesResponse::class.java, Duration.ofMinutes(5)),
      DATES_BALANCES_CACHE to getCacheConfig(DateRange::class.java, DatesBalanceResponse::class.java, Duration.ofMinutes(5)),
      PLATFORM_INSIGHTS_CACHE to getCacheConfig(String::class.java, PlatformInsightsResponse::class.java, Duration.ofMinutes(5)),
      CRYPTO_INSIGHTS_CACHE to getCacheConfig(String::class.java, CryptoInsightResponse::class.java, Duration.ofMinutes(5)),
      PLATFORMS_BALANCES_INSIGHTS_CACHE to getCacheConfig(SimpleKey::class.java, PlatformsBalancesInsightsResponse::class.java, Duration.ofMinutes(5)),
      CRYPTOS_BALANCES_INSIGHTS_CACHE to getCacheConfig(SimpleKey::class.java, CryptosBalancesInsightsResponse::class.java, Duration.ofMinutes(5)),
      LATEST_TRANSACTIONS_CACHE to getCacheConfig(Int::class.javaObjectType, transactionsPage),
      TRANSACTIONS_INFO_CACHE to getCacheConfig(String::class.java, TransactionsInfo::class.java),
    )
  }

  private fun <K, V> getCacheConfig(
    keyType: Class<K>,
    valueType: Class<V>,
    duration: Duration = Duration.ofMinutes(60),
    resourcePools: ResourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(1, MemoryUnit.MB)
  ): CacheConfiguration<K, V> {
    val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(duration)

    val cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
      keyType,
      valueType,
      resourcePools
    ).withExpiry(expirationPolicyBuilder).build()

    return Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration)
  }
}
