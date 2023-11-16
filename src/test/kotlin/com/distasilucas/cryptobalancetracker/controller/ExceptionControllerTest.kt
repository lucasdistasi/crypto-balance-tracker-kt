package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.COINGECKO_CRYPTO_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_CRYPTO_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_GOAL
import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.GOAL_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.NOT_ENOUGH_BALANCE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.REQUEST_LIMIT_REACHED
import com.distasilucas.cryptobalancetracker.constants.UNKNOWN_ERROR
import com.distasilucas.cryptobalancetracker.constants.USERNAME_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.exception.TooManyRequestsException
import com.distasilucas.cryptobalancetracker.service.CoingeckoCryptoNotFoundException
import com.distasilucas.cryptobalancetracker.service.DuplicatedCryptoPlatFormException
import com.distasilucas.cryptobalancetracker.service.DuplicatedGoalException
import com.distasilucas.cryptobalancetracker.service.DuplicatedPlatformException
import com.distasilucas.cryptobalancetracker.service.GoalNotFoundException
import com.distasilucas.cryptobalancetracker.service.InsufficientBalanceException
import com.distasilucas.cryptobalancetracker.service.PlatformNotFoundException
import com.distasilucas.cryptobalancetracker.service.UserCryptoNotFoundException
import com.distasilucas.cryptobalancetracker.service.UsernameNotFoundException
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
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
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
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(problemDetail)))
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
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(problemDetail)))
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
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle GoalNotFoundException`() {
        val exception = GoalNotFoundException(GOAL_ID_NOT_FOUND.format("123e4567-e89b-12d3-a456-426614174000"))
        val httpServletRequest = MockHttpServletRequest("DELETE", "/api/v1/goals/123e4567-e89b-12d3-a456-426614174000")
        val servletRequest = ServletWebRequest(httpServletRequest)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleGoalNotFoundException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle DuplicatedPlatformException`() {
        val exception = DuplicatedPlatformException(DUPLICATED_PLATFORM.format("BINANCE"))
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleDuplicatedPlatformException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
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
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle DuplicatedGoalException`() {
        val exception = DuplicatedGoalException(DUPLICATED_GOAL.format("Bitcoin"))
        val httpServletRequest = MockHttpServletRequest("POST", "/api/v1/goals")
        val servletRequest = ServletWebRequest(httpServletRequest)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleDuplicatedGoalException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle InsufficientBalanceException`() {
        val exception = InsufficientBalanceException(NOT_ENOUGH_BALANCE)
        val httpServletRequest = MockHttpServletRequest("POST", "/api/v1/cryptos/transfer")
        val servletRequest = ServletWebRequest(httpServletRequest)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = exception.message

        val responseEntity = exceptionController.handleInsufficientBalanceException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle UsernameNotFoundException`() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = USERNAME_NOT_FOUND

        val responseEntity = exceptionController.handleUsernameNotFoundException(UsernameNotFoundException(), servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle TooManyRequestsException`() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = REQUEST_LIMIT_REACHED

        val responseEntity = exceptionController.handleTooManyRequestsException(TooManyRequestsException(), servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(listOf(problemDetail)))
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
        problemDetail.detail = UNKNOWN_ERROR

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
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
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
    fun `should handle MissingServletRequestParameterException`() {
        val exception = MissingServletRequestParameterException("parameterName", "parameterType")
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "Required parameter 'parameterName' is not present."

        val responseEntity = exceptionController.handleMissingServletRequestParameterException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle AccessDeniedException with custom message`() {
        val exception = AccessDeniedException("AccessDeniedException")
        val problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "AccessDeniedException"

        val responseEntity = exceptionController.handleAccessDeniedException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail))
    }

    @Test
    fun `should handle AccessDeniedException with default message`() {
        val exception = AccessDeniedException(null)
        val problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "Forbidden"

        val responseEntity = exceptionController.handleAccessDeniedException(exception, servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail))
    }

    @Test
    fun `should handle ApiException with custom http status code`() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = "Error"

        val responseEntity = exceptionController.handleApiException(ApiException(HttpStatus.BAD_REQUEST, "Error"), servletRequest)

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
            .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle ApiException without custom message`() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = UNKNOWN_ERROR

        val responseEntity = exceptionController.handleApiException(ApiException(), servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(listOf(problemDetail)))
    }

    @Test
    fun `should handle any not handled exception`() {
        val nullPointerException = NullPointerException()

        val responseEntity = exceptionController.handleException(nullPointerException)
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_ERROR)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(listOf(problemDetail)))
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