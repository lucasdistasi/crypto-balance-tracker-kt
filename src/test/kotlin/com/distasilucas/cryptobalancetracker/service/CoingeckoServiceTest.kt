package com.distasilucas.cryptobalancetracker.service

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

private const val COINGECKO_API_URL = "https://api.coingecko.com/api/v3"

class CoingeckoServiceTest {

    companion object {
        private lateinit var mockWebServer: MockWebServer
        private lateinit var coingeckoService: CoingeckoService

        @BeforeAll
        @JvmStatic
        fun `set up`() {
            mockWebServer = MockWebServer()
            mockWebServer.start()
        }

        @AfterAll
        @JvmStatic
        fun `tear down`() {
            mockWebServer.shutdown()
        }
    }

    @Test
    fun `should retrieve all cryptos`() {
        val mockResponse = MockResponse()
        mockWebServer.enqueue(mockResponse)

        val webClient = WebClient.create(COINGECKO_API_URL)
        coingeckoService = CoingeckoService(null, webClient)

        val cryptos = coingeckoService.retrieveAllCryptos()

        StepVerifier.create(Flux.just(cryptos))
            .expectNextMatches { it.isNotEmpty() }
            .verifyComplete()
    }

    @Test
    fun `should retrieve coingecko crypto info for bitcoin`() {
        val mockResponse = MockResponse()
        mockWebServer.enqueue(mockResponse)

        val webClient = WebClient.create(COINGECKO_API_URL)
        coingeckoService = CoingeckoService(null, webClient)

        val coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo("bitcoin")

        StepVerifier.create(Mono.just(coingeckoCryptoInfo))
            .expectNextMatches { it.id == "bitcoin" }
            .verifyComplete()
    }

    @Test
    fun `should throw WebClientResponseException with Not Found code when searching for non existent crypto`() {
        val mockResponse = MockResponse()
        mockWebServer.enqueue(mockResponse)

        val webClient = WebClient.create(COINGECKO_API_URL)
        coingeckoService = CoingeckoService(null, webClient)

        val exception = assertThrows<WebClientResponseException> { coingeckoService.retrieveCryptoInfo("pipicoin") }

        assertThat(exception)
            .usingRecursiveComparison()
            .comparingOnlyFields("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should throw WebClientResponseException with Bad Request when using invalid api key for crypto info`() {
        val mockResponse = MockResponse()
        mockWebServer.enqueue(mockResponse)

        val webClient = WebClient.create(COINGECKO_API_URL)
        coingeckoService = CoingeckoService("TEST123", webClient)

        val exception = assertThrows<WebClientResponseException> { coingeckoService.retrieveCryptoInfo("bitcoin") }

        assertThat(exception)
            .usingRecursiveComparison()
            .comparingOnlyFields("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should throw WebClientResponseException with Bad Request when using invalid api key for all cryptos`() {
        val mockResponse = MockResponse()
        mockWebServer.enqueue(mockResponse)

        val webClient = WebClient.create(COINGECKO_API_URL)
        coingeckoService = CoingeckoService("TEST123", webClient)

        val exception = assertThrows<WebClientResponseException> { coingeckoService.retrieveAllCryptos() }

        assertThat(exception)
            .usingRecursiveComparison()
            .comparingOnlyFields("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST)
    }
}