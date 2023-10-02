package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.INVALID_PLATFORM_UUID
import com.distasilucas.cryptobalancetracker.controller.swagger.InsightsControllerAPI
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Optional

@Validated
@RestController
@RequestMapping("/api/v1/insights")
class InsightsController(private val insightsService: InsightsService) : InsightsControllerAPI {

    @GetMapping("/balances")
    override fun retrieveTotalBalancesInsights(): ResponseEntity<BalancesResponse> {
        val totalBalances = insightsService.retrieveTotalBalancesInsights()

        return okOrNoContent(totalBalances)
    }

    @GetMapping("/cryptos")
    override fun retrieveUserCryptosInsights(
        @RequestParam
        @Min(value = 0, message = "Page must be greater than or equal to 0")
        page: Int
    ): ResponseEntity<PageUserCryptosInsightsResponse> {
        val serCryptosInsights = insightsService.retrieveUserCryptosInsights(page)

        return okOrNoContent(serCryptosInsights)
    }

    @GetMapping("/cryptos/platforms")
    override fun retrieveUserCryptosPlatformsInsights(
        @RequestParam
        @Min(value = 0, message = "Page must be greater than or equal to 0")
        page: Int
    ): ResponseEntity<PageUserCryptosInsightsResponse> {
        val userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(page)

        return okOrNoContent(userCryptosPlatformsInsights)
    }

    @GetMapping("/cryptos/balances")
    override fun retrieveCryptosBalancesInsights(): ResponseEntity<CryptosBalancesInsightsResponse> {
        val cryptosBalancesInsights = insightsService.retrieveCryptosBalancesInsights()

        return okOrNoContent(cryptosBalancesInsights)
    }

    @GetMapping("/platforms/balances")
    override fun retrievePlatformsBalancesInsights(): ResponseEntity<PlatformsBalancesInsightsResponse> {
        val platformsBalancesInsights = insightsService.retrievePlatformsBalancesInsights()

        return okOrNoContent(platformsBalancesInsights)
    }

    @GetMapping("/cryptos/{coingeckoCryptoId}")
    override fun retrieveCryptoInsights(@PathVariable coingeckoCryptoId: String): ResponseEntity<CryptoInsightResponse> {
        val cryptoInsights = insightsService.retrieveCryptoInsights(coingeckoCryptoId)

        return okOrNoContent(cryptoInsights)
    }

    @GetMapping("/platforms/{platformId}")
    override fun retrievePlatformInsights(
        @PathVariable
        @UUID(message = INVALID_PLATFORM_UUID)
        platformId: String
    ): ResponseEntity<PlatformInsightsResponse> {
        val platformsInsights = insightsService.retrievePlatformInsights(platformId)

        return okOrNoContent(platformsInsights)
    }

    private fun <T> okOrNoContent(body: Optional<T>): ResponseEntity<T> {
        return if (body.isEmpty) ResponseEntity.noContent().build() else ResponseEntity.ok(body.get())
    }
}