package com.distasilucas.cryptobalancetracker.model.request.platform

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PlatformNameValidatorTest {

    private val platformNameValidator = PlatformNameValidator()

    @ParameterizedTest
    @ValueSource(strings = [
            "binance", "OKX", "Kraken", "Safepal", "Coinbase", "Trezor One", "Trezor T", "Ledger Nano X",
            "LOOOOOOOOOOOOOOOONG NAME"])
    fun `should return true when validating platform`(platformName: String) {
        val isValid = platformNameValidator.isValid(platformName, null)

        assertTrue(isValid)
    }

    @ParameterizedTest
    @ValueSource(strings = [
            "INVALID-PLATFORM", "INVALID_PLATFORM", "LOOOOOOONGINVALIDPLATFORM", "", "1NV4L1D",
            " invalid", "inv  alid", "invalid "])
    fun `should return false when validating platform`(platformName: String) {
        val isValid = platformNameValidator.isValid(platformName, null)

        assertFalse(isValid)
    }

    @Test
    fun `should return false when validating null platform`() {
        val isValid = platformNameValidator.isValid(null, null)

        assertFalse(isValid)
    }
}