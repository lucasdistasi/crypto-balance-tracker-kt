package com.distasilucas.cryptobalancetracker.validation

import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_REGEX
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [CryptoNameValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidCryptoName(
  val message: String = "Invalid crypto name",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = []
)

class CryptoNameValidator : ConstraintValidator<ValidCryptoName, String> {

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    val regex = CRYPTO_NAME_REGEX.toRegex()

    return value?.let { value.matches(regex) } ?: false
  }
}
