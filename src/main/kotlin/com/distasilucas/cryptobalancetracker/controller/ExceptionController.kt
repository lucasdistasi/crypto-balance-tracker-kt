package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.INVALID_VALUE_FOR
import com.distasilucas.cryptobalancetracker.constants.UNKNOWN_ERROR
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.exception.TooManyRequestsException
import com.distasilucas.cryptobalancetracker.service.CoingeckoCryptoNotFoundException
import com.distasilucas.cryptobalancetracker.service.DuplicatedCryptoPlatFormException
import com.distasilucas.cryptobalancetracker.service.DuplicatedGoalException
import com.distasilucas.cryptobalancetracker.service.DuplicatedPlatformException
import com.distasilucas.cryptobalancetracker.service.DuplicatedPriceTargetException
import com.distasilucas.cryptobalancetracker.service.GoalNotFoundException
import com.distasilucas.cryptobalancetracker.service.InsufficientBalanceException
import com.distasilucas.cryptobalancetracker.service.PlatformNotFoundException
import com.distasilucas.cryptobalancetracker.service.PriceTargetNotFoundException
import com.distasilucas.cryptobalancetracker.service.UserCryptoNotFoundException
import com.distasilucas.cryptobalancetracker.service.UsernameNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.net.URI

@RestControllerAdvice
class ExceptionController {

  private val logger = KotlinLogging.logger { }
  private val NOT_FOUND_STATUS = HttpStatus.NOT_FOUND
  private val BAD_REQUEST_STATUS = HttpStatus.BAD_REQUEST

