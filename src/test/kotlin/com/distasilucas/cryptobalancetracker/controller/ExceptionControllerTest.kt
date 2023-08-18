package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.service.DuplicatedPlatformException
import com.distasilucas.cryptobalancetracker.service.PlatformNotFoundException
import jakarta.servlet.http.HttpServletRequest
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

    private val httpServletRequest: HttpServletRequest = MockHttpServletRequest("POST", "/api/v1/platforms")
    private val servletRequest = ServletWebRequest(httpServletRequest)

    @Test
    fun `should handle PlatformNotFoundException`() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = PLATFORM_NOT_FOUND

        val responseEntity = exceptionController.handlePlatformNotFoundException(PlatformNotFoundException(), servletRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail))
    }

    @Test
    fun `should handle DuplicatedPlatformException`() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.type = URI.create(httpServletRequest.requestURL.toString())
        problemDetail.detail = DUPLICATED_PLATFORM

        val responseEntity = exceptionController.handleDuplicatedPlatformException(DuplicatedPlatformException(), servletRequest)

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
}

fun createMethodParameter(
    clazz: Class<*>,
    methodName: String,
    vararg parameterTypes: Class<*>
): MethodParameter {
    val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
    return MethodParameter(method, -1)
}