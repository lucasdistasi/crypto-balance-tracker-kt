package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_SIZE
import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.constants.INVALID_PRICE_TARGET_UUID
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import com.distasilucas.cryptobalancetracker.service.PriceTargetService
import com.ninjasquad.springmockk.MockkBean
import deletePriceTarget
import getCryptoInfo
import io.mockk.every
import io.mockk.justRun
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import retrievePriceTarget
import retrievePriceTargetsForPage
import savePriceTarget
import updatePriceTarget
import java.math.BigDecimal

@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension::class)
@WebMvcTest(PriceTargetController::class)
class PriceTargetControllerMvcTest(
  @Autowired private val mockMvc: MockMvc,
) {

  @MockkBean
  private lateinit var priceTargetServiceMock: PriceTargetService

  @Test
  fun `should retrieve price target with status 200`() {
    val priceTargetResponse = getPriceTargetResponse()

    every {
      priceTargetServiceMock.retrievePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")
    } returns priceTargetResponse

    mockMvc.retrievePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.priceTargetId", `is`("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.symbol", `is`("btc")))
      .andExpect(jsonPath("$.cryptoInfo.image", `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")))
      .andExpect(jsonPath("$.currentPrice", `is`("60000")))
      .andExpect(jsonPath("$.priceTarget", `is`("100000")))
      .andExpect(jsonPath("$.change", `is`(35.30)))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should fail with status 400 with 1 message when retrieving price target with invalid id`(priceTargetId: String) {
    mockMvc.retrievePriceTarget(priceTargetId)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Price target id must be a valid UUID")))
  }

  @Test
  fun `should retrieve price targets for page with status 200`() {
    val pagePriceTargetResponse = PagePriceTargetResponse(
      page = 1,
      totalPages = 1,
      hasNextPage = false,
      targets = listOf(getPriceTargetResponse())
    )

    every { priceTargetServiceMock.retrievePriceTargetsByPage(0) } returns pagePriceTargetResponse

    mockMvc.retrievePriceTargetsForPage(0)
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.page", `is`(1)))
      .andExpect(jsonPath("$.totalPages", `is`(1)))
      .andExpect(jsonPath("$.targets").isArray())
      .andExpect(jsonPath("$.targets", hasSize<Int>(1)))
      .andExpect(jsonPath("$.targets[0].priceTargetId", `is`("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
      .andExpect(jsonPath("$.targets[0].cryptoInfo.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.targets[0].cryptoInfo.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.targets[0].cryptoInfo.symbol", `is`("btc")))
      .andExpect(jsonPath("$.targets[0].cryptoInfo.image", `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")))
      .andExpect(jsonPath("$.targets[0].currentPrice", `is`("60000")))
      .andExpect(jsonPath("$.targets[0].priceTarget", `is`("100000")))
      .andExpect(jsonPath("$.targets[0].change", `is`(35.30)))
  }

  @Test
  fun `should fail with status 400 with 1 message when retrieving price targets with invalid page`() {
    mockMvc.retrievePriceTargetsForPage(-1)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`(INVALID_PAGE_NUMBER)))
  }

  @Test
  fun `should save price target with status 200`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("100000"))
    val priceTargetResponse = getPriceTargetResponse()
    val payload = """
      {
        "cryptoNameOrId": "bitcoin",
        "priceTarget": 100000
      }
    """

    every { priceTargetServiceMock.savePriceTarget(priceTargetRequest) } returns priceTargetResponse

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.priceTargetId", `is`("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.symbol", `is`("btc")))
      .andExpect(jsonPath("$.cryptoInfo.image", `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")))
      .andExpect(jsonPath("$.currentPrice", `is`("60000")))
      .andExpect(jsonPath("$.priceTarget", `is`("100000")))
      .andExpect(jsonPath("$.change", `is`(35.30)))
  }

  @Test
  fun `should save price target with max target with status 200`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("9999999999999999.999999999999"))
    val priceTargetResponse = getPriceTargetResponse(priceTarget = "9999999999999999.999999999999")
    val payload = """
      {
        "cryptoNameOrId": "bitcoin",
        "priceTarget": "9999999999999999.999999999999"
      }
    """

    every { priceTargetServiceMock.savePriceTarget(priceTargetRequest) } returns priceTargetResponse

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.priceTargetId", `is`("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.symbol", `is`("btc")))
      .andExpect(jsonPath("$.cryptoInfo.image", `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")))
      .andExpect(jsonPath("$.currentPrice", `is`("60000")))
      .andExpect(jsonPath("$.priceTarget", `is`("9999999999999999.999999999999")))
      .andExpect(jsonPath("$.change", `is`(35.30)))
  }

  @Test
  fun `should fail with status 400 with 2 messages when saving price target with blank cryptoNameOrId`() {
    val payload = """
      {
        "cryptoNameOrId": " ",
        "priceTarget": "10000"
      }
    """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(2)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")))
  }

  @Test
  fun `should fail with status 400 with 2 messages when saving price target with empty cryptoNameOrId`() {
    val payload = """
            {
                "cryptoNameOrId": "",
                "priceTarget": 100000
            }
        """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(3)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(
        jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, CRYPTO_NAME_SIZE, "Invalid crypto name"))
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when saving price target with long cryptoNameOrId`() {
    val cryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName"
    val payload = """
            {
                "cryptoNameOrId": "$cryptoName",
                "priceTarget": 100000
            }
        """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_SIZE)))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      " bitcoin", "bitcoin ", "bit  coin"
    ]
  )
  fun `should fail with status 400 with 1 message when saving price target with invalid cryptoNameOrId`(cryptoNameOrId: String) {
    val payload = """
            {
                "cryptoNameOrId": "$cryptoNameOrId",
                "priceTarget": 100000
            }
        """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Invalid crypto name")))
  }

  @Test
  fun `should fail with status 400 with 2 messages when saving price target with null cryptoNameOrId`() {
    val payload = """
            {
                "priceTarget": 100000
            }
        """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(2)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")))
  }

  @Test
  fun `should fail with status 400 with 1 message when saving price target with null priceTarget`() {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin"
            }
        """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Price target can not be null")))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "99999999999999999.999999999999", "9999999999999999.9999999999999"
    ]
  )
  fun `should fail with status 400 with 2 messages when saving price target with invalid priceTarget`(target: String) {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": $target
            }
        """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(2)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(
        jsonPath("$[*].detail")
          .value(
            containsInAnyOrder(
              "Price target must be less than or equal to 9999999999999999.999999999999",
              "Price target must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when saving price target with invalid priceTarget`() {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": "0.0000000000001"
            }
        """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(
        jsonPath(
          "$[0].detail",
          `is`("Price target must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
        )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = ["-5", "-100", "0"])
  fun `should fail with status 400 with 1 message when saving price target with negative priceTarget`(priceTarget: String) {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": $priceTarget
            }
        """

    mockMvc.savePriceTarget(payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Price target must be greater than 0")))
  }

  @Test
  fun `should update price target with status 200`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("120000"))
    val priceTargetResponse = getPriceTargetResponse(priceTarget = "120000")
    val payload = """
      {
        "cryptoNameOrId": "bitcoin",
        "priceTarget": 120000
      }
    """

    every {
      priceTargetServiceMock.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", priceTargetRequest)
    } returns priceTargetResponse

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.priceTargetId", `is`("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.symbol", `is`("btc")))
      .andExpect(jsonPath("$.cryptoInfo.image", `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")))
      .andExpect(jsonPath("$.currentPrice", `is`("60000")))
      .andExpect(jsonPath("$.priceTarget", `is`("120000")))
      .andExpect(jsonPath("$.change", `is`(35.30)))
  }

  @Test
  fun `should update price target with max priceTarget with status 200`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("9999999999999999.999999999999"))
    val priceTargetResponse = getPriceTargetResponse(priceTarget = "9999999999999999.999999999999")
    val payload = """
      {
        "cryptoNameOrId": "bitcoin",
        "priceTarget": 9999999999999999.999999999999
      }
    """

    every {
      priceTargetServiceMock.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", priceTargetRequest)
    } returns priceTargetResponse

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.priceTargetId", `is`("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoName", `is`("Bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.cryptoId", `is`("bitcoin")))
      .andExpect(jsonPath("$.cryptoInfo.symbol", `is`("btc")))
      .andExpect(jsonPath("$.cryptoInfo.image", `is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")))
      .andExpect(jsonPath("$.currentPrice", `is`("60000")))
      .andExpect(jsonPath("$.priceTarget", `is`("9999999999999999.999999999999")))
      .andExpect(jsonPath("$.change", `is`(35.30)))
  }

  @Test
  fun `should fail with status 400 with 2 messages when updating price target with blank cryptoNameOrId`() {
    val payload = """
      {
        "cryptoNameOrId": " ",
        "priceTarget": "10000"
      }
    """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(2)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")))
  }

  @Test
  fun `should fail with status 400 with 2 messages when updating price target with empty cryptoNameOrId`() {
    val payload = """
            {
                "cryptoNameOrId": "",
                "priceTarget": 100000
            }
        """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(3)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(
        jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, CRYPTO_NAME_SIZE, "Invalid crypto name"))
      )
  }

  @Test
  fun `should fail with status 400 with 2 messages when updating price target with long cryptoNameOrId`() {
    val cryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName"
    val payload = """
            {
                "cryptoNameOrId": "$cryptoName",
                "priceTarget": 100000
            }
        """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_SIZE)))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      " bitcoin", "bitcoin ", "bit  coin"
    ]
  )
  fun `should fail with status 400 with 1 message when updating price target with invalid cryptoNameOrId`(cryptoNameOrId: String) {
    val payload = """
            {
                "cryptoNameOrId": "$cryptoNameOrId",
                "priceTarget": 100000
            }
        """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Invalid crypto name")))
  }

  @Test
  fun `should fail with status 400 with 2 messages when updating price target with null cryptoNameOrId`() {
    val payload = """
            {
                "priceTarget": 100000
            }
        """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(2)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")))
  }

  @Test
  fun `should fail with status 400 with 1 message when updating target with null priceTarget`() {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin"
            }
        """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Price target can not be null")))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "99999999999999999.999999999999", "9999999999999999.9999999999999"
    ]
  )
  fun `should fail with status 400 with 2 messages when updating price target with invalid priceTarget`(priceTarget: String) {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": $priceTarget
            }
        """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(2)))
      .andExpect(jsonPath("$[*].title").value(everyItem(`is`("Bad Request"))))
      .andExpect(jsonPath("$[*].status").value(everyItem(`is`(400))))
      .andExpect(
        jsonPath("$[*].detail")
          .value(
            containsInAnyOrder(
              "Price target must be less than or equal to 9999999999999999.999999999999",
              "Price target must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
            )
          )
      )
  }

  @Test
  fun `should fail with status 400 with 1 message when updating price target with invalid priceTarget`() {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": "0.0000000000001"
            }
        """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(
        jsonPath(
          "$[0].detail",
          `is`("Price target must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
        )
      )
  }

  @ParameterizedTest
  @ValueSource(strings = ["-5", "-100", "0"])
  fun `should fail with status 400 with 1 message when updating price target with negative priceTarget`(priceTarget: String) {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": $priceTarget
            }
        """

    mockMvc.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`("Price target must be greater than 0")))
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should fail with status 400 with 1 message when updating price target with invalid priceTargetId`(priceTargetId: String) {
    val payload = """
            {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": "120000"
            }
        """

    mockMvc.updatePriceTarget(priceTargetId, payload)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`(INVALID_PRICE_TARGET_UUID)))
  }

  @Test
  fun `should delete price target`() {
    justRun { priceTargetServiceMock.deletePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5") }

    mockMvc.deletePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")
      .andExpect(status().isNoContent)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
      "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    ]
  )
  fun `should fail with status 400 with 1 message when deleting price target with invalid priceTargetId`(priceTargetId: String) {
    mockMvc.deletePriceTarget(priceTargetId)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$", hasSize<Int>(1)))
      .andExpect(jsonPath("$[0].title", `is`("Bad Request")))
      .andExpect(jsonPath("$[0].status", `is`(400)))
      .andExpect(jsonPath("$[0].detail", `is`(INVALID_PRICE_TARGET_UUID)))
  }

  private fun getPriceTargetResponse(
    priceTargetId: String = "2ca0a475-bf4b-4733-9f13-6be497ad6fe5",
    cryptoInfo: CryptoInfo = getCryptoInfo(),
    currentPrice: String = "60000",
    priceTarget: String = "100000",
    change: Float = 35.30F
  ) = PriceTargetResponse(priceTargetId, cryptoInfo, currentPrice, priceTarget, change)
}
