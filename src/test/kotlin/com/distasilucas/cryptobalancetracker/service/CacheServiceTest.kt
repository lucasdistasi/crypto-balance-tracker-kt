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
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import getCryptoEntity
import getGoalResponse
import getUserCrypto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.interceptor.SimpleKey
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class CacheServiceTest {

  private val cacheManagerMock = mockk<CacheManager>()

  private val cacheService = CacheService(cacheManagerMock)

  @Test
  fun `should invalidate user cryptos caches if they exist`() {
    val userCrypto = getUserCrypto()
    val userCryptoResponse = userCrypto.toUserCryptoResponse("Bitcoin", "BINANCE")

    val userCryptosCacheMap = mapOf(SimpleKey::class.java to listOf(userCrypto))
    val userCryptosPlatformIdMap = mapOf("123e4567-e89b-12d3-a456-426614174111" to listOf(userCrypto))
    val userCryptosCoingeckoCryptoIdMap = mapOf("bitcoin" to listOf(userCrypto))
    val userCryptoIdMap = mapOf("bc7a8ee5-13f9-4405-a7fb-887458c21bed" to listOf(userCrypto))
    val userCryptoResponseUserCryptoIdMap = mapOf("bc7a8ee5-13f9-4405-a7fb-887458c21bed" to listOf(userCryptoResponse))

    val userCryptosCache = getMapCache(USER_CRYPTOS_CACHE, userCryptosCacheMap)
    val userCryptosPlatformIdCache = getMapCache(USER_CRYPTOS_PLATFORM_ID_CACHE, userCryptosPlatformIdMap)
    val userCryptosCoingeckoCryptoIdCache = getMapCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, userCryptosCoingeckoCryptoIdMap)
    val userCryptoIdCache = getMapCache(USER_CRYPTO_ID_CACHE, userCryptoIdMap)
    val userCryptoResponseUserCryptoIdCache = getMapCache(USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE, userCryptoResponseUserCryptoIdMap)

    every { cacheManagerMock.getCache(USER_CRYPTOS_CACHE) } returns userCryptosCache
    every { cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE) } returns userCryptosPlatformIdCache
    every { cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE) } returns userCryptosCoingeckoCryptoIdCache
    every { cacheManagerMock.getCache(USER_CRYPTO_ID_CACHE) } returns userCryptoIdCache
    every { cacheManagerMock.getCache(USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE) } returns userCryptoResponseUserCryptoIdCache

    cacheService.invalidate(CacheType.USER_CRYPTOS_CACHES)

    assertTrue(userCryptosCache.nativeCache.isEmpty())
    assertTrue(userCryptosPlatformIdCache.nativeCache.isEmpty())
    assertTrue(userCryptosCoingeckoCryptoIdCache.nativeCache.isEmpty())
    assertTrue(userCryptoIdCache.nativeCache.isEmpty())
    assertTrue(userCryptoResponseUserCryptoIdCache.nativeCache.isEmpty())

    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTO_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE) }
  }

  @Test
  fun `should invalidate cryptos cache if it exists`() {
    val crypto = getCryptoEntity()
    val map = mapOf(listOf("bitcoin") to listOf(crypto))
    val cache = getMapCache(CRYPTOS_CRYPTOS_IDS_CACHE, map)

    every { cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE) } returns cache

    cacheService.invalidate(CacheType.CRYPTOS_CACHES)

    val store = cache.nativeCache

    assertTrue(store.isEmpty())

    verify(exactly = 1) { cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE) }
  }

  @Test
  fun `should invalidate platforms cache if it exists`() {
    val platform = Platform(
      id = "123e4567-e89b-12d3-a456-426614174000",
      name = "BINANCE"
    )
    val platformsIdsMap = mapOf(listOf("123e4567-e89b-12d3-a456-426614174000") to listOf(platform))
    val allPlatformsMap = mapOf(SimpleKey::class.java to listOf(platform))
    val platformIdMap = mapOf("123e4567-e89b-12d3-a456-426614174000" to platform)

    val platformsIdsCache = getMapCache(PLATFORMS_PLATFORMS_IDS_CACHE, platformsIdsMap)
    val allPlatformsCache = getMapCache(ALL_PLATFORMS_CACHE, allPlatformsMap)
    val platformIdCache = getMapCache(PLATFORM_PLATFORM_ID_CACHE, platformIdMap)

    every { cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE) } returns platformsIdsCache
    every { cacheManagerMock.getCache(ALL_PLATFORMS_CACHE) } returns allPlatformsCache
    every { cacheManagerMock.getCache(PLATFORM_PLATFORM_ID_CACHE) } returns platformIdCache

    cacheService.invalidate(CacheType.PLATFORMS_CACHES)

    val platformsIdsStore = platformsIdsCache.nativeCache
    val allPlatformsStore = allPlatformsCache.nativeCache
    val platformIdStore = platformIdCache.nativeCache

    assertTrue(platformsIdsStore.isEmpty())
    assertTrue(allPlatformsStore.isEmpty())
    assertTrue(platformIdStore.isEmpty())

    verify(exactly = 1) { cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(ALL_PLATFORMS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PLATFORM_PLATFORM_ID_CACHE) }
  }

  @Test
  fun `should invalidate goals cache if it exists`() {
    val goalResponseGoalIdMap = mapOf("123e4567-e89b-12d3-a456-426614174111" to getGoalResponse())
    val pageGoalsResponsePageMap = mapOf(0 to PageGoalResponse(1, 1, listOf(getGoalResponse())))

    val goalResponseGoalIdCache = getMapCache(GOAL_RESPONSE_GOAL_ID_CACHE, goalResponseGoalIdMap)
    val pageGoalsResponsePageCache = getMapCache(PAGE_GOALS_RESPONSE_PAGE_CACHE, pageGoalsResponsePageMap)

    every { cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE) } returns goalResponseGoalIdCache
    every { cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE) } returns pageGoalsResponsePageCache

    cacheService.invalidate(CacheType.GOALS_CACHES)

    val goalResponseGoalIdStore = goalResponseGoalIdCache.nativeCache
    val pageGoalsResponsePageStore = pageGoalsResponsePageCache.nativeCache

    assertTrue(goalResponseGoalIdStore.isEmpty())
    assertTrue(pageGoalsResponsePageStore.isEmpty())

    verify(exactly = 1) { cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE) }
  }

  @Test
  fun `should invalidate price targets cache if it exists`() {
    val priceTarget = PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", BigDecimal("120000"))
    val priceTargetResponse = PriceTargetResponse(
      "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
      "Bitcoin",
      "58000",
      "120000",
      50F
    )
    val priceTargetIdMap = mapOf("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08" to priceTarget)
    val priceTargetResponseIdMap = mapOf("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08" to priceTargetResponse)
    val priceTargetResponsePageMap = mapOf(0 to PagePriceTargetResponse(0, 1, listOf(priceTargetResponse)))

    val priceTargetIdCache = getMapCache(PRICE_TARGET_ID_CACHE, priceTargetIdMap)
    val priceTargetResponseIdCache = getMapCache(PRICE_TARGET_RESPONSE_ID_CACHE, priceTargetResponseIdMap)
    val priceTargetResponsePageCache = getMapCache(PRICE_TARGET_RESPONSE_PAGE_CACHE, priceTargetResponsePageMap)

    every { cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE) } returns priceTargetIdCache
    every { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_ID_CACHE) } returns priceTargetResponseIdCache
    every { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_PAGE_CACHE) } returns priceTargetResponsePageCache

    cacheService.invalidate(CacheType.PRICE_TARGETS_CACHES)

    val priceTargetIdStore = priceTargetIdCache.nativeCache
    val priceTargetResponseIdStore = priceTargetResponseIdCache.nativeCache
    val priceTargetResponsePageStore = priceTargetResponsePageCache.nativeCache

    assertTrue(priceTargetIdStore.isEmpty())
    assertTrue(priceTargetResponseIdStore.isEmpty())
    assertTrue(priceTargetResponsePageStore.isEmpty())

    verify(exactly = 1) { cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_PAGE_CACHE) }
  }

  @Test
  fun `should invalidate insights caches if they exist`() {
    val totalBalancesMap = mapOf(SimpleKey::class.java to BalancesResponse("1000", "927.30", "0.015384615"))
    val datesBalancesMap = mapOf(DateRange::class.java to getDateBalanceResponse())
    val platformInsightsMap = mapOf(String::class.java to getPlatformInsightsResponse())
    val cryptoInsightsMap = mapOf(String::class.java to getCryptoInsightResponse())
    val platformsBalancesInsightsMap = mapOf(SimpleKey::class.java to getPlatformsBalancesInsightsResponse())
    val cryptosBalancesInsightsMap = mapOf(SimpleKey::class.java to getCryptosBalancesInsightsResponse())

    val totalBalancesCache = getMapCache(TOTAL_BALANCES_CACHE, totalBalancesMap)
    val datesBalancesCache = getMapCache(DATES_BALANCES_CACHE, datesBalancesMap)
    val platformInsightsCache = getMapCache(PLATFORM_INSIGHTS_CACHE, platformInsightsMap)
    val cryptoInsightsCache = getMapCache(CRYPTO_INSIGHTS_CACHE, cryptoInsightsMap)
    val platformsBalancesInsightsCache = getMapCache(PLATFORMS_BALANCES_INSIGHTS_CACHE, platformsBalancesInsightsMap)
    val cryptosBalancesInsightsCache = getMapCache(CRYPTOS_BALANCES_INSIGHTS_CACHE, cryptosBalancesInsightsMap)

    every { cacheManagerMock.getCache(TOTAL_BALANCES_CACHE) } returns totalBalancesCache
    every { cacheManagerMock.getCache(DATES_BALANCES_CACHE) } returns datesBalancesCache
    every { cacheManagerMock.getCache(PLATFORM_INSIGHTS_CACHE) } returns platformInsightsCache
    every { cacheManagerMock.getCache(CRYPTO_INSIGHTS_CACHE) } returns cryptoInsightsCache
    every { cacheManagerMock.getCache(PLATFORMS_BALANCES_INSIGHTS_CACHE) } returns platformsBalancesInsightsCache
    every { cacheManagerMock.getCache(CRYPTOS_BALANCES_INSIGHTS_CACHE) } returns cryptosBalancesInsightsCache

    cacheService.invalidate(CacheType.INSIGHTS_CACHES)

    assertTrue(totalBalancesCache.nativeCache.isEmpty())
    assertTrue(datesBalancesCache.nativeCache.isEmpty())
    assertTrue(platformInsightsCache.nativeCache.isEmpty())
    assertTrue(cryptoInsightsCache.nativeCache.isEmpty())
    assertTrue(platformsBalancesInsightsCache.nativeCache.isEmpty())
    assertTrue(cryptosBalancesInsightsCache.nativeCache.isEmpty())

    verify(exactly = 1) { cacheManagerMock.getCache(TOTAL_BALANCES_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(DATES_BALANCES_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PLATFORM_INSIGHTS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(CRYPTO_INSIGHTS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PLATFORMS_BALANCES_INSIGHTS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(CRYPTOS_BALANCES_INSIGHTS_CACHE) }
  }

  @Test
  fun `should invalidate caches`() {
    val crypto = getCryptoEntity()
    val map = mapOf(listOf("bitcoin") to listOf(crypto))
    val goalResponseGoalIdMap = mapOf("123e4567-e89b-12d3-a456-426614174111" to getGoalResponse())
    val pageGoalsResponsePageMap = mapOf(0 to PageGoalResponse(1, 1, listOf(getGoalResponse())))
    val cryptosIdsCache = ConcurrentMapCache(CRYPTOS_CRYPTOS_IDS_CACHE, ConcurrentHashMap(map), false)
    val goalResponseGoalIdCache = ConcurrentMapCache(GOAL_RESPONSE_GOAL_ID_CACHE, ConcurrentHashMap(goalResponseGoalIdMap), false)
    val pageGoalsResponsePageCache = ConcurrentMapCache(PAGE_GOALS_RESPONSE_PAGE_CACHE, ConcurrentHashMap(pageGoalsResponsePageMap), false)

    every { cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE) } returns cryptosIdsCache
    every { cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE) } returns goalResponseGoalIdCache
    every { cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE) } returns pageGoalsResponsePageCache

    cacheService.invalidate(CacheType.CRYPTOS_CACHES, CacheType.GOALS_CACHES)

    val cryptosIdsStore = cryptosIdsCache.nativeCache
    val goalResponseGoalIdStore = goalResponseGoalIdCache.nativeCache
    val pageGoalsResponsePageStore = pageGoalsResponsePageCache.nativeCache

    assertTrue(cryptosIdsStore.isEmpty())
    assertTrue(goalResponseGoalIdStore.isEmpty())
    assertTrue(pageGoalsResponsePageStore.isEmpty())

    verify(exactly = 1) { cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE) }
  }

  private fun getMapCache(name: String, map: Map<*, *>) = ConcurrentMapCache(name, ConcurrentHashMap(map), false)

  private fun getPlatformInsightsResponse() = PlatformInsightsResponse(
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

  private fun getDateBalanceResponse() = DatesBalanceResponse(
    datesBalances = listOf(
      DateBalances("16 March 2024", BalancesResponse("1000", "918.45", "0.01438911")),
      DateBalances("17 March 2024", BalancesResponse("1500", "1377.67", "0.021583665"))
    ),
    change = BalanceChanges(50F, 50F, 49.99F),
    priceDifference = DifferencesChanges("500", "459.22", "0.007194555")
  )

  private fun getCryptosBalancesInsightsResponse() = CryptosBalancesInsightsResponse(
    balances = BalancesResponse(
      totalUSDBalance = "7108.39",
      totalBTCBalance = "0.2512793593",
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
          totalBTCBalance = "0.0861664843",
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

  private fun getPlatformsBalancesInsightsResponse() = PlatformsBalancesInsightsResponse(
    balances = BalancesResponse(
      totalUSDBalance = "7108.39",
      totalBTCBalance = "0.2512793593",
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
          totalBTCBalance = "0.0771904337",
          totalEURBalance = "1855.17"
        ),
        percentage = 27.97f
      )
    )
  )

  private fun getCryptoInsightResponse() = CryptoInsightResponse(
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

}
