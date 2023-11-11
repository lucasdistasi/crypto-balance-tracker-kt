package com.distasilucas.cryptobalancetracker.model.request.crypto

import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_SIZE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_DECIMAL_MAX
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_DIGITS
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_POSITIVE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.validation.ValidCryptoName
import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.UUID
import java.math.BigDecimal

data class UserCryptoRequest(
    @field: NotBlank(message = CRYPTO_NAME_NOT_BLANK)
    @field: Size(min = 1, max = 64, message = CRYPTO_NAME_SIZE)
    @field: ValidCryptoName
    val cryptoName: String?,

    @field: NotNull(message = CRYPTO_QUANTITY_NOT_NULL)
    @field: Digits(
        integer = 16,
        fraction = 12,
        message = CRYPTO_QUANTITY_DIGITS
    )
    @field: DecimalMax(
        value = "9999999999999999.999999999999",
        message = CRYPTO_QUANTITY_DECIMAL_MAX
    )
    @field: Positive(message = CRYPTO_QUANTITY_POSITIVE)
    val quantity: BigDecimal?,

    @field: NotBlank(message = PLATFORM_ID_NOT_BLANK)
    @field: UUID(message = PLATFORM_ID_UUID)
    val platformId: String?
) {

    fun toEntity(coingeckoCryptoId: String) = UserCrypto(
        coingeckoCryptoId = coingeckoCryptoId,
        quantity = quantity!!,
        platformId = platformId!!
    )
}