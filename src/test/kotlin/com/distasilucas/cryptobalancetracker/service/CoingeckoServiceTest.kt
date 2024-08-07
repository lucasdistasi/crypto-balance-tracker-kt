package com.distasilucas.cryptobalancetracker.service

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

private const val COINGECKO_API_URL = "https://api.coingecko.com/api/v3"
private const val PRO_COINGECKO_API_URL = "https://pro-api.coingecko.com/api/v3"

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

    val webClient = RestClient.create(COINGECKO_API_URL)
    coingeckoService = CoingeckoService("", "", webClient)

    val cryptos = coingeckoService.retrieveAllCryptos()

    assertFalse(cryptos.isEmpty())
  }

  @Test
  fun `should retrieve coingecko crypto info for bitcoin`() {
    val mockResponse = MockResponse()
    mockWebServer.enqueue(mockResponse)

    val webClient = RestClient.create(COINGECKO_API_URL)
    coingeckoService = CoingeckoService("", "", webClient)

    val coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo("bitcoin")

    assertEquals("bitcoin", coingeckoCryptoInfo.id)
  }

  @Test
  fun `should throw WebClientResponseException with Not Found code when searching for non existent crypto`() {
    val mockResponse = MockResponse()
    mockWebServer.enqueue(mockResponse)

    val webClient = RestClient.create(COINGECKO_API_URL)
    coingeckoService = CoingeckoService("", "", webClient)

    val exception = assertThrows<RestClientResponseException> { coingeckoService.retrieveCryptoInfo("pipicoin") }

    assertThat(exception.statusCode)
      .isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should throw WebClientResponseException with Unauthorized when using invalid api key for crypto info`() {
    val mockResponse = MockResponse()
    mockWebServer.enqueue(mockResponse)

    val webClient = RestClient.create(PRO_COINGECKO_API_URL)
    coingeckoService = CoingeckoService("TEST123", "", webClient)

    val exception = assertThrows<RestClientResponseException> { coingeckoService.retrieveCryptoInfo("bitcoin") }

    assertThat(exception.statusCode)
      .isEqualTo(HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun `should throw WebClientResponseException with Unauthorized when using invalid api key for all cryptos`() {
    val mockResponse = MockResponse()
    mockWebServer.enqueue(mockResponse)

    val webClient = RestClient.create(PRO_COINGECKO_API_URL)
    coingeckoService = CoingeckoService("TEST123", "", webClient)

    val exception = assertThrows<RestClientResponseException> { coingeckoService.retrieveAllCryptos() }

    assertThat(exception.statusCode)
      .isEqualTo(HttpStatus.UNAUTHORIZED)
  }
}
