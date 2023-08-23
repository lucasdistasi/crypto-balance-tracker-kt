package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import com.distasilucas.cryptobalancetracker.service.PlatformService
import com.ninjasquad.springmockk.MockkBean
import countPlatforms
import deletePlatform
import io.mockk.every
import io.mockk.justRun
import org.assertj.core.api.Assertions.assertThat
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
import retrieveAllPlatforms
import retrievePlatform
import savePlatform
import updatePlatform

@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@WebMvcTest(PlatformController::class)
class PlatformControllerMvcTest(
    @Autowired private val mockMvc: MockMvc
) {

    @MockkBean
    private lateinit var platformServiceMock: PlatformService

    @Test
    fun `should retrieve number of entities with status 200`() {
        every { platformServiceMock.countPlatforms() } returns 5L

        val mvcResult = mockMvc.countPlatforms()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        assertThat(mvcResult.response.contentAsString).isEqualTo("5")
    }

    @Test
    fun `should retrieve platform with status 200`() {
        val id = "123e4567-e89b-12d3-a456-426614174000"
        val platformResponse = PlatformResponse("123e4567-e89b-12d3-a456-426614174000", "BINANCE")

        every {
            platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174000")
        } returns platformResponse

        mockMvc.retrievePlatform(id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174000")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`("BINANCE")))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
            "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
        ]
    )
    fun `should fail with status 400 with 1 message when retrieving platform with invalid id`(platformId: String) {
        mockMvc.retrievePlatform(platformId)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`("must be a valid UUID")))
    }

    @Test
    fun `should retrieve all platforms with status 200`() {
        val platformResponse = PlatformResponse("123e4567-e89b-12d3-a456-426614174000", "BINANCE")

        every { platformServiceMock.retrieveAllPlatforms() } returns listOf(platformResponse)

        mockMvc.retrieveAllPlatforms()
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174000")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.`is`("BINANCE")))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "binance", "OKX", "Coinbase", "Kraken"
        ]
    )
    fun `should save platform with status 200`(platformName: String) {
        val payload = """
            {
                "name": "$platformName"
            }
        """

        every {
            platformServiceMock.savePlatform(PlatformRequest(platformName))
        } returns PlatformResponse("123e4567-e89b-12d3-a456-426614174000", platformName.uppercase())

        mockMvc.savePlatform(payload)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174000")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(platformName.uppercase())))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "123", "Coin base", "C01nb453", "NmkwRsgZuYqEPvDbAtIoCfLHX", "Coinba#e"
        ]
    )
    fun `should fail with status 400 with 1 message when adding invalid platform`(platformName: String) {
        val payload = """
            {
                "name": "$platformName"
            }
        """

        mockMvc.savePlatform(payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$[0].detail",
                    Matchers.`is`("Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed")
                )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "  ", ""
        ]
    )
    fun `should fail with status 400 with 2 messages when adding blank or empty platform`(platformName: String) {
        val payload = """
            {
                "name": "$platformName"
            }
        """

        mockMvc.savePlatform(payload)
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
                            "Platform name cannot be null or blank",
                            "Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 2 messages when adding null platform`() {
        val payload = """
            {
                "name": ${null}
            }
        """

        mockMvc.savePlatform(payload)
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
                            "Platform name cannot be null or blank",
                            "Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed"
                        )
                    )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "binance", "OKX", "Coinbase", "Kraken"
        ]
    )
    fun `should update platform with status 200`(platformName: String) {
        val payload = """
            {
                "name": "$platformName"
            }
        """

        every {
            platformServiceMock.updatePlatform("123e4567-e89b-12d3-a456-426614174000", PlatformRequest(platformName))
        } returns PlatformResponse("123e4567-e89b-12d3-a456-426614174000", platformName.uppercase())

        mockMvc.updatePlatform("123e4567-e89b-12d3-a456-426614174000", payload)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174000")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(platformName.uppercase())))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
            "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
        ]
    )
    fun `should fail with status 400 with 1 message when updating platform with invalid UUID`(platformId: String) {
        val payload = """
            {
                "name": "name"
            }
        """

        mockMvc.updatePlatform(platformId, payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`("must be a valid UUID")))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "123", "Coin base", "C01nb453", "NmkwRsgZuYqEPvDbAtIoCfLHX", "Coinba#e"
        ]
    )
    fun `should fail with status 400 with 1 messages when updating invalid platform`(platformName: String) {
        val payload = """
            {
                "name": "$platformName"
            }
        """

        mockMvc.updatePlatform("123e4567-e89b-12d3-a456-426614174000", payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$[0].detail",
                    Matchers.`is`("Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed")
                )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "", " "
        ]
    )
    fun `should fail with status 400 with 2 messages when updating blank or empty platform`(platformName: String) {
        val payload = """
            {
                "name": "$platformName"
            }
        """

        mockMvc.updatePlatform("123e4567-e89b-12d3-a456-426614174000", payload)
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
                            "Platform name cannot be null or blank",
                            "Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 2 messages when updating null platform`() {
        val payload = """
            {
                "name": ${null}
            }
        """

        mockMvc.updatePlatform("123e4567-e89b-12d3-a456-426614174000", payload)
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
                            "Platform name cannot be null or blank",
                            "Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed"
                        )
                    )
            )
    }

    @Test
    fun `should delete platform with status 200`() {
        val id = "123e4567-e89b-12d3-a456-426614174000"

        justRun { platformServiceMock.deletePlatform(id) }

        mockMvc.deletePlatform(id)
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `should fail with status 400 with 1 message when deleting invalid platform`() {
        val invalidId = "123e4567-e89b-12d3-a456-4266141740001"

        mockMvc.deletePlatform(invalidId)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`("must be a valid UUID")))
    }
}

