package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime

@Service
class CryptoService(
    val coingeckoService: CoingeckoService,
    val cryptoRepository: CryptoRepository,
    val clock: Clock
) {

    private val logger = KotlinLogging.logger { }

    fun retrieveCryptoInfoById(coingeckoCryptoId: String): Crypto {
        logger.info { "Retrieving crypto info for id $coingeckoCryptoId" }

        // TODO - instead of throwing an exception, maybe call coingecko api to try save the crypto (again)
        return cryptoRepository.findById(coingeckoCryptoId)
            .orElseThrow { CryptoNotFoundException(CRYPTO_NOT_FOUND) }
    }

    fun retrieveCoingeckoCryptoInfoByName(cryptoName: String): CoingeckoCrypto {
        logger.info { "Retrieving info for coingecko crypto $cryptoName" }

        return coingeckoService.retrieveAllCryptos().find { it.name.equals(cryptoName, true) }
            ?: throw CoingeckoCryptoNotFoundException(COINGECKO_CRYPTO_NOT_FOUND.format(cryptoName))
    }

    fun saveCryptoIfNotExists(coingeckoCryptoId: String) {
        val cryptoOptional = cryptoRepository.findById(coingeckoCryptoId)

        if (cryptoOptional.isEmpty) {
            val coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo(coingeckoCryptoId)

            with(coingeckoCryptoInfo) {
                val crypto = Crypto(
                    id = coingeckoCryptoId,
                    name = name,
                    ticker = symbol,
                    lastKnownPrice = marketData.currentPrice.usd,
                    lastKnownPriceInEUR = marketData.currentPrice.eur,
                    lastKnownPriceInBTC = marketData.currentPrice.btc,
                    circulatingSupply = marketData.circulatingSupply,
                    maxSupply = marketData.maxSupply ?: BigDecimal.ZERO,
                    lastUpdatedAt = LocalDateTime.now(clock)
                )

                cryptoRepository.save(crypto)
                logger.info { "Saved crypto $crypto" }
            }
        }
    }
}

class CryptoNotFoundException(message: String) : RuntimeException(message)
class CoingeckoCryptoNotFoundException(message: String) : RuntimeException(message)