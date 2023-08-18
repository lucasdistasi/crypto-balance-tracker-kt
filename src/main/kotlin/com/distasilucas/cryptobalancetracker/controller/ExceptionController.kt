package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_NOT_FOUND
import com.distasilucas.cryptobalancetracker.service.DuplicatedPlatformException
import com.distasilucas.cryptobalancetracker.service.PlatformNotFoundException
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

private val logger = KotlinLogging.logger { }

@RestControllerAdvice
class ExceptionController {

    @ExceptionHandler(PlatformNotFoundException::class)
    fun handlePlatformNotFoundException(
        exception: PlatformNotFoundException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A PlatformNotFoundException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.type = URI.create(request.requestURL.toString())
        problemDetail.detail = PLATFORM_NOT_FOUND

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail)
    }

    @ExceptionHandler(DuplicatedPlatformException::class)
    fun handleDuplicatedPlatformException(
        exception: DuplicatedPlatformException,
        webRequest: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.info { "A DuplicatedPlatformException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(request.requestURL.toString())
        problemDetail.detail = DUPLICATED_PLATFORM

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException,
        webRequest: WebRequest
    ): ResponseEntity<List<ProblemDetail>> {
        logger.info { "A MethodArgumentNotValidException occurred $exception" }

        val request = (webRequest as ServletWebRequest).request
        val errors: List<ProblemDetail> = exception.allErrors.map { error ->
            val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
            problemDetail.type = URI.create(request.requestURL.toString())
            problemDetail.detail = error.defaultMessage

            problemDetail
        }.toList()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
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

        val errors = constraintViolations.map {
            val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
            problemDetail.type = URI.create(request.requestURL.toString())
            problemDetail.detail = it.message

            problemDetail
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
    }
}