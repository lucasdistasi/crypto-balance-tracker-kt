package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import com.distasilucas.cryptobalancetracker.repository.PriceTargetRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PriceTargetService(
  private val priceTargetRepository: PriceTargetRepository,
  private val cryptoService: CryptoService
) {

  private val logger = KotlinLogging.logger { }

  fun retrievePriceTarget(priceTargetId: String): PriceTargetResponse {
    logger.info { "Retrieving price target for id $priceTargetId" }

    val priceTarget = findById(priceTargetId)
    val crypto = cryptoService.retrieveCryptoInfoById(priceTarget.coingeckoCryptoId)
    val changeNeeded = priceTarget.calculateChangeNeeded(crypto.lastKnownPrice)

    return priceTarget.toPriceTargetResponse(crypto.name, crypto.lastKnownPrice, changeNeeded)
  }

  fun retrievePriceTargetsByPage(page: Int): PagePriceTargetResponse {
    logger.info { "Retrieving price targets for page $page" }

    val pageRequest = PageRequest.of(page, 10)
    val priceTargets = priceTargetRepository.findAll(pageRequest)
    val priceTargetsResponse = priceTargets.content.map {
      val crypto = cryptoService.retrieveCryptoInfoById(it.coingeckoCryptoId)
      it.toPriceTargetResponse(crypto.name, crypto.lastKnownPrice, it.calculateChangeNeeded(crypto.lastKnownPrice))
    }.toList()

    return PagePriceTargetResponse(page, priceTargets.totalPages, priceTargetsResponse)
  }

  fun savePriceTarget(priceTargetRequest: PriceTargetRequest): PriceTargetResponse {
    logger.info { "Saving price target $priceTargetRequest" }

    val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId!!)
    validatePriceTargetIsNotDuplicated(coingeckoCrypto.id, priceTargetRequest.priceTarget!!)
    val crypto = cryptoService.retrieveCryptoInfoById(coingeckoCrypto.id)
    val priceTarget = priceTargetRepository.save(priceTargetRequest.toEntity(crypto.id))

    return priceTarget.toPriceTargetResponse(crypto.name, crypto.lastKnownPrice, priceTarget.calculateChangeNeeded(crypto.lastKnownPrice))
  }

  fun updatePriceTarget(priceTargetId: String, priceTargetRequest: PriceTargetRequest): PriceTargetResponse {
    logger.info { "Updating price target for id $priceTargetId. New value: $priceTargetRequest" }

    val priceTarget = findById(priceTargetId).copy(target = priceTargetRequest.priceTarget!!)
    validatePriceTargetIsNotDuplicated(priceTarget.coingeckoCryptoId, priceTargetRequest.priceTarget)
    val crypto = cryptoService.retrieveCryptoInfoById(priceTarget.coingeckoCryptoId)
    val changeNeeded = priceTarget.calculateChangeNeeded(crypto.lastKnownPrice)
    val newPriceTarget = priceTargetRepository.save(priceTarget)

    return newPriceTarget.toPriceTargetResponse(crypto.name, crypto.lastKnownPrice, changeNeeded)
  }

  fun deletePriceTarget(priceTargetId: String) {
    logger.info { "Deleting price target for id $priceTargetId" }

    val priceTarget = findById(priceTargetId)

    priceTargetRepository.delete(priceTarget)
    cryptoService.deleteCryptoIfNotUsed(priceTarget.coingeckoCryptoId)
  }

  private fun findById(priceTargetId: String): PriceTarget {
    return priceTargetRepository.findById(priceTargetId)
      .orElseThrow { PriceTargetNotFoundException("Price target with id $priceTargetId not found") }
  }

  private fun validatePriceTargetIsNotDuplicated(coingeckoCryptoId: String, priceTarget: BigDecimal) {
    val optionalPriceTarget = priceTargetRepository.findByCoingeckoCryptoIdAndTarget(coingeckoCryptoId, priceTarget)

    if (optionalPriceTarget.isPresent)
      throw DuplicatedPriceTargetException("You already have a price target for $coingeckoCryptoId at that price")
  }
}

class PriceTargetNotFoundException(message: String) : RuntimeException(message)
class DuplicatedPriceTargetException(message: String) : RuntimeException(message)
