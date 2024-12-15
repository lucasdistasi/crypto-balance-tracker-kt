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

@Constraint(validatedBy = [ValidCryptoNameOrIdValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidCryptoNameOrId(
  val message: String = "Invalid crypto name or id. Must contain at least 1 not blank character and no more than 64",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = []
)

class ValidCryptoNameOrIdValidator : ConstraintValidator<ValidCryptoNameOrId, String> {

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    return value?.let { value.isNotBlank() && value.length in 1..64 } ?: false
  }
}
