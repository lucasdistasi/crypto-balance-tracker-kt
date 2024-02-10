package com.distasilucas.cryptobalancetracker.model.request.goal

import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_SIZE
import com.distasilucas.cryptobalancetracker.entity.Goal
import com.distasilucas.cryptobalancetracker.validation.ValidCryptoName
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class GoalRequest(
  @field: NotBlank(message = CRYPTO_NAME_NOT_BLANK)
  @field: Size(min = 1, max = 64, message = CRYPTO_NAME_SIZE)
  @field: ValidCryptoName
  val cryptoName: String?,

  @field: NotNull(message = "Goal quantity can not be null")
  @field: Digits(
    integer = 16,
    fraction = 12,
    message = "Goal quantity must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
  )
  @field: DecimalMax(
    value = "9999999999999999.999999999999",
    message = "Goal quantity must be less than or equal to 9999999999999999.999999999999"
  )
  @field: Positive(message = "Goal quantity must be greater than 0")
  val goalQuantity: BigDecimal?,
) {

  fun toEntity(cryptoId: String) = Goal(
    coingeckoCryptoId = cryptoId,
    goalQuantity = goalQuantity!!
  )

}
