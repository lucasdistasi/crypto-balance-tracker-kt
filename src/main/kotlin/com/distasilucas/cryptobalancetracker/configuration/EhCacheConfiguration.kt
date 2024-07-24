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
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.PriceTarget
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
    val coingeckoCryptoList = CastUtils.cast<Class<List<CoingeckoCrypto>>>(MutableList::class.java)
    val userCryptoList = CastUtils.cast<Class<List<UserCrypto>>>(MutableList::class.java)
    val stringCollection = CastUtils.cast<Class<Collection<String>>>(Collection::class.java)
    val platformList = CastUtils.cast<Class<List<Platform>>>(MutableList::class.java)
    val cryptoList = CastUtils.cast<Class<List<Crypto>>>(MutableList::class.java)

    val coingeckoCryptosCache = getCacheConfig(SimpleKey::class.java, coingeckoCryptoList, Duration.ofDays(3))
    val coingeckoCryptoInfoCache = getCacheConfig(String::class.java, CoingeckoCryptoInfo::class.java, Duration.ofMinutes(10))
    val userCryptosCache = getCacheConfig(SimpleKey::class.java, userCryptoList)
    val userCryptosPlatformIdCache = getCacheConfig(String::class.java, userCryptoList)
    val userCryptosCoingeckoCryptoIdCache = getCacheConfig(String::class.java, userCryptoList)
    val userCryptoIdCache = getCacheConfig(String::class.java, UserCrypto::class.java)
    val userCryptoResponseIdCache = getCacheConfig(String::class.java, UserCryptoResponse::class.java)
    val userCryptosResponsePageCache = getCacheConfig(Int::class.javaObjectType, PageUserCryptoResponse::class.java)
    val platformsIdsCache = getCacheConfig(stringCollection, platformList)
    val cryptoCoingeckoCryptoIdCache = getCacheConfig(String::class.java, Crypto::class.java, Duration.ofMinutes(2))
    val cryptosIdsCache = getCacheConfig(stringCollection, cryptoList, Duration.ofMinutes(2))
    val allPlatformsCache = getCacheConfig(SimpleKey::class.java, platformList, Duration.ofDays(10))
    val platformCache = getCacheConfig(String::class.java, Platform::class.java, Duration.ofDays(10))
    val priceTargetCache = getCacheConfig(String::class.java, PriceTarget::class.java)
    val priceTargetResponseCache = getCacheConfig(String::class.java, PriceTargetResponse::class.java)
    val pagePriceTargetResponseCache = getCacheConfig(Int::class.javaObjectType, PagePriceTargetResponse::class.java)
    val goalResponseCache = getCacheConfig(String::class.java, GoalResponse::class.java)
    val pageGoalsResponseCache = getCacheConfig(Int::class.javaObjectType, PageGoalResponse::class.java)
    val totalBalancesCache = getCacheConfig(SimpleKey::class.java, BalancesResponse::class.java, Duration.ofMinutes(5))
    val datesBalancesCache = getCacheConfig(DateRange::class.java, DatesBalanceResponse::class.java, Duration.ofMinutes(5))
    val platformInsightsCache = getCacheConfig(String::class.java, PlatformInsightsResponse::class.java, Duration.ofMinutes(5))
    val cryptoInsightsCache = getCacheConfig(String::class.java, CryptoInsightResponse::class.java, Duration.ofMinutes(5))
    val platformsBalancesInsightsCache = getCacheConfig(SimpleKey::class.java, PlatformsBalancesInsightsResponse::class.java, Duration.ofMinutes(5))
    val cryptosBalancesInsightsCache = getCacheConfig(SimpleKey::class.java, CryptosBalancesInsightsResponse::class.java, Duration.ofMinutes(5))

    cacheManager.createCache(COINGECKO_CRYPTOS_CACHE, coingeckoCryptosCache)
    cacheManager.createCache(CRYPTO_INFO_CACHE, coingeckoCryptoInfoCache)
    cacheManager.createCache(USER_CRYPTOS_CACHE, userCryptosCache)
    cacheManager.createCache(USER_CRYPTOS_PLATFORM_ID_CACHE, userCryptosPlatformIdCache)
    cacheManager.createCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, userCryptosCoingeckoCryptoIdCache)
    cacheManager.createCache(USER_CRYPTO_ID_CACHE, userCryptoIdCache)
    cacheManager.createCache(USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE, userCryptoResponseIdCache)
    cacheManager.createCache(USER_CRYPTOS_RESPONSE_PAGE_CACHE, userCryptosResponsePageCache)
    cacheManager.createCache(PLATFORMS_PLATFORMS_IDS_CACHE, platformsIdsCache)
    cacheManager.createCache(CRYPTO_COINGECKO_CRYPTO_ID_CACHE, cryptoCoingeckoCryptoIdCache)
    cacheManager.createCache(CRYPTOS_CRYPTOS_IDS_CACHE, cryptosIdsCache)
    cacheManager.createCache(ALL_PLATFORMS_CACHE, allPlatformsCache)
    cacheManager.createCache(PLATFORM_PLATFORM_ID_CACHE, platformCache)
    cacheManager.createCache(PRICE_TARGET_ID_CACHE, priceTargetCache)
    cacheManager.createCache(PRICE_TARGET_RESPONSE_ID_CACHE, priceTargetResponseCache)
    cacheManager.createCache(PRICE_TARGET_RESPONSE_PAGE_CACHE, pagePriceTargetResponseCache)
    cacheManager.createCache(GOAL_RESPONSE_GOAL_ID_CACHE, goalResponseCache)
    cacheManager.createCache(PAGE_GOALS_RESPONSE_PAGE_CACHE, pageGoalsResponseCache)
    cacheManager.createCache(TOTAL_BALANCES_CACHE, totalBalancesCache)
    cacheManager.createCache(DATES_BALANCES_CACHE, datesBalancesCache)
    cacheManager.createCache(PLATFORM_INSIGHTS_CACHE, platformInsightsCache)
    cacheManager.createCache(CRYPTO_INSIGHTS_CACHE, cryptoInsightsCache)
    cacheManager.createCache(PLATFORMS_BALANCES_INSIGHTS_CACHE, platformsBalancesInsightsCache)
    cacheManager.createCache(CRYPTOS_BALANCES_INSIGHTS_CACHE, cryptosBalancesInsightsCache)

    return cacheManager
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
