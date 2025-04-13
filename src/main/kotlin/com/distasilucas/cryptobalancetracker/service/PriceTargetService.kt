package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_RESPONSE_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.PRICE_TARGET_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import com.distasilucas.cryptobalancetracker.repository.PriceTargetRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class PriceTargetService(
  private val priceTargetRepository: PriceTargetRepository,
  private val cryptoService: CryptoService,
  private val cacheService: CacheService,
  private val _priceTargetService: PriceTargetService?
) {

  private val logger = KotlinLogging.logger { }

  @Cacheable(cacheNames = [PRICE_TARGET_ID_CACHE], key = "#priceTargetId")
  fun findById(priceTargetId: String): PriceTarget {
    return priceTargetRepository.findById(priceTargetId)
      .orElseThrow { PriceTargetNotFoundException("Price target with id $priceTargetId not found") }
  }

  @Cacheable(cacheNames = [PRICE_TARGET_RESPONSE_ID_CACHE], key = "#priceTargetId")
  fun retrievePriceTarget(priceTargetId: String): PriceTargetResponse {
    logger.info { "Retrieving price target for id $priceTargetId" }

    val priceTarget = _priceTargetService!!.findById(priceTargetId)
    val crypto = cryptoService.retrieveCryptoInfoById(priceTarget.coingeckoCryptoId)
    val changeNeeded = priceTarget.calculateChangeNeeded(crypto.lastKnownPrice)
    val cryptoInfo = crypto.toCryptoInfo()

    return priceTarget.toPriceTargetResponse(cryptoInfo, crypto.lastKnownPrice, changeNeeded)
  }

  @Cacheable(cacheNames = [PRICE_TARGET_RESPONSE_PAGE_CACHE], key = "#page")
  fun retrievePriceTargetsByPage(page: Int): PagePriceTargetResponse {
    logger.info { "Retrieving price targets for page $page" }

    val pageRequest = PageRequest.of(page, 10)
    val priceTargets = priceTargetRepository.findAll(pageRequest)
    val priceTargetsResponse = priceTargets.content.map {
      val crypto = cryptoService.retrieveCryptoInfoById(it.coingeckoCryptoId)
      val cryptoInfo = crypto.toCryptoInfo()
      it.toPriceTargetResponse(cryptoInfo, crypto.lastKnownPrice, it.calculateChangeNeeded(crypto.lastKnownPrice))
    }.toList()

    return PagePriceTargetResponse(page, priceTargets.totalPages, priceTargetsResponse)
  }

  fun savePriceTarget(priceTargetRequest: PriceTargetRequest): PriceTargetResponse {
    logger.info { "Saving price target $priceTargetRequest" }

    val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId!!)
    validatePriceTargetIsNotDuplicated(coingeckoCrypto.id, priceTargetRequest.priceTarget!!)
    val crypto = cryptoService.retrieveCryptoInfoById(coingeckoCrypto.id)
    val cryptoInfo = crypto.toCryptoInfo()
    val priceTarget = priceTargetRepository.save(priceTargetRequest.toEntity(crypto.id))
    cacheService.invalidate(CacheType.PRICE_TARGETS_CACHES)

    return priceTarget.toPriceTargetResponse(cryptoInfo, crypto.lastKnownPrice, priceTarget.calculateChangeNeeded(crypto.lastKnownPrice))
  }

  fun updatePriceTarget(priceTargetId: String, priceTargetRequest: PriceTargetRequest): PriceTargetResponse {
    logger.info { "Updating price target for id $priceTargetId. New value: $priceTargetRequest" }

    val priceTarget = _priceTargetService!!.findById(priceTargetId).copy(target = priceTargetRequest.priceTarget!!)
    validatePriceTargetIsNotDuplicated(priceTarget.coingeckoCryptoId, priceTargetRequest.priceTarget)
    val crypto = cryptoService.retrieveCryptoInfoById(priceTarget.coingeckoCryptoId)
    val cryptoInfo = crypto.toCryptoInfo()
    val changeNeeded = priceTarget.calculateChangeNeeded(crypto.lastKnownPrice)
    val newPriceTarget = priceTargetRepository.save(priceTarget)
    cacheService.invalidate(CacheType.PRICE_TARGETS_CACHES)

    return newPriceTarget.toPriceTargetResponse(cryptoInfo, crypto.lastKnownPrice, changeNeeded)
  }

  fun deletePriceTarget(priceTargetId: String) {
    logger.info { "Deleting price target for id $priceTargetId" }

    val priceTarget = _priceTargetService!!.findById(priceTargetId)

    priceTargetRepository.delete(priceTarget)
    cacheService.invalidate(CacheType.PRICE_TARGETS_CACHES)
    cryptoService.deleteCryptoIfNotUsed(priceTarget.coingeckoCryptoId)
  }

  private fun validatePriceTargetIsNotDuplicated(coingeckoCryptoId: String, priceTarget: BigDecimal) {
    val optionalPriceTarget = priceTargetRepository.findByCoingeckoCryptoIdAndTarget(coingeckoCryptoId, priceTarget)

    if (optionalPriceTarget.isPresent)
      throw DuplicatedPriceTargetException("You already have a price target for $coingeckoCryptoId at that price")
  }
}

class PriceTargetNotFoundException(message: String) : RuntimeException(message)
class DuplicatedPriceTargetException(message: String) : RuntimeException(message)
