package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.NOT_ENOUGH_BALANCE
import com.distasilucas.cryptobalancetracker.constants.SAME_FROM_TO_PLATFORM
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
        val quantityToTransfer = transferCryptoRequest.quantityToTransfer!!
        val totalToSubtract = transferCryptoRequest.calculateTotalToSubtract()

        if (availableQuantity.minus(totalToSubtract) < BigDecimal.ZERO) {
            throw InsufficientBalanceException(NOT_ENOUGH_BALANCE)
        }

        val toPlatformOptionalUserCrypto = userCryptoService.findByCoingeckoCryptoIdAndPlatformId(
            userCryptoToTransfer.coingeckoCryptoId,
            transferCryptoRequest.toPlatformId
        )
        val remainingCryptoQuantity = transferCryptoRequest.calculateRemainingCryptoQuantity(availableQuantity)

        var transferCryptoResponse: TransferCryptoResponse? = null

        if (doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isPresent) {
            val quantityToSendReceive = transferCryptoRequest.calculateQuantityToSendReceive(remainingCryptoQuantity)
            val toPlatformUserCrypto = toPlatformOptionalUserCrypto.get()
            val newQuantity = toPlatformUserCrypto.quantity.add(quantityToSendReceive)

            val updatedFromPlatformUserCrypto = userCryptoToTransfer.copy(quantity = remainingCryptoQuantity)
            val updatedToPlatformUserCrypto = toPlatformUserCrypto.copy(quantity = newQuantity)

            userCryptoService.saveOrUpdateAll(listOf(updatedFromPlatformUserCrypto, updatedToPlatformUserCrypto))

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                remainingCryptoQuantity = remainingCryptoQuantity,
                newQuantity = newQuantity
            )
        }

        if (doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isEmpty) {
            val uuid = UUID.randomUUID().toString()
            val quantityToSendReceive = transferCryptoRequest.calculateQuantityToSendReceive(remainingCryptoQuantity)
            val toPlatformUserCrypto = userCryptoToTransfer.copy(
                id = uuid,
                quantity = quantityToSendReceive,
                platformId = transferCryptoRequest.toPlatformId
            )
            val updatedUserCryptoToTransfer = userCryptoToTransfer.copy(quantity = remainingCryptoQuantity)

            userCryptoService.saveOrUpdateAll(listOf(updatedUserCryptoToTransfer, toPlatformUserCrypto))

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                remainingCryptoQuantity = remainingCryptoQuantity,
                newQuantity = quantityToSendReceive
            )
        }

        if (!doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isPresent) {
            val toPlatformUserCrypto = toPlatformOptionalUserCrypto.get()
            val quantityToSendReceive = transferCryptoRequest.calculateQuantityToSendReceive(remainingCryptoQuantity)
            val newQuantity = toPlatformUserCrypto.quantity.add(quantityToSendReceive)

            val updatedToPlatformUserCrypto = toPlatformUserCrypto.copy(quantity = newQuantity)

            userCryptoService.deleteUserCrypto(userCryptoToTransfer.id)
            userCryptoService.saveOrUpdateAll(listOf(updatedToPlatformUserCrypto))

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                remainingCryptoQuantity = remainingCryptoQuantity,
                newQuantity = newQuantity
            )
        }

        if (!doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isEmpty) {
            val quantityToSendReceive = transferCryptoRequest.calculateQuantityToSendReceive(remainingCryptoQuantity)
            val updatedFromPlatformUserCrypto = userCryptoToTransfer.copy(
                quantity = quantityToSendReceive,
                platformId = toPlatformResponse.id
            )

            userCryptoService.saveOrUpdateAll(listOf(updatedFromPlatformUserCrypto))

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                remainingCryptoQuantity = remainingCryptoQuantity,
                newQuantity = quantityToSendReceive
            )
        }

        logger.info { "Transferred $quantityToTransfer of ${userCryptoToTransfer.coingeckoCryptoId} from platform ${fromPlatformResponse.name} to ${toPlatformResponse.name}" }

        return transferCryptoResponse!!
    }

    private fun isToAndFromSamePlatform(toPlatformId: String, fromPlatformId: String) = toPlatformId == fromPlatformId

    private fun doesFromPlatformHaveRemaining(remainingCryptoQuantity: BigDecimal) =
        remainingCryptoQuantity > BigDecimal.ZERO
}

class InsufficientBalanceException(message: String) : RuntimeException(message)