package com.distasilucas.cryptobalancetracker.configuration

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_INFO_CACHE
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
import org.springframework.data.util.CastUtils
import java.time.Duration
import javax.cache.CacheManager
import javax.cache.Caching
import javax.cache.configuration.Configuration as CacheConfig

@Configuration
class EhCacheConfiguration {

    @Bean
    fun ehcacheManager(): CacheManager {
        val coingeckoCryptosCache = getCryptosCache()
        val coingeckoCryptoInfoCache = getCryptoInfoCache()
        val cachingProvider = Caching.getCachingProvider()
        val cacheManager = cachingProvider.cacheManager

        cacheManager.createCache(COINGECKO_CRYPTOS_CACHE, getCoingeckoCryptosCacheConfiguration(coingeckoCryptosCache))
        cacheManager.createCache(CRYPTO_INFO_CACHE, getCryptoInfoConfiguration(coingeckoCryptoInfoCache))

        return cacheManager
    }

    private fun getCryptosCache(): CacheConfiguration<SimpleKey, List<CoingeckoCrypto>> {
        val coinListClass = CastUtils.cast<Class<List<CoingeckoCrypto>>>(MutableList::class.java)

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            SimpleKey::class.java, coinListClass,
            ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(2, MemoryUnit.MB).build()
        ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(30))).build()
    }

    private fun getCryptoInfoCache(): CacheConfiguration<String, CoingeckoCryptoInfo> {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String::class.java, CoingeckoCryptoInfo::class.java,
            ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(1, MemoryUnit.MB).build()
        ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))).build()
    }

    private fun getCoingeckoCryptosCacheConfiguration(
        coingeckoCryptosCache: CacheConfiguration<SimpleKey, List<CoingeckoCrypto>>
    ): CacheConfig<SimpleKey, List<CoingeckoCrypto>> {
        return Eh107Configuration.fromEhcacheCacheConfiguration { coingeckoCryptosCache }
    }

    private fun getCryptoInfoConfiguration(
        cryptoInfoCache: CacheConfiguration<String, CoingeckoCryptoInfo>
    ): CacheConfig<String, CoingeckoCryptoInfo> {
        return Eh107Configuration.fromEhcacheCacheConfiguration { cryptoInfoCache }
    }
}