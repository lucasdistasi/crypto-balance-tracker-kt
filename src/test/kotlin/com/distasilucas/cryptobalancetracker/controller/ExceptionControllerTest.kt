package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_CRYPTO_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.service.ApiException
import com.distasilucas.cryptobalancetracker.service.CoingeckoCryptoNotFoundException
import com.distasilucas.cryptobalancetracker.service.CryptoNotFoundException
import com.distasilucas.cryptobalancetracker.service.DuplicatedCryptoPlatFormException
import com.distasilucas.cryptobalancetracker.service.DuplicatedPlatformException
import com.distasilucas.cryptobalancetracker.service.PlatformNotFoundException
import com.distasilucas.cryptobalancetracker.service.UserCryptoNotFoundException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.internal.engine.ConstraintViolationImpl
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.client.MockClientHttpResponse
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.validation.BindException
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.ServletWebRequest
import java.net.URI


class ExceptionControllerTest {

    private val exceptionController = ExceptionController()

    private val httpServletRequest = MockHttpServletRequest("POST", "/api/v1/platforms")
    private val servletRequest = ServletWebRequest(httpServletRequest)

    @Test
    fun `should handle PlatformNotFoundException`() {
        val exception = PlatformNotFoundException(PLATFORM_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174111"))
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handlePlatformNotFoundException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail))
    }

    @Test
    fun `should handle DuplicatedPlatformException`() {
        val exception = DuplicatedPlatformException(DUPLICATED_PLATFORM.format("BINANCE"))
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleDuplicatedPlatformException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail))
    }

    @Test
    fun `should handle CryptoNotFoundException`() {
        val exception = CryptoNotFoundException(CRYPTO_NOT_FOUND)
        val httpServletRequest = MockHttpServletRequest("POST", "/api/v1/cryptos/123e4567-e89b-12d3-a456-426614174000")
        val servletRequest = ServletWebRequest(httpServletRequest)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleCryptoNotFoundException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail))
    }

    @Test
    fun `should handle CoingeckoCryptoNotFoundException`() {
        val exception = CoingeckoCryptoNotFoundException(COINGECKO_CRYPTO_NOT_FOUND)
        val httpServletRequest = MockHttpServletRequest("POST", "/api/v1/cryptos")
        val servletRequest = ServletWebRequest(httpServletRequest)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleCoingeckoCryptoNotFoundException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail))
    }

    @Test
    fun `should handle UserCryptoNotFoundException`() {
        val exception = UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000"))
        val httpServletRequest = MockHttpServletRequest("DELETE", "/api/v1/cryptos/123e4567-e89b-12d3-a456-426614174000")
        val servletRequest = ServletWebRequest(httpServletRequest)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleUserCryptoNotFoundException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail))
    }

    @Test
    fun `should handle DuplicatedCryptoPlatFormException`() {
        val exception = DuplicatedCryptoPlatFormException(DUPLICATED_CRYPTO_PLATFORM.format("Bitcoin", "BINANCE"))
        val httpServletRequest = MockHttpServletRequest("PUT", "/api/v1/cryptos/123e4567-e89b-12d3-a456-426614174000")
        val servletRequest = ServletWebRequest(httpServletRequest)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleDuplicatedCryptoPlatFormException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail))
    }

    @Test
    fun `should handle MethodArgumentNotValidException`() {
        val bindException = BindException("target", "objectName")
        bindException.addError(ObjectError("objectName", "Error Message"))
        val methodParameter = createMethodParameter(String::class.java, "compareTo", String::class.java)
        val exception = MethodArgumentNotValidException(methodParameter, bindException)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "Error Message"

        val responseEntity = exceptionController.handleMethodArgumentNotValidException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle MethodArgumentNotValidException with unknown error`() {
        val bindException = BindException("target", "objectName")
        bindException.addError(ObjectError("objectName", null))
        val methodParameter = createMethodParameter(String::class.java, "compareTo", String::class.java)
        val exception = MethodArgumentNotValidException(methodParameter, bindException)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "Unknown error"

        val responseEntity = exceptionController.handleMethodArgumentNotValidException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle HttpMessageNotReadableException`() {
        val exception = HttpMessageNotReadableException("HttpMessageNotReadableException", MockClientHttpResponse())
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())

        val responseEntity = exceptionController.handleHttpMessageNotReadableException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail))
    }

    @Test
    fun `should handle ConstraintViolationException`() {
        val constraintViolation: ConstraintViolation<Platform> = ConstraintViolationImpl.forBeanValidation(
            "messageTemplate",
            emptyMap(),
            emptyMap(),
            "Some error occurred",
            Platform::class.java,
            Platform("id", "name"),
            null,
            null,
            null,
            null,
            null
        )
        val exception = ConstraintViolationException("ConstraintViolationException", setOf(constraintViolation))
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "Some error occurred"

        val responseEntity = exceptionController.handleConstraintViolationException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle ApiException with custom message`() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "Error"

        val responseEntity = exceptionController.handleApiException(ApiException("Error"), servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail))
    }

    @Test
    fun `should handle ApiException without custom message`() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "Unknown error"

        val responseEntity = exceptionController.handleApiException(ApiException(), servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail))
    }
}

fun createMethodParameter(
    clazz: Class<*>,
    methodName: String,
    vararg parameterTypes: Class<*>
): MethodParameter {
    val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
    return MethodParameter(method, -1)
}