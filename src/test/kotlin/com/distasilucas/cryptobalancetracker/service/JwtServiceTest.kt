package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.TOKEN_EXPIRED
import com.distasilucas.cryptobalancetracker.constants.UNKNOWN_ERROR
import com.distasilucas.cryptobalancetracker.entity.User
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.Role
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.*

class JwtServiceTest {

  private val jwtService = JwtService("741C4AB93C0B3257FA14BF418BE24F9678A022862D7A7AA77C8C7397B1E4DA66")
  private val TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWNhcyIsImlhdCI6MTY3NzYyNjI1NiwiZXhwIjoxOTkzMjQ1NDU2fQ.rXsZUCh8cRXoZW712O1BCGjUsONUoO0nRiBGlYwPeu8"
  private val MALFORMED_TOKEN = "eyJhbGciOiJIUzI1NiJ9aeyJzdWIiOiJsdWNhcyIsImlhdCI6MTY3NzYyNjI1NiwiZXhwIjoxOTkzMjQ1NDU2fQ.rXsZUCh8cRXoZW712O1BCGjUsONUoO0nRiBGlYwPeu8"
  private val EXPIRED_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWNhcyIsImlhdCI6MTY3NzYyNjk0MywiZXhwIjoxNjc3NjI2OTMzfQ.eG5dlO40efrT9PXN6tWetyX6io3OxLTLO6ZR447zDXg"

  @Test
  fun `should validate non expired token`() {
    val user = User(UUID.randomUUID().toString(), "lucas", "admin", Role.ROLE_ADMIN, LocalDateTime.now())

    val valid = jwtService.isTokenValid(TOKEN, user)

    assertTrue(valid)
  }

  @Test
  fun `should throw api exception with expired token`() {
    val user = User(UUID.randomUUID().toString(), "admin", "admin", Role.ROLE_ADMIN, LocalDateTime.now())

    val apiException = assertThrows(ApiException::class.java) { jwtService.isTokenValid(EXPIRED_TOKEN, user) }

    assertAll(
      Executable { assertEquals(TOKEN_EXPIRED, apiException.message) },
      Executable { assertEquals(HttpStatus.BAD_REQUEST, apiException.httpStatusCode) }
    )
  }

  @Test
  fun `should throw api exception when no caught exception is thrown`() {
    val user = User(UUID.randomUUID().toString(), "admin", "admin", Role.ROLE_ADMIN, LocalDateTime.now())

    val apiException = assertThrows(ApiException::class.java) { jwtService.isTokenValid(MALFORMED_TOKEN, user) }

    assertAll(
      Executable { assertEquals(UNKNOWN_ERROR, apiException.message) },
      Executable { assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, apiException.httpStatusCode) }
    )
  }

  @Test
  fun `should return false with invalid username`() {
    val user = User(UUID.randomUUID().toString(), "admin", "admin", Role.ROLE_ADMIN, LocalDateTime.now())

    val valid = jwtService.isTokenValid(TOKEN, user)

    assertFalse(valid)
  }

  @Test
  fun `should extract username`() {
    val username = jwtService.extractUsername(TOKEN)

    assertEquals("lucas", username)
  }
}
