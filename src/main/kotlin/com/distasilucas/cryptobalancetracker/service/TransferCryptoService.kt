package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.NOT_ENOUGH_BALANCE
import com.distasilucas.cryptobalancetracker.constants.SAME_FROM_TO_PLATFORM
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.request.crypto.TransferCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.TransferCryptoResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class TransferCryptoService(
  private val userCryptoService: UserCryptoService,
  private val platformService: PlatformService
) {

  private val logger = KotlinLogging.logger { }

  fun transferCrypto(transferCryptoRequest: TransferCryptoRequest): TransferCryptoResponse {
    val toPlatformResponse = platformService.retrievePlatformById(transferCryptoRequest.toPlatformId!!)
    val userCryptoToTransfer = userCryptoService.findByUserCryptoId(transferCryptoRequest.userCryptoId!!)
    val fromPlatformResponse = platformService.retrievePlatformById(userCryptoToTransfer.platformId)

    if (isToAndFromSamePlatform(toPlatformResponse.id, fromPlatformResponse.id)) {
      throw ApiException(HttpStatus.BAD_REQUEST, SAME_FROM_TO_PLATFORM)
    }

    val availableQuantity = userCryptoToTransfer.quantity
    val quantityToTransfer = transferCryptoRequest.quantityToTransfer

    if (transferCryptoRequest.hasInsufficientBalance(availableQuantity)) {
      throw InsufficientBalanceException(NOT_ENOUGH_BALANCE)
    }

    val remainingCryptoQuantity = transferCryptoRequest.calculateRemainingCryptoQuantity(availableQuantity)
    val quantityToSendReceive = transferCryptoRequest.calculateQuantityToSendReceive(remainingCryptoQuantity, availableQuantity)
    val toPlatformUserCrypto = userCryptoService.findByCoingeckoCryptoIdAndPlatformId(
      userCryptoToTransfer.coingeckoCryptoId,
      transferCryptoRequest.toPlatformId
    )

    var transferCryptoResponse: TransferCryptoResponse? = null

    if (doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformUserCrypto != null) {
      val newQuantity = toPlatformUserCrypto.quantity.add(quantityToSendReceive)
      val updatedFromPlatformUserCrypto = userCryptoToTransfer.copy(quantity = remainingCryptoQuantity)
      val updatedToPlatformUserCrypto = toPlatformUserCrypto.copy(quantity = newQuantity)

      userCryptoService.saveOrUpdateAll(listOf(updatedFromPlatformUserCrypto, updatedToPlatformUserCrypto))

      transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
        remainingCryptoQuantity,
        newQuantity,
        quantityToSendReceive
      )
    }

    if (doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformUserCrypto == null) {
      val uuid = UUID.randomUUID().toString()
      val updatedToPlatformUserCrypto = UserCrypto(
        uuid,
        userCryptoToTransfer.coingeckoCryptoId,
        quantityToSendReceive,
        transferCryptoRequest.toPlatformId
      )
      val updatedUserCryptoToTransfer = userCryptoToTransfer.copy(quantity = remainingCryptoQuantity)

      if (transferCryptoRequest.sendFullQuantity == true) {
        userCryptoService.saveOrUpdateAll(listOf(updatedUserCryptoToTransfer, updatedToPlatformUserCrypto))
      } else {
        if (quantityToSendReceive > BigDecimal.ZERO) {
          userCryptoService.saveOrUpdateAll(listOf(updatedUserCryptoToTransfer, updatedToPlatformUserCrypto))
        } else {
          userCryptoService.saveOrUpdateAll(listOf(updatedUserCryptoToTransfer))
        }
      }

      transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
        remainingCryptoQuantity,
        quantityToSendReceive,
        quantityToSendReceive
      )
    }

    if (!doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformUserCrypto != null) {
      val newQuantity = toPlatformUserCrypto.quantity.add(quantityToSendReceive)
      val updatedToPlatformUserCrypto = toPlatformUserCrypto.copy(quantity = newQuantity)

      userCryptoService.deleteUserCrypto(userCryptoToTransfer.id)
      userCryptoService.saveOrUpdateAll(listOf(updatedToPlatformUserCrypto))

      transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
        remainingCryptoQuantity,
        newQuantity,
        quantityToSendReceive
      )
    }

    if (!doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformUserCrypto == null) {
      val updatedFromPlatformUserCrypto = UserCrypto(
        userCryptoToTransfer.id,
        userCryptoToTransfer.coingeckoCryptoId,
        quantityToSendReceive,
        toPlatformResponse.id
      )

      if (updatedFromPlatformUserCrypto.quantity > BigDecimal.ZERO) {
        userCryptoService.saveOrUpdateAll(listOf(updatedFromPlatformUserCrypto))
      } else {
        userCryptoService.deleteUserCrypto(updatedFromPlatformUserCrypto.id)
      }

      transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
        remainingCryptoQuantity,
        quantityToSendReceive,
        quantityToSendReceive
      )
    }

    logger.info { "Transferred $quantityToTransfer of ${userCryptoToTransfer.coingeckoCryptoId} from platform ${fromPlatformResponse.name} to ${toPlatformResponse.name}" }

    return transferCryptoResponse!!
  }

  private fun isToAndFromSamePlatform(toPlatformId: String, fromPlatformId: String) = toPlatformId == fromPlatformId

  private fun doesFromPlatformHaveRemaining(remainingCryptoQuantity: BigDecimal) = remainingCryptoQuantity > BigDecimal.ZERO
}

class InsufficientBalanceException(message: String) : RuntimeException(message)
