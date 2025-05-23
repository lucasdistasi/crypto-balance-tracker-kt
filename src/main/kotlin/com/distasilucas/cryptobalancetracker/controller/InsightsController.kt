package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.controller.swagger.InsightsControllerAPI
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.SortBy
import com.distasilucas.cryptobalancetracker.model.SortParams
import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.service.CryptoInsightsService
import com.distasilucas.cryptobalancetracker.service.InsightsService
import com.distasilucas.cryptobalancetracker.service.PlatformInsightService
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.UUID
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/insights")
@CrossOrigin(origins = ["\${allowed-origins}"])
class InsightsController(
  private val insightsService: InsightsService,
  private val cryptoInsightsService: CryptoInsightsService,
  private val platformInsightService: PlatformInsightService
) : InsightsControllerAPI {

  @GetMapping
  override fun retrieveHomeInsights() = ResponseEntity.ok(insightsService.retrieveHomeInsightsResponse())

  @GetMapping("/dates-balances")
  override fun retrieveDatesBalances(@RequestParam dateRange: DateRange) =
    ResponseEntity.ok(insightsService.retrieveDatesBalances(dateRange))

  @GetMapping("/cryptos")
  override fun retrieveUserCryptosInsights(
    @RequestParam
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    page: Int,
    @RequestParam(required = false)
    sortBy: SortBy,
    @RequestParam(required = false)
    sortType: SortType
  ): ResponseEntity<PageUserCryptosInsightsResponse> {
    val sortParams = SortParams(sortBy, sortType)
    val userCryptosInsights = cryptoInsightsService.retrieveUserCryptosInsights(page, sortParams)

    if (userCryptosInsights == PageUserCryptosInsightsResponse.EMPTY) return ResponseEntity.noContent().build()

    return ResponseEntity.ok(userCryptosInsights)
  }

  @GetMapping("/cryptos/balances")
  override fun retrieveCryptosBalancesInsights(): ResponseEntity<List<BalancesChartResponse>> {
    val cryptosBalancesInsights = cryptoInsightsService.retrieveCryptosBalancesInsights()

    return okOrNoContent(cryptosBalancesInsights)
  }

  @GetMapping("/platforms/balances")
  override fun retrievePlatformsBalancesInsights(): ResponseEntity<List<BalancesChartResponse>> {
    val platformsBalancesInsights = platformInsightService.retrievePlatformsBalancesInsights()

    return okOrNoContent(platformsBalancesInsights)
  }

  @GetMapping("/cryptos/{coingeckoCryptoId}")
  override fun retrieveCryptoInsights(@PathVariable coingeckoCryptoId: String) =
    ResponseEntity.ok(cryptoInsightsService.retrieveCryptoInsights(coingeckoCryptoId))

  @GetMapping("/platforms/{platformId}")
  override fun retrievePlatformInsights(
    @PathVariable
    @UUID(message = PLATFORM_ID_UUID)
    platformId: String
  ) = ResponseEntity.ok(platformInsightService.retrievePlatformInsights(platformId))

  private fun <T> okOrNoContent(response: List<T>): ResponseEntity<List<T>> {
    return if (response.isEmpty()) ResponseEntity.noContent().build() else ResponseEntity.ok(response)
  }
}
