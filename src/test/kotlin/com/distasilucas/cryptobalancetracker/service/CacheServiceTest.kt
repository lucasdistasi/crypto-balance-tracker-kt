package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_CRYPTOS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_PLATFORMS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Platform
import getCryptoEntity
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
import org.springframework.data.domain.PageImpl
import java.util.concurrent.ConcurrentHashMap
import kotlin.NullPointerException

class CacheServiceTest {

    private val cacheManagerMock = mockk<CacheManager>()

    private val cacheService = CacheService(cacheManagerMock)

    @Test
    fun `should invalidate user cryptos cache if it exists`() {
        val userCrypto = getUserCrypto()
        val pageImpl = PageImpl(listOf(userCrypto))

        val userCryptosCacheMap = mapOf(SimpleKey::class.java to listOf(userCrypto))
        val userCryptosPageCacheMap = mapOf(0 to pageImpl)
        val userCryptosPlatformIdMap = mapOf("123e4567-e89b-12d3-a456-426614174111" to listOf(userCrypto))
        val userCryptosCoingeckoCryptoIdMap = mapOf("bitcoin" to listOf(userCrypto))

        val userCryptosCacheCache =
            ConcurrentMapCache(USER_CRYPTOS_CACHE, ConcurrentHashMap(userCryptosCacheMap), false)
        val userCryptosPageCacheCache =
            ConcurrentMapCache(USER_CRYPTOS_PAGE_CACHE, ConcurrentHashMap(userCryptosPageCacheMap), false)
        val userCryptosPlatformIdCache =
            ConcurrentMapCache(USER_CRYPTOS_PLATFORM_ID_CACHE, ConcurrentHashMap(userCryptosPlatformIdMap), false)
        val userCryptosCoingeckoCryptoIdCache =
            ConcurrentMapCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, ConcurrentHashMap(userCryptosCoingeckoCryptoIdMap), false)

        every { cacheManagerMock.getCache(USER_CRYPTOS_CACHE) } returns userCryptosCacheCache
        every { cacheManagerMock.getCache(USER_CRYPTOS_PAGE_CACHE) } returns userCryptosPageCacheCache
        every { cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE) } returns userCryptosPlatformIdCache
        every { cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE) } returns userCryptosCoingeckoCryptoIdCache

        cacheService.invalidateUserCryptosCaches()

        val userCryptosCacheStore = userCryptosCacheCache.nativeCache
        val userCryptosPageCacheStore = userCryptosPageCacheCache.nativeCache
        val userCryptosPlatformIdStore = userCryptosPlatformIdCache.nativeCache
        val userCryptosCoingeckoCryptoIdStore = userCryptosCoingeckoCryptoIdCache.nativeCache

        assertThat(userCryptosCacheStore).isEmpty()
        assertThat(userCryptosPageCacheStore).isEmpty()
        assertThat(userCryptosPlatformIdStore).isEmpty()
        assertThat(userCryptosCoingeckoCryptoIdStore).isEmpty()
        verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_CACHE) }
        verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_PAGE_CACHE) }
        verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE) }
        verify(exactly = 1) { cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE) }
    }

    @Test
    fun `should throw NullPointerException if user cryptos caches dont exists`() {
        every { cacheManagerMock.getCache(USER_CRYPTOS_CACHE) } returns null
        every { cacheManagerMock.getCache(USER_CRYPTOS_PAGE_CACHE) } returns null
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
        val map = mapOf(listOf("123e4567-e89b-12d3-a456-426614174000") to listOf(platform))
        val cache = ConcurrentMapCache(PLATFORMS_PLATFORMS_IDS_CACHE, ConcurrentHashMap(map), false)

        every { cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE) } returns cache

        cacheService.invalidatePlatformsCaches()

        val store = cache.nativeCache

        assertThat(store).isEmpty()
        verify(exactly = 1) { cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE) }
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
}