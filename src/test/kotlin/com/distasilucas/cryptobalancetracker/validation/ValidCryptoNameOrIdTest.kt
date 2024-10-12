package com.distasilucas.cryptobalancetracker.validation

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ValidCryptoNameOrIdTest {

  private val validCryptoNameOrId = ValidCryptoNameOrIdValidator()

  @ParameterizedTest
  @ValueSource(strings = ["BTC", "ETH", "USDT", "X", "BITCOINBITCOINB", "BIT COIN", "BIT-COIN"])
  fun `should return true when validating crypto ticker`(cryptoTicker: String) {
    val isValid = validCryptoNameOrId.isValid(cryptoTicker, null)

    assertTrue(isValid)
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "BITCOINBITCOINBITCOINBITCOINBITCOINBITCOINBITCOINBITCOINBITCOINBI"])
  fun `should return false when validating crypto ticker`(cryptoTicker: String) {
    val isValid = validCryptoNameOrId.isValid(cryptoTicker, null)

    assertFalse(isValid)
  }

  @Test
  fun `should return false when validating null crypto ticker`() {
    val isValid = validCryptoNameOrId.isValid(null, null)

    assertFalse(isValid)
  }
}
