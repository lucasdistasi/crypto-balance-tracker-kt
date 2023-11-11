package com.distasilucas.cryptobalancetracker.scheduler

import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.service.CoingeckoService
import com.distasilucas.cryptobalancetracker.service.CryptoService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime

@Component
class CryptoScheduler(
    @Value("\${max-limit-crypto}")
    private val maxLimit: Int,
    private val clock: Clock,
    private val cryptoService: CryptoService,
    private val coingeckoService: CoingeckoService
) {

    private val logger = KotlinLogging.logger { }

    @Scheduled(cron = "0 */3 * ? * *")
    fun updateCryptosInformation() {
        logger.info { "Running cron to update cryptos information..." }

        val cryptosToUpdate = getCryptosToUpdate()
            .map {
                val crypto = coingeckoService.retrieveCryptoInfo(it.id)

                Crypto(
                    id = crypto.id,
                    name = crypto.name,
                    ticker = crypto.symbol,
                    image = crypto.image.large,
                    lastKnownPrice = crypto.marketData.currentPrice.usd,
                    lastKnownPriceInEUR = crypto.marketData.currentPrice.eur,
                    lastKnownPriceInBTC = crypto.marketData.currentPrice.btc,
                    circulatingSupply = crypto.marketData.circulatingSupply,
                    maxSupply = crypto.marketData.maxSupply ?: BigDecimal.ZERO,
                    lastUpdatedAt = LocalDateTime.now(clock)
                )
            }

        if (cryptosToUpdate.isNotEmpty()) {
            logger.info { "About to update ${cryptosToUpdate.size} crypto(s)" }

            cryptoService.updateCryptos(cryptosToUpdate)
        } else {
            logger.info { "No cryptos to update" }
        }
    }

    private fun getCryptosToUpdate(): List<Crypto> {
        return cryptoService.findOldestNCryptosByLastPriceUpdate(
            LocalDateTime.now(clock).minusMinutes(5),
            maxLimit
        )
    }
}