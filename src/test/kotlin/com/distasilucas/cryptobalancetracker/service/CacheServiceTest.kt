package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.ALL_PLATFORMS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_CRYPTOS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.GOAL_RESPONSE_GOAL_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PAGE_GOALS_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_PLATFORMS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_RESPONSE_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import getCryptoEntity
import getGoalResponse
import getUserCrypto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.interceptor.SimpleKey
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class CacheServiceTest {

  private val cacheManagerMock = mockk<CacheManager>()

  private val cacheService = CacheService(cacheManagerMock)

  @Test
  fun `should invalidate user cryptos cache if it exists`() {
    val userCrypto = getUserCrypto()
    val userCryptoResponse = userCrypto.toUserCryptoResponse("Bitcoin", "BINANCE")

    val userCryptosCacheMap = mapOf(SimpleKey::class.java to listOf(userCrypto))
    val userCryptosPlatformIdMap = mapOf("123e4567-e89b-12d3-a456-426614174111" to listOf(userCrypto))
    val userCryptosCoingeckoCryptoIdMap = mapOf("bitcoin" to listOf(userCrypto))
    val userCryptoIdMap = mapOf("bc7a8ee5-13f9-4405-a7fb-887458c21bed" to listOf(userCrypto))
    val userCryptoResponseUserCryptoIdMap = mapOf("bc7a8ee5-13f9-4405-a7fb-887458c21bed" to listOf(userCryptoResponse))
    val userCryptosResponsePageMap = mapOf(0 to listOf(userCryptoResponse))

    val userCryptosCache =
      ConcurrentMapCache(USER_CRYPTOS_CACHE, ConcurrentHashMap(userCryptosCacheMap), false)
    val userCryptosPlatformIdCache =
      ConcurrentMapCache(USER_CRYPTOS_PLATFORM_ID_CACHE, ConcurrentHashMap(userCryptosPlatformIdMap), false)
    val userCryptosCoingeckoCryptoIdCache =
      ConcurrentMapCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, ConcurrentHashMap(userCryptosCoingeckoCryptoIdMap), false)
    val userCryptoIdCache =
      ConcurrentMapCache(USER_CRYPTO_ID_CACHE, ConcurrentHashMap(userCryptoIdMap), false)
    val userCryptoResponseUserCryptoIdCache =
      ConcurrentMapCache(USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE, ConcurrentHashMap(userCryptoResponseUserCryptoIdMap), false)
    val userCryptosResponsePageCache =
      ConcurrentMapCache(USER_CRYPTOS_RESPONSE_PAGE_CACHE, ConcurrentHashMap(userCryptosResponsePageMap), false)

    every { cacheManagerMock.getCache(USER_CRYPTOS_CACHE) } returns userCryptosCache
    every { cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE) } returns userCryptosPlatformIdCache
    every { cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE) } returns userCryptosCoingeckoCryptoIdCache
    every { cacheManagerMock.getCache(USER_CRYPTO_ID_CACHE) } returns userCryptoIdCache
    every { cacheManagerMock.getCache(USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE) } returns userCryptoResponseUserCryptoIdCache
    every { cacheManagerMock.getCache(USER_CRYPTOS_RESPONSE_PAGE_CACHE) } returns userCryptosResponsePageCache

    cacheService.invalidateUserCryptosCaches()

    val userCryptosCacheStore = userCryptosCache.nativeCache
    val userCryptosPlatformIdStore = userCryptosPlatformIdCache.nativeCache
    val userCryptosCoingeckoCryptoIdStore = userCryptosCoingeckoCryptoIdCache.nativeCache
    val userCryptoIdStore = userCryptoIdCache.nativeCache
    val userCryptoResponseUserCryptoIdStore = userCryptoResponseUserCryptoIdCache.nativeCache
    val userCryptosResponsePageStore = userCryptosResponsePageCache.nativeCache

    assertThat(userCryptosCacheStore).isEmpty()
    assertThat(userCryptosPlatformIdStore).isEmpty()
    assertThat(userCryptosCoingeckoCryptoIdStore).isEmpty()
    assertThat(userCryptoIdStore).isEmpty()
    assertThat(userCryptoResponseUserCryptoIdStore).isEmpty()
    assertThat(userCryptosResponsePageStore).isEmpty()
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTO_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_RESPONSE_PAGE_CACHE) }
  }

  @Test
  fun `should throw NullPointerException if user cryptos caches dont exists`() {
    every { cacheManagerMock.getCache(USER_CRYPTOS_CACHE) } returns null
    every { cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE) } returns null
    every { cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE) } returns null

    assertThrows<NullPointerException> { cacheService.invalidateUserCryptosCaches() }
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

    val platformsIdsCache = ConcurrentMapCache(PLATFORMS_PLATFORMS_IDS_CACHE, ConcurrentHashMap(platformsIdsMap), false)
    val allPlatformsCache = ConcurrentMapCache(ALL_PLATFORMS_CACHE, ConcurrentHashMap(allPlatformsMap), false)
    val platformIdCache = ConcurrentMapCache(PLATFORM_PLATFORM_ID_CACHE, ConcurrentHashMap(platformIdMap), false)

    every { cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE) } returns platformsIdsCache
    every { cacheManagerMock.getCache(ALL_PLATFORMS_CACHE) } returns allPlatformsCache
    every { cacheManagerMock.getCache(PLATFORM_PLATFORM_ID_CACHE) } returns platformIdCache

    cacheService.invalidatePlatformsCaches()

    val platformsIdsStore = platformsIdsCache.nativeCache
    val allPlatformsStore = allPlatformsCache.nativeCache
    val platformIdStore = platformIdCache.nativeCache

    assertThat(platformsIdsStore).isEmpty()
    assertThat(allPlatformsStore).isEmpty()
    assertThat(platformIdStore).isEmpty()
    verify(exactly = 1) { cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(ALL_PLATFORMS_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PLATFORM_PLATFORM_ID_CACHE) }
  }

  @Test
  fun `should throw NullPointerException if platforms cache does not exists`() {
    every { cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE) } returns null

    assertThrows<NullPointerException> { cacheService.invalidatePlatformsCaches() }
  }

  @Test
  fun `should invalidate cryptos cache if it exists`() {
    val crypto = getCryptoEntity()
    val map = mapOf(listOf("bitcoin") to listOf(crypto))
    val cache = ConcurrentMapCache(CRYPTOS_CRYPTOS_IDS_CACHE, ConcurrentHashMap(map), false)

    every { cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE) } returns cache

    cacheService.invalidateCryptosCache()

    val store = cache.nativeCache

    assertThat(store).isEmpty()
    verify(exactly = 1) { cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE) }
  }

  @Test
  fun `should throw NullPointerException if cryptos cache does not exists`() {
    every { cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE) } returns null

    assertThrows<NullPointerException> { cacheService.invalidateCryptosCache() }
  }

  @Test
  fun `should invalidate goals cache if it exists`() {
    val goalResponseGoalIdMap = mapOf("123e4567-e89b-12d3-a456-426614174111" to getGoalResponse())
    val pageGoalsResponsePageMap = mapOf(0 to PageGoalResponse(1, 1, listOf(getGoalResponse())))

    val goalResponseGoalIdCache = ConcurrentMapCache(GOAL_RESPONSE_GOAL_ID_CACHE, ConcurrentHashMap(goalResponseGoalIdMap), false)
    val pageGoalsResponsePageCache = ConcurrentMapCache(PAGE_GOALS_RESPONSE_PAGE_CACHE, ConcurrentHashMap(pageGoalsResponsePageMap), false)

    every { cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE) } returns goalResponseGoalIdCache
    every { cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE) } returns pageGoalsResponsePageCache

    cacheService.invalidateGoalsCaches()

    val goalResponseGoalIdStore = goalResponseGoalIdCache.nativeCache
    val pageGoalsResponsePageStore = pageGoalsResponsePageCache.nativeCache

    assertThat(goalResponseGoalIdStore).isEmpty()
    assertThat(pageGoalsResponsePageStore).isEmpty()
    verify(exactly = 1) { cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE) }
  }

  @Test
  fun `should throw NullPointerException if goals cache does not exists`() {
    every { cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE) } returns null
    every { cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE) } returns null

    assertThrows<NullPointerException> { cacheService.invalidateGoalsCaches() }
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

    val priceTargetIdCache = ConcurrentMapCache(PRICE_TARGET_ID_CACHE, ConcurrentHashMap(priceTargetIdMap), false)
    val priceTargetResponseIdCache = ConcurrentMapCache(PRICE_TARGET_RESPONSE_ID_CACHE, ConcurrentHashMap(priceTargetResponseIdMap), false)
    val priceTargetResponsePageCache = ConcurrentMapCache(PRICE_TARGET_RESPONSE_PAGE_CACHE, ConcurrentHashMap(priceTargetResponsePageMap), false)

    every { cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE) } returns priceTargetIdCache
    every { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_ID_CACHE) } returns priceTargetResponseIdCache
    every { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_PAGE_CACHE) } returns priceTargetResponsePageCache

    cacheService.invalidatePriceTargetCaches()

    val priceTargetIdStore = priceTargetIdCache.nativeCache
    val priceTargetResponseIdStore = priceTargetResponseIdCache.nativeCache
    val priceTargetResponsePageStore = priceTargetResponsePageCache.nativeCache

    assertThat(priceTargetIdStore).isEmpty()
    assertThat(priceTargetResponseIdStore).isEmpty()
    assertThat(priceTargetResponsePageStore).isEmpty()
    verify(exactly = 1) { cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_ID_CACHE) }
    verify(exactly = 1) { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_PAGE_CACHE) }
  }

  @Test
  fun `should throw NullPointerException if price targets cache does not exists`() {
    every { cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE) } returns null
    every { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_ID_CACHE) } returns null
    every { cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_PAGE_CACHE) } returns null

    assertThrows<NullPointerException> { cacheService.invalidatePriceTargetCaches() }
  }
}
