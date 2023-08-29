package com.distasilucas.cryptobalancetracker.configuration

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
    @Value("\${coingecko.api-key}")
    val coingeckoApiKey: String,

    @Value("\${coingecko.pro.url}")
    val coingeckoProUrl: String,

    @Value("\${coingecko.url}")
    val coingeckoUrl: String
) {

    @Bean
    fun coingeckoWebClient(): WebClient {
        val baseUrl = if (StringUtils.isNotBlank(coingeckoApiKey)) coingeckoProUrl else coingeckoUrl
        val httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(10))
        val httpConnector = ReactorClientHttpConnector(httpClient)

        return WebClient.builder()
            .codecs { it.defaultCodecs().maxInMemorySize(700 * 1024) }
            .baseUrl(baseUrl)
            .clientConnector(httpConnector)
            .build()
    }
}

