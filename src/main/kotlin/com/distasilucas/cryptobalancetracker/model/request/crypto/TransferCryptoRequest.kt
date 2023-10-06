package com.distasilucas.cryptobalancetracker.model.request.crypto

import com.distasilucas.cryptobalancetracker.constants.TO_PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.constants.NETWORK_FEE_DIGITS
import com.distasilucas.cryptobalancetracker.constants.NETWORK_FEE_MIN
import com.distasilucas.cryptobalancetracker.constants.NETWORK_FEE_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.QUANTITY_TO_TRANSFER_DECIMAL_MAX
import com.distasilucas.cryptobalancetracker.constants.QUANTITY_TO_TRANSFER_DIGITS
import com.distasilucas.cryptobalancetracker.constants.QUANTITY_TO_TRANSFER_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.QUANTITY_TO_TRANSFER_POSITIVE
import com.distasilucas.cryptobalancetracker.constants.TO_PLATFORM_ID_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_UUID
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_BLANK
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
    @field: NotBlank(message = USER_CRYPTO_ID_NOT_BLANK)
    @field: UUID(message = USER_CRYPTO_ID_UUID)
    val userCryptoId: String?,

    @field: NotNull(message = QUANTITY_TO_TRANSFER_NOT_NULL)
    @field: Digits(
        integer = 16,
        fraction = 12,
        message = QUANTITY_TO_TRANSFER_DIGITS
    )
    @field: DecimalMax(
        value = "9999999999999999.999999999999",
        message = QUANTITY_TO_TRANSFER_DECIMAL_MAX
    )
    @field: Positive(message = QUANTITY_TO_TRANSFER_POSITIVE)
    val quantityToTransfer: BigDecimal?,

    @field: NotNull(message = NETWORK_FEE_NOT_NULL)
    @field: Digits(
        integer = 16,
        fraction = 12,
        message = NETWORK_FEE_DIGITS
    )
    @field: Min(value = 0, message = NETWORK_FEE_MIN)
    val networkFee: BigDecimal?,

    val sendFullQuantity: Boolean? = false,

    @field: NotBlank(message = TO_PLATFORM_ID_NOT_BLANK)
    @field: UUID(message = TO_PLATFORM_ID_UUID)
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