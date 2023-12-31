package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_CRYPTOS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository
import com.distasilucas.cryptobalancetracker.repository.GoalRepository
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime

@Service
class CryptoService(
    private val coingeckoService: CoingeckoService,
    private val cacheService: CacheService,
    private val cryptoRepository: CryptoRepository,
    private val userCryptoRepository: UserCryptoRepository,
    private val goalRepository: GoalRepository,
    private val clock: Clock
) {

    private val logger = KotlinLogging.logger { }

    @Cacheable(cacheNames = [CRYPTO_COINGECKO_CRYPTO_ID_CACHE], key = "#coingeckoCryptoId")
    fun retrieveCryptoInfoById(coingeckoCryptoId: String): Crypto {
        logger.info { "Retrieving crypto info for id $coingeckoCryptoId" }

        val cryptoOptional = cryptoRepository.findById(coingeckoCryptoId)

        if (cryptoOptional.isPresent) {
            return cryptoOptional.get()
        }

        val coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo(coingeckoCryptoId)

        val cryptoToSave = Crypto(
            id = coingeckoCryptoId,
            name = coingeckoCryptoInfo.name,
            ticker = coingeckoCryptoInfo.symbol,
            image = coingeckoCryptoInfo.image.large,
            lastKnownPrice = coingeckoCryptoInfo.marketData.currentPrice.usd,
            lastKnownPriceInEUR = coingeckoCryptoInfo.marketData.currentPrice.eur,
            lastKnownPriceInBTC = coingeckoCryptoInfo.marketData.currentPrice.btc,
            circulatingSupply = coingeckoCryptoInfo.marketData.circulatingSupply,
            maxSupply = coingeckoCryptoInfo.marketData.maxSupply ?: BigDecimal.ZERO,
            lastUpdatedAt = LocalDateTime.now(clock)
        )

        logger.info { "Saved crypto $cryptoToSave because it didn't exist" }

        return cryptoRepository.save(cryptoToSave)
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
                    image = image.large,
                    lastKnownPrice = marketData.currentPrice.usd,
                    lastKnownPriceInEUR = marketData.currentPrice.eur,
                    lastKnownPriceInBTC = marketData.currentPrice.btc,
                    circulatingSupply = marketData.circulatingSupply,
                    maxSupply = marketData.maxSupply ?: BigDecimal.ZERO,
                    lastUpdatedAt = LocalDateTime.now(clock)
                )

                cryptoRepository.save(crypto)
                cacheService.invalidateCryptosCache()
                logger.info { "Saved crypto $crypto" }
            }
        }
    }

    fun deleteCryptoIfNotUsed(coingeckoCryptoId: String) {
        val userCryptos = userCryptoRepository.findAllByCoingeckoCryptoId(coingeckoCryptoId)

        if (userCryptos.isEmpty()) {
            val goalOptional = goalRepository.findByCoingeckoCryptoId(coingeckoCryptoId)

            if (goalOptional.isEmpty) {
                cryptoRepository.deleteById(coingeckoCryptoId)
                cacheService.invalidateCryptosCache()
                logger.info { "Deleted crypto $coingeckoCryptoId because it was not used" }
            }
        }
    }

    fun findOldestNCryptosByLastPriceUpdate(dateFilter: LocalDateTime, limit: Int): List<Crypto> {
        return cryptoRepository.findOldestNCryptosByLastPriceUpdate(dateFilter, limit)
    }

    fun updateCryptos(cryptosToUpdate: List<Crypto>) {
        cryptoRepository.saveAll(cryptosToUpdate)
        logger.info { "Updated cryptos ${cryptosToUpdate.map { it.name }}" }
    }

    @Cacheable(cacheNames = [CRYPTOS_CRYPTOS_IDS_CACHE], key = "#ids")
    fun findAllByIds(ids: Collection<String>): List<Crypto> {
        logger.info { "Retrieving cryptos with ids $ids" }

        return cryptoRepository.findAllByIdIn(ids)
    }
}

class CoingeckoCryptoNotFoundException(message: String) : RuntimeException(message)