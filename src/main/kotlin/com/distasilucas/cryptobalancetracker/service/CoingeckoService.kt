package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_INFO_CACHE
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.util.function.Function

const val COIN_URI = "/coins/"
const val COINS_URI = "$COIN_URI/list"

@Service
class CoingeckoService(
        @Value("\${coingecko.api-key}")
        private val coingeckoApiKey: String,
        private val coingeckoRestClient: RestClient
) {

    private val logger = KotlinLogging.logger { }

    @Cacheable(cacheNames = [COINGECKO_CRYPTOS_CACHE])
    @Retryable(retryFor = [RestClientException::class], backoff = Backoff(delay = 1500))
    fun retrieveAllCryptos(): List<CoingeckoCrypto> {
        logger.info { "Hitting Coingecko API... Retrieving all cryptos..." }

        return coingeckoRestClient.get()
                .uri(getCryptosURI())
                .retrieve()
                .body(object : ParameterizedTypeReference<List<CoingeckoCrypto>>() {}) ?: emptyList()
    }

    @Cacheable(cacheNames = [CRYPTO_INFO_CACHE], key = "#coingeckoCryptoId")
    @Retryable(retryFor = [RestClientException::class], backoff = Backoff(delay = 1500))
    fun retrieveCryptoInfo(coingeckoCryptoId: String): CoingeckoCryptoInfo {
        logger.info { "Hitting Coingecko API... Retrieving information for $coingeckoCryptoId..." }
        val coinURI = "$COIN_URI/$coingeckoCryptoId"

        return coingeckoRestClient.get()
                .uri(getCoingeckoCryptoInfoURI(coinURI))
                .retrieve()
                .body(object : ParameterizedTypeReference<CoingeckoCryptoInfo>() {})
                ?: throw ApiException("Error retrieving crypto information for $coingeckoCryptoId")
    }

    private fun getCryptosURI(): Function<UriBuilder, URI> {
        val proCoingeckoURI = Function { uriBuilder: UriBuilder ->
            uriBuilder.path(COINS_URI)
                    .queryParam("x_cg_pro_api_key", coingeckoApiKey)
                    .build()
        }

        val freeCoingeckoURI = Function { uriBuilder: UriBuilder -> uriBuilder.path(COINS_URI).build() }

        return if (StringUtils.isNotBlank(coingeckoApiKey)) proCoingeckoURI else freeCoingeckoURI
    }

    private fun getCoingeckoCryptoInfoURI(url: String): Function<UriBuilder, URI> {
        val commonParams: MultiValueMap<String, String> = HttpHeaders()
        commonParams.add("tickers", "false")
        commonParams.add("community_data", "false")
        commonParams.add("developer_data", "false")

        val proCoingeckoUri = Function { uriBuilder: UriBuilder ->
            uriBuilder.path(url)
                    .queryParam("x_cg_pro_api_key", coingeckoApiKey)
                    .queryParams(commonParams)
                    .build()
        }

        val freeCoingeckoUri = Function { uriBuilder: UriBuilder ->
            uriBuilder.path(url)
                    .queryParams(commonParams)
                    .build()
        }

        return if (StringUtils.isNotBlank(coingeckoApiKey)) proCoingeckoUri else freeCoingeckoUri
    }
}