package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_SIZE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_DECIMAL_MAX
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_QUANTITY_POSITIVE
import com.distasilucas.cryptobalancetracker.constants.NETWORK_FEE_MIN
import com.distasilucas.cryptobalancetracker.constants.NETWORK_FEE_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.constants.QUANTITY_TO_TRANSFER_DECIMAL_MAX
import com.distasilucas.cryptobalancetracker.constants.QUANTITY_TO_TRANSFER_NOT_NULL
import com.distasilucas.cryptobalancetracker.constants.QUANTITY_TO_TRANSFER_POSITIVE
import com.distasilucas.cryptobalancetracker.constants.TO_PLATFORM_ID_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.TO_PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_UUID
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.request.crypto.TransferCryptoRequest
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.FromPlatform
import com.distasilucas.cryptobalancetracker.model.response.crypto.ToPlatform
import com.distasilucas.cryptobalancetracker.model.response.crypto.TransferCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.service.TransferCryptoService
import com.distasilucas.cryptobalancetracker.service.UserCryptoService
import com.ninjasquad.springmockk.MockkBean
import deleteUserCrypto
import getUserCrypto
import io.mockk.every
import io.mockk.justRun
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import retrieveUserCrypto
import saveUserCrypto
import transferUserCrypto
import updateUserCrypto
import java.math.BigDecimal

