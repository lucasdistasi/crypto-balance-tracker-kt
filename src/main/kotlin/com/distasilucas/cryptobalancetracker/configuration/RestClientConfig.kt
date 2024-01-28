package com.distasilucas.cryptobalancetracker.configuration

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class RestClientConfig(
        @Value("\${coingecko.api-key.pro}")
        private val proCoingeckoApiKey: String,

        @Value("\${coingecko.url.pro}")
        private val coingeckoProUrl: String,

        @Value("\${coingecko.url.free}")
        private val coingeckoUrl: String
) {

    @Bean
    fun coingeckoRestClient(): RestClient {
        val baseUrl = if (StringUtils.isNotBlank(proCoingeckoApiKey)) coingeckoProUrl else coingeckoUrl

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(getSimpleClientHttpRequestFactory())
                .build()
    }

    private fun getSimpleClientHttpRequestFactory(): SimpleClientHttpRequestFactory {
        val simpleClientHttpRequestFactory = SimpleClientHttpRequestFactory()
        simpleClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(5))
        simpleClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(10))
        simpleClientHttpRequestFactory.setChunkSize(36 * 1024)

        return simpleClientHttpRequestFactory
    }

}