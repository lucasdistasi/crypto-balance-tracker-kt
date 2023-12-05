package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.ALL_PLATFORMS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_CRYPTOS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_PLATFORMS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PLATFORM_ID_CACHE
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class CacheService(private val cacheManager: CacheManager) {

    private val logger = KotlinLogging.logger { }

    fun invalidateUserCryptosCaches() {
        logger.info { "Invalidating user cryptos cache" }

        cacheManager.getCache(USER_CRYPTOS_CACHE)!!.invalidate()
        cacheManager.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE)!!.invalidate()
        cacheManager.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE)!!.invalidate()
    }

    fun invalidatePlatformsCaches() {
        logger.info { "Invalidating platforms cache" }

        cacheManager.getCache(PLATFORMS_PLATFORMS_IDS_CACHE)!!.invalidate()
        cacheManager.getCache(ALL_PLATFORMS_CACHE)!!.invalidate()
        cacheManager.getCache(PLATFORM_PLATFORM_ID_CACHE)!!.invalidate()
    }

    fun invalidateCryptosCache() {
        logger.info { "Invalidating cryptos cache" }

        cacheManager.getCache(CRYPTOS_CRYPTOS_IDS_CACHE)!!.invalidate()
    }
}