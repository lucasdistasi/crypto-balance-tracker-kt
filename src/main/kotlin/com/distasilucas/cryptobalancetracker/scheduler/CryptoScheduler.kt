package com.distasilucas.cryptobalancetracker.scheduler

import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.exception.TooManyRequestsException
import com.distasilucas.cryptobalancetracker.service.CoingeckoService
import com.distasilucas.cryptobalancetracker.service.CryptoService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import java.time.Clock
import java.time.LocalDateTime

@Component
class CryptoScheduler(
  @Value("\${crypto.scheduler.update.quantity}")
  private val maxLimit: Int,
  private val clock: Clock,
  private val cryptoService: CryptoService,
  private val coingeckoService: CoingeckoService
) {

  private val logger = KotlinLogging.logger { }

  @Scheduled(cron = "\${crypto.scheduler.update.cron}")
  fun updateCryptosInformation() {
    logger.info { "Running cron to update cryptos information..." }

    val cryptosToUpdate = getCryptosToUpdate()
      .map {
        try {
          val coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo(it.id)
          coingeckoCryptoInfo.toCrypto(clock)
        } catch (exception: RestClientResponseException) {
          if (exception.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
            throw TooManyRequestsException()
          } else {
            logger.warn { "A WebClientResponseException occurred while retrieving info for ${it.id}. Exception: $exception" }
            it
          }
        } catch (exception: Exception) {
          logger.error { "An exception occurred while retrieving info for ${it.id}, therefore crypto info might be outdated. Exception: $exception" }
          it
        }
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
