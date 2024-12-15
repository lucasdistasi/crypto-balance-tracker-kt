package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.controller.swagger.InsightsControllerAPI
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.SortBy
import com.distasilucas.cryptobalancetracker.model.SortParams
import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.TotalBalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.service.InsightsService
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
import java.util.Optional

@Validated
@RestController
@RequestMapping("/api/v1/insights")
@CrossOrigin(origins = ["\${allowed-origins}"])
class InsightsController(private val insightsService: InsightsService) : InsightsControllerAPI {

  @GetMapping("/total-balances")
  override fun retrieveTotal(): ResponseEntity<TotalBalancesResponse> {
    val totalBalances = insightsService.retrieveTotal()

    return ResponseEntity.ok(totalBalances)
  }

  @GetMapping("/balances")
  override fun retrieveTotalBalances(): ResponseEntity<BalancesResponse> {
    val totalBalances = insightsService.retrieveTotalBalances()

    return ResponseEntity.ok(totalBalances)
  }

  @GetMapping("/dates-balances")
  override fun retrieveDatesBalances(@RequestParam dateRange: DateRange): ResponseEntity<DatesBalanceResponse> {
    val dateBalances = insightsService.retrieveDatesBalances(dateRange)

    return ResponseEntity.ok(dateBalances)
  }

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
    val serCryptosInsights = insightsService.retrieveUserCryptosInsights(page, sortParams)

    return okOrNoContent(serCryptosInsights)
  }

  @GetMapping("/cryptos/platforms")
  override fun retrieveUserCryptosPlatformsInsights(
    @RequestParam
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    page: Int,
    @RequestParam(required = false)
    sortBy: SortBy,
    @RequestParam(required = false)
    sortType: SortType
  ): ResponseEntity<PageUserCryptosInsightsResponse> {
    val sortParams = SortParams(sortBy, sortType)
    val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(page, sortParams)

    return okOrNoContent(userCryptosPlatformsInsights)
  }

  @GetMapping("/cryptos/balances")
  override fun retrieveCryptosBalancesInsights(): ResponseEntity<CryptosBalancesInsightsResponse> {
    val cryptosBalancesInsights = insightsService.retrieveCryptosBalancesInsights()

    return ResponseEntity.ok(cryptosBalancesInsights)
  }

  @GetMapping("/platforms/balances")
  override fun retrievePlatformsBalancesInsights(): ResponseEntity<PlatformsBalancesInsightsResponse> {
    val platformsBalancesInsights = insightsService.retrievePlatformsBalancesInsights()

    return ResponseEntity.ok(platformsBalancesInsights)
  }

  @GetMapping("/cryptos/{coingeckoCryptoId}")
  override fun retrieveCryptoInsights(@PathVariable coingeckoCryptoId: String): ResponseEntity<CryptoInsightResponse> {
    val cryptoInsights = insightsService.retrieveCryptoInsights(coingeckoCryptoId)

    return ResponseEntity.ok(cryptoInsights)
  }

  @GetMapping("/platforms/{platformId}")
  override fun retrievePlatformInsights(
    @PathVariable
    @UUID(message = PLATFORM_ID_UUID)
    platformId: String
  ): ResponseEntity<PlatformInsightsResponse> {
    val platformsInsights = insightsService.retrievePlatformInsights(platformId)

    return ResponseEntity.ok(platformsInsights)
  }

  private fun <T> okOrNoContent(body: Optional<T>): ResponseEntity<T> {
    return if (body.isEmpty) ResponseEntity.noContent().build() else ResponseEntity.ok(body.get())
  }
}
