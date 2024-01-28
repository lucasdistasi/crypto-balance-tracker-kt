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
        @Value("\${coingecko.api-key.pro}")
        private val proCoingeckoApiKey: String,
        @Value("\${coingecko.api-key.demo}")
        private val demoCoingeckoApiKey: String,
        private val coingeckoRestClient: RestClient
) {

    private val logger = KotlinLogging.logger { }
    private val DEMO_API_KEY_QUERY_PARAM = "x_cg_demo_api_key"
    private val PRO_API_KEY_QUERY_PARAM = "x_cg_pro_api_key"

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
                    .queryParam(PRO_API_KEY_QUERY_PARAM, proCoingeckoApiKey)
                    .build()
        }

        val freeCoingeckoURI = Function { uriBuilder: UriBuilder -> uriBuilder.path(COINS_URI)
                    .queryParam(DEMO_API_KEY_QUERY_PARAM, demoCoingeckoApiKey)
                    .build()
        }

        return if (StringUtils.isNotBlank(proCoingeckoApiKey)) proCoingeckoURI else freeCoingeckoURI
    }

    private fun getCoingeckoCryptoInfoURI(url: String): Function<UriBuilder, URI> {
        val params: MultiValueMap<String, String> = HttpHeaders()
        params.add("tickers", "false")
        params.add("community_data", "false")
        params.add("developer_data", "false")
        params.add(DEMO_API_KEY_QUERY_PARAM, demoCoingeckoApiKey)

        val proCoingeckoUri = Function { uriBuilder: UriBuilder ->
            uriBuilder.path(url)
                    .queryParam(PRO_API_KEY_QUERY_PARAM, proCoingeckoApiKey)
                    .queryParams(params)
                    .build()
        }

        val freeCoingeckoUri = Function { uriBuilder: UriBuilder ->
            uriBuilder.path(url)
                    .queryParams(params)
                    .build()
        }

        return if (StringUtils.isNotBlank(proCoingeckoApiKey)) proCoingeckoUri else freeCoingeckoUri
    }
}