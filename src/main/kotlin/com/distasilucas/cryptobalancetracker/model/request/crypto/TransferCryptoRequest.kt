package com.distasilucas.cryptobalancetracker.model.request.crypto

import com.distasilucas.cryptobalancetracker.constants.INVALID_TO_PLATFORM_UUID
import com.distasilucas.cryptobalancetracker.constants.INVALID_USER_CRYPTO_UUID
import com.distasilucas.cryptobalancetracker.model.response.crypto.FromPlatform
import com.distasilucas.cryptobalancetracker.model.response.crypto.ToPlatform
import com.distasilucas.cryptobalancetracker.model.response.crypto.TransferCryptoResponse
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.UUID
import java.math.BigDecimal

data class TransferCryptoRequest(
    @field: NotBlank(message = "User crypto id can not be null or blank")
    @field: UUID(message = INVALID_USER_CRYPTO_UUID)
    val userCryptoId: String?,

    @field: NotNull(message = "Quantity to transfer can not be null")
    @field: Digits(
        integer = 16,
        fraction = 12,
        message = "Quantity to transfer must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
    )
    @field: DecimalMax(
        value = "9999999999999999.999999999999",
        message = "Quantity to transfer must be less than or equal to 9999999999999999.999999999999"
    )
    @field: Positive(message = "Quantity to transfer must be greater than 0")
    val quantityToTransfer: BigDecimal?,

    @field: NotNull(message = "Network fee can not be null")
    @field: Digits(
        integer = 16,
        fraction = 12,
        message = "Network fee must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
    )
    @field: Min(value = 0, message = "Network fee must be greater than or equal to 0")
    val networkFee: BigDecimal?,

    val sendFullQuantity: Boolean? = false,

    @field: NotBlank(message = "To platform id can not be null or blank")
    @field: UUID(message = INVALID_TO_PLATFORM_UUID)
    val toPlatformId: String?
) {

    fun toTransferCryptoResponse(
        remainingCryptoQuantity: BigDecimal,
        newQuantity: BigDecimal
    ): TransferCryptoResponse {
        return TransferCryptoResponse(
            fromPlatform = FromPlatform(
                userCryptoId = userCryptoId!!,
                networkFee = networkFee!!.toPlainString(),
                quantityToTransfer = quantityToTransfer!!.toPlainString(),
                totalToSubtract = calculateTotalToSubtract().toPlainString(),
                quantityToSendReceive = calculateQuantityToSendReceive(remainingCryptoQuantity).toPlainString(),
                remainingCryptoQuantity = remainingCryptoQuantity.toPlainString(),
                sendFullQuantity = sendFullQuantity!!
            ),
            toPlatform = ToPlatform(
                platformId = toPlatformId!!,
                newQuantity = newQuantity.toPlainString()
            )
        )
    }

    fun calculateTotalToSubtract(): BigDecimal {
        return if (sendFullQuantity == true) {
            quantityToTransfer!!.add(networkFee)
        } else {
            quantityToTransfer!!
        }
    }

    fun calculateQuantityToSendReceive(remainingCryptoQuantity: BigDecimal): BigDecimal {
        // If there is no remaining in from platform, it does not matter if full quantity is true or false.
        if (remainingCryptoQuantity == BigDecimal.ZERO) {
            return quantityToTransfer!!.minus(networkFee!!)
        }

        return if (sendFullQuantity == true)
            quantityToTransfer!! else quantityToTransfer!!.minus(networkFee!!)
    }

    fun calculateRemainingCryptoQuantity(
        availableQuantity: BigDecimal
    ): BigDecimal {
        return if (sendFullQuantity == true) {
            val totalToSubtract = calculateTotalToSubtract()
            calculateRemainingCryptoQuantity(availableQuantity, totalToSubtract)
        } else {
            calculateRemainingCryptoQuantity(availableQuantity, quantityToTransfer!!)
        }
    }

    private fun calculateRemainingCryptoQuantity(
        availableQuantity: BigDecimal,
        totalToSubtract: BigDecimal
    ): BigDecimal {
        val remaining = availableQuantity.minus(totalToSubtract)

        // Sometimes remaining comes as '0E-9', so I need to strip trailing zeros to see if it's zero
        return if (remaining.stripTrailingZeros() == BigDecimal.ZERO) return BigDecimal.ZERO else remaining
    }
}