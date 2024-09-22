package com.distasilucas.cryptobalancetracker.model.request.transaction

import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_DECIMAL_MAX
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_DIGITS
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_POSITIVE
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_DATE_NOT_FUTURE
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_DATE_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_PLATFORM_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_TYPE_NOT_NULL
import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.entity.TransactionType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KClass

data class TransactionRequest(

  @field: ValidTicker
  val ticker: String?,

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

  @field: NotNull(message = "Price can not be null")
  @field: Digits(
    integer = 16,
    fraction = 12,
    message = "Price must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
  )
  @field: DecimalMax(
    value = "9999999999999999.999999999999",
    message = "Price must be less than or equal to 9999999999999999.999999999999"
  )
  @field: Positive(message = "Price must be greater than 0")
  val price: BigDecimal?,

  @field: NotNull(message = TRANSACTION_TYPE_NOT_NULL)
  val transactionType: TransactionType?,

  @field: NotBlank(message = TRANSACTION_PLATFORM_NOT_BLANK)
  @field: Size(min = 1, max = 100, message = "Platform must be between {min} and {max} characters")
  val platform: String?,

  @field: NotNull(message = TRANSACTION_DATE_NOT_NULL)
  @field: PastOrPresent(message = TRANSACTION_DATE_NOT_FUTURE)
  @field: Schema(example = "2024-05-13", pattern = "yyyy-MM-dd", required = true)
  val date: LocalDate?
) {

  fun toTransactionEntity(transactionId: String = UUID.randomUUID().toString()): Transaction {
    return Transaction(
      id = transactionId,
      cryptoTicker = ticker!!.uppercase(),
      quantity = quantity!!,
      price = price!!,
      transactionType = transactionType!!,
      platform = platform!!.uppercase(),
      date = date.toString(),
    )
  }
}

@Constraint(validatedBy = [CryptoTickerValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidTicker(
  val message: String = "Crypto ticker must have at least 1 character and no more than 15",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = []
)

class CryptoTickerValidator : ConstraintValidator<ValidTicker, String> {

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    return value?.let { value.isNotBlank() && value.length in 1..15 } ?: false
  }
}

