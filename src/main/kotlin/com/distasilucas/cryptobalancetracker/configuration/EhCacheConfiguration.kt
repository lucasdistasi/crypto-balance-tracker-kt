package com.distasilucas.cryptobalancetracker.configuration

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_CRYPTOS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_INFO_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_PLATFORMS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo
import org.ehcache.config.CacheConfiguration
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

@Configuration
class EhCacheConfiguration {

    @Bean
    fun ehcacheManager(): CacheManager {
        val cacheManager = Caching.getCachingProvider().cacheManager
        val coingeckoCryptosCache = getCoingeckoCryptosCache()
        val coingeckoCryptoInfoCache = getCoingeckoCryptoInfoCache()
        val userCryptosCache = getAllUserCryptosCache()
        val userCryptosPageCache = getUserCryptosPageCache()
        val userCryptosPlatformIdCache = getUserCryptosPlatformIdCache()
        val userCryptosCoingeckoCryptoIdCache = getUserCryptosCoingeckoCryptoIdCache()
        val platformsIdsCache = getPlatformsIdsCache()
        val cryptoCoingeckoCryptoIdCache = getCryptoCoingeckoCryptoIdCache()
        val cryptosIdsCache = getCryptosIdsCache()

        cacheManager.createCache(COINGECKO_CRYPTOS_CACHE, getCacheConfiguration(coingeckoCryptosCache))
        cacheManager.createCache(CRYPTO_INFO_CACHE, getCacheConfiguration(coingeckoCryptoInfoCache))
        cacheManager.createCache(USER_CRYPTOS_CACHE, getCacheConfiguration(userCryptosCache))
        cacheManager.createCache(USER_CRYPTOS_PAGE_CACHE, getCacheConfiguration(userCryptosPageCache))
        cacheManager.createCache(USER_CRYPTOS_PLATFORM_ID_CACHE, getCacheConfiguration(userCryptosPlatformIdCache))
        cacheManager.createCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, getCacheConfiguration(userCryptosCoingeckoCryptoIdCache))
        cacheManager.createCache(PLATFORMS_PLATFORMS_IDS_CACHE, getCacheConfiguration(platformsIdsCache))
        cacheManager.createCache(CRYPTO_COINGECKO_CRYPTO_ID_CACHE, getCacheConfiguration(cryptoCoingeckoCryptoIdCache))
        cacheManager.createCache(CRYPTOS_CRYPTOS_IDS_CACHE, getCacheConfiguration(cryptosIdsCache))

        return cacheManager
    }

    private fun getCoingeckoCryptosCache(): CacheConfiguration<SimpleKey, List<CoingeckoCrypto>> {
        val coinListClass = CastUtils.cast<Class<List<CoingeckoCrypto>>>(MutableList::class.java)
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(30))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            SimpleKey::class.java,
            coinListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun getCoingeckoCryptoInfoCache(): CacheConfiguration<String, CoingeckoCryptoInfo> {
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String::class.java,
            CoingeckoCryptoInfo::class.java,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun getAllUserCryptosCache(): CacheConfiguration<SimpleKey, List<UserCrypto>> {
        val userCryptoListClass = CastUtils.cast<Class<List<UserCrypto>>>(MutableList::class.java)
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            SimpleKey::class.java,
            userCryptoListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun getUserCryptosPageCache(): CacheConfiguration<Int, Page<UserCrypto>> {
        val userCryptoPageClass = CastUtils.cast<Class<Page<UserCrypto>>>(Page::class.java)
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Int::class.javaObjectType,
            userCryptoPageClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun getUserCryptosPlatformIdCache(): CacheConfiguration<String, List<UserCrypto>> {
        val userCryptoListClass = CastUtils.cast<Class<List<UserCrypto>>>(MutableList::class.java)
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String::class.java,
            userCryptoListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun getUserCryptosCoingeckoCryptoIdCache(): CacheConfiguration<String, List<UserCrypto>> {
        val userCryptoListClass = CastUtils.cast<Class<List<UserCrypto>>>(MutableList::class.java)
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String::class.java,
            userCryptoListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun getPlatformsIdsCache(): CacheConfiguration<Collection<String>, List<Platform>> {
        val stringCollectionClass = CastUtils.cast<Class<Collection<String>>>(Collection::class.java)
        val platformListClass = CastUtils.cast<Class<List<Platform>>>(MutableList::class.java)
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            stringCollectionClass,
            platformListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun getCryptoCoingeckoCryptoIdCache(): CacheConfiguration<String, Crypto> {
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(2))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String::class.java,
            Crypto::class.java,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun getCryptosIdsCache(): CacheConfiguration<Collection<String>, List<Crypto>> {
        val stringCollectionClass = CastUtils.cast<Class<Collection<String>>>(Collection::class.java)
        val cryptoListClass = CastUtils.cast<Class<List<Crypto>>>(MutableList::class.java)
        val resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build()
        val expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(2))

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            stringCollectionClass,
            cryptoListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build()
    }

    private fun <K,V> getCacheConfiguration(cache: CacheConfiguration<K, V>) =
        Eh107Configuration.fromEhcacheCacheConfiguration(cache)
}