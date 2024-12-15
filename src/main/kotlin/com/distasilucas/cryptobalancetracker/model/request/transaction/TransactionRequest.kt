package com.distasilucas.cryptobalancetracker.model.request.transaction

import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_DECIMAL_MAX
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_DIGITS
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_POSITIVE
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_DATE_NOT_FUTURE
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_DATE_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_PLATFORM_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_PRICE_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_PRICE_POSITIVE
import com.distasilucas.cryptobalancetracker.constants.TRANSACTION_TYPE_NOT_NULL
import com.distasilucas.cryptobalancetracker.entity.Transaction
import com.distasilucas.cryptobalancetracker.entity.TransactionType
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.validation.ValidCryptoNameOrId
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class TransactionRequest(

  @field: ValidCryptoNameOrId
  val cryptoNameOrId: String?,

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

  @field: NotNull(message = TRANSACTION_PRICE_NOT_NULL)
  @field: Digits(
    integer = 16,
    fraction = 12,
    message = "Price must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
  )
  @field: DecimalMax(
    value = "9999999999999999.999999999999",
    message = "Price must be less than or equal to 9999999999999999.999999999999"
  )
  @field: Positive(message = TRANSACTION_PRICE_POSITIVE)
  val price: BigDecimal?,

  @field: NotNull(message = TRANSACTION_TYPE_NOT_NULL)
  val transactionType: TransactionType?,

  @field: NotBlank(message = TRANSACTION_PLATFORM_NOT_BLANK)
  @field: Size(min = 1, max = 24, message = "Platform must be between {min} and {max} characters")
  val platform: String?,

  @field: NotNull(message = TRANSACTION_DATE_NOT_NULL)
  @field: PastOrPresent(message = TRANSACTION_DATE_NOT_FUTURE)
  @field: Schema(example = "2024-05-13", pattern = "yyyy-MM-dd", required = true)
  val date: LocalDate?
) {

  fun toTransactionEntity(
    transactionId: String = UUID.randomUUID().toString(),
    coingeckoCrypto: CoingeckoCrypto,
  ) = Transaction(
    id = transactionId,
    coingeckoCryptoId = coingeckoCrypto.id,
    cryptoTicker = coingeckoCrypto.symbol,
    quantity = quantity!!,
    price = price!!,
    transactionType = transactionType!!,
    platform = platform!!.uppercase(),
    date = date.toString(),
  )
}