  @ExceptionHandler(PlatformNotFoundException::class)
  fun handlePlatformNotFoundException(
    exception: PlatformNotFoundException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A PlatformNotFoundException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      NOT_FOUND_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(NOT_FOUND_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(CoingeckoCryptoNotFoundException::class)
  fun handleCoingeckoCryptoNotFoundException(
    exception: CoingeckoCryptoNotFoundException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A CoingeckoCryptoNotFoundException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      NOT_FOUND_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(NOT_FOUND_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(UserCryptoNotFoundException::class)
  fun handleUserCryptoNotFoundException(
    exception: UserCryptoNotFoundException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A UserCryptoNotFoundException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      NOT_FOUND_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(NOT_FOUND_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(GoalNotFoundException::class)
  fun handleGoalNotFoundException(
    exception: GoalNotFoundException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A GoalNotFoundException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      NOT_FOUND_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(NOT_FOUND_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(PriceTargetNotFoundException::class)
  fun handlePriceTargetNotFoundException(
    exception: PriceTargetNotFoundException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A PriceTargetNotFoundException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      NOT_FOUND_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(NOT_FOUND_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(DuplicatedPriceTargetException::class)
  fun handleDuplicatedPriceTargetException(
    exception: DuplicatedPriceTargetException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A DuplicatedPriceTargetException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      BAD_REQUEST_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(BAD_REQUEST_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(DuplicatedPlatformException::class)
  fun handleDuplicatedPlatformException(
    exception: DuplicatedPlatformException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A DuplicatedPlatformException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      BAD_REQUEST_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(BAD_REQUEST_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(DuplicatedCryptoPlatFormException::class)
  fun handleDuplicatedCryptoPlatFormException(
    exception: DuplicatedCryptoPlatFormException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A DuplicatedCryptoPlatFormException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      BAD_REQUEST_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail))
  }

  @ExceptionHandler(DuplicatedGoalException::class)
  fun handleDuplicatedGoalException(
    exception: DuplicatedGoalException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A DuplicatedGoalException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      BAD_REQUEST_STATUS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(BAD_REQUEST_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(InsufficientBalanceException::class)
  fun handleInsufficientBalanceException(
    exception: InsufficientBalanceException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "An InsufficientBalanceException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      HttpStatus.BAD_REQUEST.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail))
  }

  @ExceptionHandler(UsernameNotFoundException::class)
  fun handleUsernameNotFoundException(
    exception: UsernameNotFoundException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "An UsernameNotFoundException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      HttpStatus.NOT_FOUND.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(problemDetail))
  }

  @ExceptionHandler(TooManyRequestsException::class)
  fun handleTooManyRequestsException(
    exception: TooManyRequestsException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.warn { "A TooManyRequestsException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail =
      HttpStatus.TOO_MANY_REQUESTS.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(listOf(problemDetail))
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(
    exception: MethodArgumentNotValidException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A MethodArgumentNotValidException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetails = exception.allErrors.map {
      HttpStatus.BAD_REQUEST.withDetailsAndURI(
        it.defaultMessage ?: UNKNOWN_ERROR,
        URI.create(request.requestURL.toString())
      )
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetails)
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleHttpMessageNotReadableException(
    exception: HttpMessageNotReadableException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A HttpMessageNotReadableException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val problemDetail = ProblemDetail.forStatus(BAD_REQUEST_STATUS)
    problemDetail.type = URI.create(request.requestURL.toString())

    return ResponseEntity.status(BAD_REQUEST_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(ConstraintViolationException::class)
  fun handleConstraintViolationException(
    exception: ConstraintViolationException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A ConstraintViolationException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val constraintViolations = exception.constraintViolations.toList()

    val problemDetails = constraintViolations.map {
      BAD_REQUEST_STATUS.withDetailsAndURI(it.message, URI.create(request.requestURL.toString()))
    }

    return ResponseEntity.status(BAD_REQUEST_STATUS).body(problemDetails)
  }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleMissingServletRequestParameterException(
    exception: MissingServletRequestParameterException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A MissingServletRequestParameterException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val detail = exception.body.detail ?: exception.message
    val problemDetail = BAD_REQUEST_STATUS.withDetailsAndURI(detail, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(BAD_REQUEST_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(
    exception: AccessDeniedException,
    webRequest: WebRequest
  ): ResponseEntity<ProblemDetail> {
    logger.info { "An AccessDeniedException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val detail = exception.message ?: "Forbidden"
    val problemDetail = HttpStatus.FORBIDDEN.withDetailsAndURI(detail, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail)
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleMethodArgumentTypeMismatchException(
    exception: MethodArgumentTypeMismatchException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.info { "A MethodArgumentTypeMismatchException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val name = exception.name
    val availableValues = exception.requiredType?.let { it.enumConstants.contentToString() }
    val message = if (availableValues != null) {
      INVALID_VALUE_FOR.format(exception.value, name, availableValues)
    } else {
      "Invalid value ${exception.value} for $name"
    }
    val problemDetail = BAD_REQUEST_STATUS.withDetailsAndURI(message, URI.create(request.requestURL.toString()))

    return ResponseEntity.status(BAD_REQUEST_STATUS).body(listOf(problemDetail))
  }

  @ExceptionHandler(ApiException::class)
  fun handleApiException(
    exception: ApiException,
    webRequest: WebRequest
  ): ResponseEntity<List<ProblemDetail>> {
    logger.warn { "An ApiException occurred $exception" }

    val request = (webRequest as ServletWebRequest).request
    val httpStatusCode = exception.httpStatusCode

    val problemDetail = ProblemDetail.forStatusAndDetail(httpStatusCode, exception.message)
    problemDetail.type = URI.create(request.requestURL.toString())

    return ResponseEntity.status(httpStatusCode).body(listOf(problemDetail))
  }

  @ExceptionHandler(Exception::class)
  fun handleException(
    exception: Exception
  ): ResponseEntity<List<ProblemDetail>> {
    logger.error { "An unhandled Exception occurred $exception" }

    val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_ERROR)

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(listOf(problemDetail))
  }

  private fun HttpStatus.withDetailsAndURI(
    detail: String,
    type: URI
  ): ProblemDetail {
    val problemDetail = ProblemDetail.forStatusAndDetail(this, detail)
    problemDetail.type = type

    return problemDetail
  }
}
