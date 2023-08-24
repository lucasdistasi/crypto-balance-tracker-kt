package com.distasilucas.cryptobalancetracker.model.request.crypto

import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.validation.ValidCryptoName
import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.UUID
import java.math.BigDecimal

data class UserCryptoRequest(
    @field: NotBlank(message = "Crypto name can not be null or blank")
    @field: Size(min = 1, max = 64, message = "Crypto name must be between 1 and 64 characters")
    @field: ValidCryptoName
    val cryptoName: String?,

    @field: NotNull(message = "Crypto quantity can not be null")
    @field: Digits(
        integer = 16,
        fraction = 12,
        message = "Crypto quantity must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
    )
    @field: DecimalMax(
        value = "9999999999999999.999999999999",
        message = "Crypto quantity must be less than or equal to 9999999999999999.999999999999"
    )
    @field: Positive(message = "Crypto quantity must be greater than 0")
    val quantity: BigDecimal?,

    @field: NotBlank(message = "Platform id can not be null or blank")
    @field: UUID(message = "Platform id must be a valid UUID")
    val platformId: String?
) {

    fun toEntity(coingeckoCryptoId: String) = UserCrypto(
        coingeckoCryptoId = coingeckoCryptoId,
        quantity = quantity!!,
        platformId = platformId!!
    )
}