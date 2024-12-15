package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_CRYPTOS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository
import com.distasilucas.cryptobalancetracker.repository.GoalRepository
import com.distasilucas.cryptobalancetracker.repository.PriceTargetRepository
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

@Service
class CryptoService(
  private val coingeckoService: CoingeckoService,
  private val cacheService: CacheService,
  private val orphanCryptoService: OrphanCryptoService,
  private val cryptoRepository: CryptoRepository,
  private val clock: Clock
) {

  private val logger = KotlinLogging.logger { }

  @Cacheable(cacheNames = [CRYPTO_COINGECKO_CRYPTO_ID_CACHE], key = "#coingeckoCryptoId")
  fun retrieveCryptoInfoById(coingeckoCryptoId: String): Crypto {
    logger.info { "Retrieving crypto info for id $coingeckoCryptoId" }

    return cryptoRepository.findById(coingeckoCryptoId)
      .orElseGet {
        val crypto = getCrypto(coingeckoCryptoId)
        cacheService.invalidate(CacheType.CRYPTOS_CACHES)
        logger.info { "Saved crypto $crypto because it didn't exist" }
        cryptoRepository.save(crypto)
      }
  }

  @Cacheable(cacheNames = [CRYPTOS_CRYPTOS_IDS_CACHE], key = "#ids")
  fun findAllByIds(ids: Collection<String>): List<Crypto> {
    logger.info { "Retrieving cryptos with ids $ids" }

    return cryptoRepository.findAllByIdIn(ids)
  }

  fun retrieveCoingeckoCryptoInfoByNameOrId(cryptoNameOrId: String): CoingeckoCrypto {
    logger.info { "Retrieving info for coingecko crypto $cryptoNameOrId" }

    return coingeckoService.retrieveAllCryptos()
      .find { it.id.equals(cryptoNameOrId, true) || it.name.equals(cryptoNameOrId, true) }
      ?: throw CoingeckoCryptoNotFoundException(COINGECKO_CRYPTO_NOT_FOUND.format(cryptoNameOrId))
  }

  fun findOldestNCryptosByLastPriceUpdate(dateFilter: LocalDateTime, limit: Int) =
    cryptoRepository.findOldestNCryptosByLastPriceUpdate(dateFilter, limit)

  fun saveCryptoIfNotExists(coingeckoCryptoId: String) {
    val cryptoOptional = cryptoRepository.findById(coingeckoCryptoId)

    if (cryptoOptional.isEmpty) {
      val crypto = getCrypto(coingeckoCryptoId)
      cryptoRepository.save(crypto)
      cacheService.invalidate(CacheType.CRYPTOS_CACHES)
      logger.info { "Saved crypto $crypto" }
    }
  }

  fun deleteCryptoIfNotUsed(coingeckoCryptoId: String) {
    if (orphanCryptoService.isCryptoOrphan(coingeckoCryptoId)) {
      cryptoRepository.deleteById(coingeckoCryptoId)
      cacheService.invalidate(CacheType.CRYPTOS_CACHES)
      logger.info { "Deleted crypto [$coingeckoCryptoId] because it was not used" }
    }
  }

  fun deleteCryptosIfNotUsed(coingeckoCryptoIds: List<String>) {
    val orphanCryptos = orphanCryptoService.getOrphanCryptos(coingeckoCryptoIds)

    if (orphanCryptos.isNotEmpty()) {
      cryptoRepository.deleteAllById(orphanCryptos)
      cacheService.invalidate(CacheType.CRYPTOS_CACHES)
      logger.info { "Deleted cryptos $coingeckoCryptoIds because they were not used" }
    }
  }

  fun updateCryptos(cryptosToUpdate: List<Crypto>) {
    cryptoRepository.saveAll(cryptosToUpdate)
    logger.info { "Updated cryptos ${cryptosToUpdate.map { it.name }}" }
  }

  private fun getCrypto(coingeckoCryptoId: String): Crypto {
    val coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo(coingeckoCryptoId)

    return coingeckoCryptoInfo.toCrypto(clock)
  }
}

class CoingeckoCryptoNotFoundException(message: String) : RuntimeException(message)

@Service
class OrphanCryptoService(
  private val userCryptoRepository: UserCryptoRepository,
  private val goalRepository: GoalRepository,
  private val priceTargetRepository: PriceTargetRepository
) {

  fun isCryptoOrphan(coingeckoCryptoId: String): Boolean {
    return userCryptoRepository.findAllByCoingeckoCryptoId(coingeckoCryptoId).isEmpty() &&
      goalRepository.findByCoingeckoCryptoId(coingeckoCryptoId).isEmpty &&
      priceTargetRepository.findAllByCoingeckoCryptoId(coingeckoCryptoId).isEmpty()
  }

  fun getOrphanCryptos(coingeckoCryptoId: List<String>) = coingeckoCryptoId.filter { isCryptoOrphan(it) }
}