@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension::class)
@WebMvcTest(UserCryptoController::class)
class UserCryptoControllerMvcTest(
  @Autowired private val mockMvc: MockMvc
) {

  @MockkBean
  private lateinit var userCryptoServiceMock: UserCryptoService

  @MockkBean
  private lateinit var transferCryptoServiceMock: TransferCryptoService

  @Test
  fun `should retrieve user crypto with status 200`() {
    val userCrypto = getUserCrypto(
      quantity = BigDecimal("0.5")
    )
    val userCryptoResponse = userCrypto.toUserCryptoResponse(
      cryptoName = "Bitcoin",
      platformName = "BINANCE"
    )

    every {
      userCryptoServiceMock.retrieveUserCryptoResponseById("123e4567-e89b-12d3-a456-426614174000")
    } returns userCryptoResponse

    mockMvc.retrieveUserCrypto("123e4567-e89b-12d3-a456-426614174000")
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174000")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.quantity", Matchers.`is`("0.5")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.platform", Matchers.`is`("BINANCE")))
  }

  @Test
  fun `should retrieve user crypto with max value with status 200`() {
    val userCrypto = UserCrypto(
      id = "123e4567-e89b-12d3-a456-426614174000",
      coingeckoCryptoId = "bitcoin",
      quantity = BigDecimal("9999999999999999.999999999999"),
      platformId = "123e4567-e89b-12d3-a456-426614174111"
    )

    val userCryptoResponse = userCrypto.toUserCryptoResponse(
      cryptoName = "Bitcoin",
      platformName = "BINANCE"
    )

    every {
      userCryptoServiceMock.retrieveUserCryptoResponseById("123e4567-e89b-12d3-a456-426614174000")
    } returns userCryptoResponse

    mockMvc.retrieveUserCrypto("123e4567-e89b-12d3-a456-426614174000")
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174000")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.quantity",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.platform", Matchers.`is`("BINANCE")))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should fail with status 400 with 1 message when retrieving user crypto with invalid id`(userCryptoId: String) {
    mockMvc.retrieveUserCrypto(userCryptoId)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`("User crypto id must be a valid UUID")
        )
      )
  }

  @Test
  fun `should save user crypto with status 200`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    val userCryptoResponse = UserCryptoResponse(
      id = "123e4567-e89b-12d3-a456-426614174333",
      cryptoName = "Bitcoin",
      quantity = "1",
      platform = "Binance"
    )

    every {
      userCryptoServiceMock.saveUserCrypto(UserCryptoRequest(cryptoName, quantity, platformId))
    } returns userCryptoResponse

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174333")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.quantity", Matchers.`is`("1")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.platform", Matchers.`is`("Binance")))
  }

  @Test
  fun `should save user crypto with status 200 with max quantity`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("9999999999999999.999999999999")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    val userCryptoResponse = UserCryptoResponse(
      id = "123e4567-e89b-12d3-a456-426614174333",
      cryptoName = "Bitcoin",
      quantity = "9999999999999999.999999999999",
      platform = "Binance"
    )

    every {
      userCryptoServiceMock.saveUserCrypto(UserCryptoRequest(cryptoName, quantity, platformId))
    } returns userCryptoResponse

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174333")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.quantity",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.platform", Matchers.`is`("Binance")))
  }

  @Test
  fun `should fail with status 400 with 2 messages when saving user crypto with blank cryptoName`() {
    val cryptoName = " "
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              "Invalid crypto name",
              CRYPTO_NAME_NOT_BLANK
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 2 messages when saving user crypto with null cryptoName`() {
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": ${null},
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              "Invalid crypto name",
              CRYPTO_NAME_NOT_BLANK
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when saving user crypto with long cryptoName`() {
    val cryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName"
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              CRYPTO_NAME_SIZE
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 3 messages when saving user crypto with zero-size cryptoName`() {
    val cryptoName = ""
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(3)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              "Invalid crypto name",
              CRYPTO_NAME_NOT_BLANK,
              CRYPTO_NAME_SIZE
            )
          )
      )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      " bitcoin", "bitcoin ", "bit  coin"
    ]
  )
  fun `should fail with status 400 with 1 message when saving user crypto with invalid cryptoName`(cryptoName: String) {
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`("Invalid crypto name")
        )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when saving user crypto with null quantity`() {
    val cryptoName = "bitcoin"
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": ${null},
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(CRYPTO_QUANTITY_NOT_NULL)
        )
      )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "99999999999999999.999999999999", "9999999999999999.9999999999999"
    ]
  )
  fun `should fail with status 400 with 2 messages when saving user crypto with invalid quantity`(quantity: String) {
    val cryptoName = "bitcoin"
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": ${BigDecimal(quantity)},
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              CRYPTO_QUANTITY_DECIMAL_MAX,
              "Crypto quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when saving user crypto with invalid quantity`() {
    val cryptoName = "bitcoin"
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": "0.0000000000001",
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`("Crypto quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
        )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = ["-5", "-100", "0"])
  fun `should fail with status 400 with 1 message when saving user crypto with negative quantity`(quantity: String) {
    val cryptoName = "bitcoin"
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": ${BigDecimal(quantity)},
            "platformId": "$platformId"
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(CRYPTO_QUANTITY_POSITIVE)
        )
      )
  }

  @Test
  fun `should fail with status 400 with 2 messages when saving user crypto with blank platformId`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("1")

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": "$quantity",
            "platformId": ""
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              PLATFORM_ID_NOT_BLANK,
              PLATFORM_ID_UUID
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when saving user crypto with null platformId`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("1")

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": "$quantity",
            "platformId": ${null}
        }
        """

    mockMvc.saveUserCrypto(userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(PLATFORM_ID_NOT_BLANK)
        )
      )
  }

  @Test
  fun `should update user crypto with status 200`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("9999999999999999.999999999999")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    val userCryptoResponse = UserCryptoResponse(
      id = "123e4567-e89b-12d3-a456-426614174222",
      cryptoName = "Bitcoin",
      quantity = "9999999999999999.999999999999",
      platform = "Binance"
    )

    every {
      userCryptoServiceMock.updateUserCrypto(
        "123e4567-e89b-12d3-a456-426614174222",
        UserCryptoRequest(cryptoName, quantity, platformId)
      )
    } returns userCryptoResponse

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174222")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.quantity",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.platform", Matchers.`is`("Binance")))
  }

  @Test
  fun `should update user crypto with status 200 with max quantity`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("9999999999999999.999999999999")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    val userCryptoResponse = UserCryptoResponse(
      id = "123e4567-e89b-12d3-a456-426614174222",
      cryptoName = "Bitcoin",
      quantity = "9999999999999999.999999999999",
      platform = "Binance"
    )

    every {
      userCryptoServiceMock.updateUserCrypto(
        "123e4567-e89b-12d3-a456-426614174222",
        UserCryptoRequest(cryptoName, quantity, platformId)
      )
    } returns userCryptoResponse

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174222")))
      .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.quantity",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.platform", Matchers.`is`("Binance")))
  }

  @Test
  fun `should fail when updating user crypto if invalid UUID`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("9999999999999999.999999999999")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-42661417422", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`("User crypto id must be a valid UUID")
        )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when updating user crypto with blank cryptoName`() {
    val cryptoName = " "
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              "Invalid crypto name",
              CRYPTO_NAME_NOT_BLANK
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 2 messages when updating user crypto with null cryptoName`() {
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": ${null},
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              "Invalid crypto name",
              CRYPTO_NAME_NOT_BLANK
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when updating user crypto with long cryptoName`() {
    val cryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName"
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              CRYPTO_NAME_SIZE
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 3 messages when updating user crypto with zero-size cryptoName`() {
    val cryptoName = ""
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(3)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              "Invalid crypto name",
              CRYPTO_NAME_NOT_BLANK,
              CRYPTO_NAME_SIZE
            )
          )
      )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      " bitcoin", "bitcoin ", "bit  coin"
    ]
  )
  fun `should fail with status 400 with 1 message when updating user crypto with invalid cryptoName`(cryptoName: String) {
    val quantity = BigDecimal("1")
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": $quantity,
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`("Invalid crypto name")
        )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when updating user crypto with null quantity`() {
    val cryptoName = "bitcoin"
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": ${null},
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(CRYPTO_QUANTITY_NOT_NULL)
        )
      )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "99999999999999999.999999999999", "9999999999999999.9999999999999"
    ]
  )
  fun `should fail with status 400 with 2 messages when updating user crypto with invalid quantity`(quantity: String) {
    val cryptoName = "bitcoin"
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": ${BigDecimal(quantity)},
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              CRYPTO_QUANTITY_DECIMAL_MAX,
              "Crypto quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when updating user crypto with invalid quantity`() {
    val cryptoName = "bitcoin"
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": "0.0000000000001",
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`("Crypto quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
        )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = ["-5", "-100", "0"])
  fun `should fail with status 400 with 1 message when updating user crypto with negative quantity`(quantity: String) {
    val cryptoName = "bitcoin"
    val platformId = "123e4567-e89b-12d3-a456-426614174111"

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": ${BigDecimal(quantity)},
            "platformId": "$platformId"
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(CRYPTO_QUANTITY_POSITIVE)
        )
      )
  }

  @Test
  fun `should fail with status 400 with 2 messages when updating user crypto with blank platformId`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("1")

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": "$quantity",
            "platformId": ""
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              PLATFORM_ID_NOT_BLANK,
              PLATFORM_ID_UUID
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when updating user crypto with null platformId`() {
    val cryptoName = "bitcoin"
    val quantity = BigDecimal("1")

    val userCryptoRequest = """
        {
            "cryptoName": "$cryptoName",
            "quantity": "$quantity",
            "platformId": ${null}
        }
        """

    mockMvc.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(PLATFORM_ID_NOT_BLANK)
        )
      )
  }

  @Test
  fun `should delete user crypto`() {
    justRun { userCryptoServiceMock.deleteUserCrypto("123e4567-e89b-12d3-a456-426614174000") }

    mockMvc.deleteUserCrypto("123e4567-e89b-12d3-a456-426614174000")
      .andExpect(MockMvcResultMatchers.status().isNoContent)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should fail with status 400 with 1 message when deleting user crypto with invalid id`(userCryptoId: String) {
    mockMvc.deleteUserCrypto(userCryptoId)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`("User crypto id must be a valid UUID")
        )
      )
  }

  @Test
  fun `should transfer crypto with full quantity disabled with status 200`() {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 9999999999999999.999999999999,
                "networkFee": 9999999999999999.999999999999,
                "sendFullQuantity": false,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """
    val transferCryptoRequest = getTransferCryptoRequest(
      quantityToTransfer = BigDecimal("9999999999999999.999999999999"),
      networkFee = BigDecimal("9999999999999999.999999999999")
    )
    val fromPlatform = getFromPlatform(
      networkFee = BigDecimal("9999999999999999.999999999999"),
      quantityToTransfer = BigDecimal("9999999999999999.999999999999"),
      totalToSubtract = BigDecimal("9999999999999999.999999999999"),
      quantityToSendReceive = BigDecimal("9999999999999999.999999999999"),
    )
    val toPlatform = getToPlatform(
      newQuantity = BigDecimal("9999999999999999.999999999999")
    )
    val transferCryptoResponse = TransferCryptoResponse(
      fromPlatform = fromPlatform,
      toPlatform = toPlatform
    )

    every { transferCryptoServiceMock.transferCrypto(transferCryptoRequest) } returns transferCryptoResponse

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.userCryptoId",
          Matchers.`is`("e4a55857-aa15-4a67-8c4e-936a6fb5218d")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.networkFee",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.quantityToTransfer",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.totalToSubtract",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.quantityToSendReceive",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.remainingCryptoQuantity",
          Matchers.`is`("0")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.sendFullQuantity",
          Matchers.`is`(true)
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.toPlatform.platformId",
          Matchers.`is`("c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.toPlatform.newQuantity",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
  }

  @Test
  fun `should transfer crypto with full quantity enabled with status 200`() {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 9999999999999999.999999999999,
                "networkFee": 9999999999999999.999999999999,
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """
    val transferCryptoRequest = getTransferCryptoRequest(
      quantityToTransfer = BigDecimal("9999999999999999.999999999999"),
      networkFee = BigDecimal("9999999999999999.999999999999"),
      sendFullQuantity = true
    )
    val fromPlatform = getFromPlatform(
      networkFee = BigDecimal("9999999999999999.999999999999"),
      quantityToTransfer = BigDecimal("9999999999999999.999999999999"),
      totalToSubtract = BigDecimal("9999999999999999.999999999999"),
      quantityToSendReceive = BigDecimal("9999999999999999.999999999999"),
    )
    val toPlatform = getToPlatform(
      newQuantity = BigDecimal("9999999999999999.999999999999")
    )
    val transferCryptoResponse = TransferCryptoResponse(
      fromPlatform = fromPlatform,
      toPlatform = toPlatform
    )

    every { transferCryptoServiceMock.transferCrypto(transferCryptoRequest) } returns transferCryptoResponse

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.userCryptoId",
          Matchers.`is`("e4a55857-aa15-4a67-8c4e-936a6fb5218d")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.networkFee",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.quantityToTransfer",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.totalToSubtract",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.quantityToSendReceive",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.remainingCryptoQuantity",
          Matchers.`is`("0")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.sendFullQuantity",
          Matchers.`is`(true)
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.toPlatform.platformId",
          Matchers.`is`("c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.toPlatform.newQuantity",
          Matchers.`is`("9999999999999999.999999999999")
        )
      )
  }

  @Test
  fun `should transfer crypto with minimum quantity enabled with status 200`() {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 0.000000000001,
                "networkFee": 0.000000000001,
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """
    val transferCryptoRequest = getTransferCryptoRequest(
      quantityToTransfer = BigDecimal("0.000000000001"),
      networkFee = BigDecimal("0.000000000001"),
      sendFullQuantity = true
    )
    val fromPlatform = getFromPlatform(
      networkFee = BigDecimal("0.000000000001"),
      quantityToTransfer = BigDecimal("0.000000000001"),
      totalToSubtract = BigDecimal("0.000000000001"),
      quantityToSendReceive = BigDecimal.ZERO,
    )
    val toPlatform = getToPlatform(
      newQuantity = BigDecimal.ZERO
    )
    val transferCryptoResponse = TransferCryptoResponse(
      fromPlatform = fromPlatform,
      toPlatform = toPlatform
    )

    every { transferCryptoServiceMock.transferCrypto(transferCryptoRequest) } returns transferCryptoResponse

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.userCryptoId",
          Matchers.`is`("e4a55857-aa15-4a67-8c4e-936a6fb5218d")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.networkFee",
          Matchers.`is`("0.000000000001")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.quantityToTransfer",
          Matchers.`is`("0.000000000001")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.totalToSubtract",
          Matchers.`is`("0.000000000001")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.quantityToSendReceive",
          Matchers.`is`("0")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.remainingCryptoQuantity",
          Matchers.`is`("0")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.fromPlatform.sendFullQuantity",
          Matchers.`is`(true)
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.toPlatform.platformId",
          Matchers.`is`("c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7")
        )
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$.toPlatform.newQuantity",
          Matchers.`is`("0")
        )
      )
  }

  @Test
  fun `should fail with status 400 with 2 messages when transferring user crypto with blank userCryptoId`() {
    val payload = """
            {
                "userCryptoId": " ",
                "quantityToTransfer": 100,
                "networkFee": 1,
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              USER_CRYPTO_ID_NOT_BLANK,
              USER_CRYPTO_ID_UUID
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when transferring user crypto with null userCryptoId`() {
    val payload = """
            {
                "userCryptoId": ${null},
                "quantityToTransfer": 100,
                "networkFee": 1,
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(USER_CRYPTO_ID_NOT_BLANK)
        )
      )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should fail with status 400 with 1 message when transferring user crypto with invalid userCryptoId`(
    userCryptoId: String
  ) {
    val payload = """
            {
                "userCryptoId": "$userCryptoId",
                "quantityToTransfer": 100,
                "networkFee": 1,
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(USER_CRYPTO_ID_UUID)
        )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when transferring user crypto with null quantityToTransfer`() {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": ${null},
                "networkFee": 1,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(QUANTITY_TO_TRANSFER_NOT_NULL)
        )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "99999999999999999", "9999999999999999.9999999999999"
  ])
  fun `should fail with status 400 with 2 messages when transferring user crypto with invalid quantityToTransfer`(
    quantityToTransfer: String
  ) {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": ${BigDecimal(quantityToTransfer)},
                "networkFee": 1,
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              QUANTITY_TO_TRANSFER_DECIMAL_MAX,
              "Quantity to transfer must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
            )
          )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "0", "-5"
  ])
  fun `should fail with status 400 with 1 messages when transferring user crypto with non positive quantityToTransfer`(
    quantityToTransfer: String
  ) {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": ${BigDecimal(quantityToTransfer)},
                "networkFee": 1,
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(QUANTITY_TO_TRANSFER_POSITIVE)
        )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when transferring user crypto with null networkFee`() {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 100,
                "networkFee": ${null},
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(NETWORK_FEE_NOT_NULL)
        )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "99999999999999999", "9999999999999999.9999999999999"
  ])
  fun `should fail with status 400 with 1 message when transferring user crypto with invalid networkFee`(
    networkFee: String
  ) {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 100,
                "networkFee": ${BigDecimal(networkFee)},
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`("Network fee must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
        )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = [
    "-0.000000000001", "-5"
  ])
  fun `should fail with status 400 with 1 messages when transferring user crypto with negative networkFee`(
    networkFee: String
  ) {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 100,
                "networkFee": ${BigDecimal(networkFee)},
                "sendFullQuantity": true,
                "toPlatformId": "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(NETWORK_FEE_MIN)
        )
      )
  }

  @Test
  fun `should fail with status 400 with 2 messages when transferring user crypto with blank toPlatformId`() {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 100,
                "networkFee": 1,
                "sendFullQuantity": true,
                "toPlatformId": " "
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(2)))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].title").value(Matchers.everyItem(Matchers.`is`("Bad Request")))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].status").value(Matchers.everyItem(Matchers.`is`(400)))
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$[*].detail")
          .value(
            Matchers.containsInAnyOrder(
              TO_PLATFORM_ID_NOT_BLANK,
              TO_PLATFORM_ID_UUID
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when transferring user crypto with null toPlatformId`() {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 100,
                "networkFee": 1,
                "sendFullQuantity": true,
                "toPlatformId": ${null}
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(TO_PLATFORM_ID_NOT_BLANK)
        )
      )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should fail with status 400 with 1 message when transferring user crypto with invalid toPlatformId`(
    toPlatformId: String
  ) {
    val payload = """
            {
                "userCryptoId": "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
                "quantityToTransfer": 100,
                "networkFee": 1,
                "sendFullQuantity": true,
                "toPlatformId": "$toPlatformId"
            }
        """

    mockMvc.transferUserCrypto(payload)
      .andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
      .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
      .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
      .andExpect(
        MockMvcResultMatchers.jsonPath(
          "$[0].detail",
          Matchers.`is`(TO_PLATFORM_ID_UUID)
        )
      )
  }

  private fun getTransferCryptoRequest(
    userCryptoId: String = "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
    quantityToTransfer: BigDecimal = BigDecimal("100"),
    networkFee: BigDecimal = BigDecimal("1"),
    sendFullQuantity: Boolean? = false,
    toPlatform: String = "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7"
  ) = TransferCryptoRequest(
    userCryptoId = userCryptoId,
    quantityToTransfer = quantityToTransfer,
    networkFee = networkFee,
    sendFullQuantity = sendFullQuantity,
    toPlatformId = toPlatform
  )

  private fun getFromPlatform(
    userCryptoId: String = "e4a55857-aa15-4a67-8c4e-936a6fb5218d",
    networkFee: BigDecimal = BigDecimal("0.005"),
    quantityToTransfer: BigDecimal = BigDecimal("20"),
    totalToSubtract: BigDecimal = BigDecimal("1"),
    quantityToSendReceive: BigDecimal = BigDecimal("19"),
    remainingCryptoQuantity: BigDecimal = BigDecimal.ZERO,
    sendFullQuantity: Boolean = true
  ) = FromPlatform(
    userCryptoId = userCryptoId,
    networkFee = networkFee.toPlainString(),
    quantityToTransfer = quantityToTransfer.toPlainString(),
    totalToSubtract = totalToSubtract.toPlainString(),
    quantityToSendReceive = quantityToSendReceive.toPlainString(),
    remainingCryptoQuantity = remainingCryptoQuantity.toPlainString(),
    sendFullQuantity = sendFullQuantity
  )

  private fun getToPlatform(
    platformId: String = "c8b9a2d3-7f46-42e0-9c13-5a8e8fcabfa7",
    newQuantity: BigDecimal = BigDecimal("20")
  ) = ToPlatform(
    platformId = platformId,
    newQuantity = newQuantity.toPlainString()
  )
}
