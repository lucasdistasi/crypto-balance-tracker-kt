package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.UNKNOWN_ERROR
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.service.CoingeckoCryptoNotFoundException
import com.distasilucas.cryptobalancetracker.service.DuplicatedCryptoPlatFormException
import com.distasilucas.cryptobalancetracker.service.DuplicatedGoalException
import com.distasilucas.cryptobalancetracker.service.DuplicatedPlatformException
import com.distasilucas.cryptobalancetracker.service.GoalNotFoundException
import com.distasilucas.cryptobalancetracker.service.InsufficientBalanceException
import com.distasilucas.cryptobalancetracker.service.PlatformNotFoundException
import com.distasilucas.cryptobalancetracker.service.UserCryptoNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import java.net.URI

@RestControllerAdvice
class ExceptionController {

    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(PlatformNotFoundException::class)
    fun handlePlatformNotFoundException(
        exception: PlatformNotFoundException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A PlatformNotFoundException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail =
            HttpStatus.NOT_FOUND.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail)
    }

    @ExceptionHandler(CoingeckoCryptoNotFoundException::class)
    fun handleCoingeckoCryptoNotFoundException(
        exception: CoingeckoCryptoNotFoundException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A CoingeckoCryptoNotFoundException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail =
            HttpStatus.NOT_FOUND.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail)
    }

    @ExceptionHandler(UserCryptoNotFoundException::class)
    fun handleUserCryptoNotFoundException(
        exception: UserCryptoNotFoundException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A UserCryptoNotFoundException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail =
            HttpStatus.NOT_FOUND.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail)
    }

    @ExceptionHandler(GoalNotFoundException::class)
    fun handleGoalNotFoundException(
        exception: GoalNotFoundException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A GoalNotFoundException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail =
            HttpStatus.NOT_FOUND.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail)
    }

    @ExceptionHandler(DuplicatedPlatformException::class)
    fun handleDuplicatedPlatformException(
        exception: DuplicatedPlatformException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A DuplicatedPlatformException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail =
            HttpStatus.BAD_REQUEST.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    @ExceptionHandler(DuplicatedCryptoPlatFormException::class)
    fun handleDuplicatedCryptoPlatFormException(
        exception: DuplicatedCryptoPlatFormException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A DuplicatedCryptoPlatFormException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail =
            HttpStatus.BAD_REQUEST.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    @ExceptionHandler(DuplicatedGoalException::class)
    fun handleDuplicatedGoalException(
        exception: DuplicatedGoalException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A DuplicatedGoalException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail =
            HttpStatus.BAD_REQUEST.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleInsufficientBalanceException(
        exception: InsufficientBalanceException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "An InsufficientBalanceException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail =
            HttpStatus.BAD_REQUEST.withDetailsAndURI(exception.message!!, URI.create(request.requestURL.toString()))

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
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
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A HttpMessageNotReadableException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(request.requestURL.toString())

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
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
            HttpStatus.BAD_REQUEST.withDetailsAndURI(it.message, URI.create(request.requestURL.toString()))
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetails)
    }

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        exception: ApiException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.warn { "An ApiException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val httpStatusCode = exception.httpStatusCode

        val problemDetail = ProblemDetail.forStatusAndDetail(httpStatusCode, exception.message!!)
        problemDetail.type = URI.create(request.requestURL.toString())

        return ResponseEntity.status(httpStatusCode)
            .body(problemDetail)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception
    ): ResponseEntity<ProblemDetail> {
        logger.warn { "An unhandled Exception occurred $exception" }

        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_ERROR)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(problemDetail)
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