package com.distasilucas.cryptobalancetracker.model.request.transaction

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CryptoTickerValidatorTest {

  private val cryptoTickerValidator = CryptoTickerValidator()

  @ParameterizedTest
  @ValueSource(strings = ["BTC", "ETH", "USDT", "X", "BITCOINBITCOINB", "BIT COIN"])
  fun `should return true when validating crypto ticker`(cryptoTicker: String) {
    val isValid = cryptoTickerValidator.isValid(cryptoTicker, null)

    assertTrue(isValid)
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "BITCOINBITCOINBI"])
  fun `should return false when validating crypto ticker`(cryptoTicker: String) {
    val isValid = cryptoTickerValidator.isValid(cryptoTicker, null)

    assertFalse(isValid)
  }

  @Test
  fun `should return false when validating null crypto ticker`() {
    val isValid = cryptoTickerValidator.isValid(null, null)

    assertFalse(isValid)
  }
}
