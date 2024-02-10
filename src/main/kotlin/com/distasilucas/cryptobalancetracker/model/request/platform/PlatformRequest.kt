package com.distasilucas.cryptobalancetracker.model.request.platform

import com.distasilucas.cryptobalancetracker.constants.PLATFORM_NAME_REGEX
import com.distasilucas.cryptobalancetracker.entity.Platform
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.constraints.NotBlank
import java.util.UUID
import kotlin.reflect.KClass

data class PlatformRequest(
  @field: NotBlank(message = "Platform name cannot be null or blank")
  @field: ValidPlatformName
  val name: String?
) {

  fun toEntity(id: String = UUID.randomUUID().toString()): Platform {
    return Platform(id, name = name!!.uppercase())
  }
}

@Constraint(validatedBy = [PlatformNameValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidPlatformName(
  val message: String = "Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = []
)

class PlatformNameValidator : ConstraintValidator<ValidPlatformName, String> {

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    val regex = PLATFORM_NAME_REGEX.toRegex()

    return value?.let { value.matches(regex) } ?: false
  }
}
