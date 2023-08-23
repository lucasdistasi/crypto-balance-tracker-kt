package com.distasilucas.cryptobalancetracker.validation

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CryptoNameValidatorTest {

    private val cryptoNameValidator = CryptoNameValidator()

    @ParameterizedTest
    @ValueSource(strings = [
        "bitcoin", "BITCOIN", "b1tc0in", "x", "7FbA3d9E1C6gH2jL5M0nR8kPqY4sT1vU3W6xZ9cE2aB4dF7hJ0mN5pQ8rK2tV3yx"
    ])
    fun `should return true when validating crypto name`(cryptoName: String) {
        val isValid = cryptoNameValidator.isValid(cryptoName, null)

        assertTrue(isValid)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "", " ", " bitcoin", "bitcoin ", "bit  coin", "$", "x ", " x",
        "bit.coin", "7FbA3d9E1C6gH2jL5M0nR8kPqY4sT1vU3W6xZ9cE2aB4dF7hJ0mN5pQ8rK2tV3yxz"
    ])
    fun `should return false when validating crypto name`(cryptoName: String) {
        val isValid = cryptoNameValidator.isValid(cryptoName, null)

        assertFalse(isValid)
    }

    @Test
    fun `should return false when validating null crypto name`() {
        val isValid = cryptoNameValidator.isValid(null, null)

        assertFalse(isValid)
    }

}