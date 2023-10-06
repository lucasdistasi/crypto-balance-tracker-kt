package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_NOT_BLANK
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_NAME_SIZE
import com.distasilucas.cryptobalancetracker.constants.INVALID_GOAL_UUID
import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse
import com.distasilucas.cryptobalancetracker.service.GoalService
import com.ninjasquad.springmockk.MockkBean
import deleteGoal
import getGoalResponse
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
import retrieveGoal
import retrieveGoalsForPage
import saveGoal
import updateGoal
import java.math.BigDecimal

@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@WebMvcTest(GoalController::class)
class GoalControllerMvcTest(
    @Autowired private val mockMvc: MockMvc
) {

    @MockkBean
    private lateinit var goalServiceMock: GoalService

    @Test
    fun `should retrieve goal with status 200`() {
        val goalResponse = getGoalResponse()

        every { goalServiceMock.retrieveGoalById("123e4567-e89b-12d3-a456-426614174111") } returns goalResponse

        mockMvc.retrieveGoal("123e4567-e89b-12d3-a456-426614174111")
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174111")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.progress", Matchers.`is`(100.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.remainingQuantity", Matchers.`is`("0")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goalQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.moneyNeeded", Matchers.`is`("0")))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
            "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
        ]
    )
    fun `should fail with status 400 with 1 message when retrieving goal with invalid id`(goalId: String) {
        mockMvc.retrieveGoal(goalId)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`("Goal id must be a valid UUID")))
    }

    @Test
    fun `should retrieve goals for page with status 200`() {
        val pageGoalResponse = PageGoalResponse(
            page = 1,
            totalPages = 1,
            hasNextPage = false,
            goals = listOf(getGoalResponse())
        )

        every { goalServiceMock.retrieveGoalsForPage(0) } returns pageGoalResponse

        mockMvc.retrieveGoalsForPage(0)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.page", Matchers.`is`(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.`is`(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goals").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.goals", Matchers.hasSize<Int>(1)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.goals[0].id",
                    Matchers.`is`("123e4567-e89b-12d3-a456-426614174111")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.goals[0].cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goals[0].actualQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goals[0].progress", Matchers.`is`(100.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goals[0].remainingQuantity", Matchers.`is`("0")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goals[0].goalQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goals[0].moneyNeeded", Matchers.`is`("0")))
    }

    @Test
    fun `should fail with status 400 with 1 message when retrieving goals with invalid page`() {
        mockMvc.retrieveGoalsForPage(-1)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$[0].detail",
                    Matchers.`is`(INVALID_PAGE_NUMBER)
                )
            )
    }

    @Test
    fun `should save goal with status 200`() {
        val goalRequest = GoalRequest(
            cryptoName = "bitcoin",
            goalQuantity = BigDecimal("1")
        )
        val goalResponse = getGoalResponse()
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": 1
            }
        """

        every { goalServiceMock.saveGoal(goalRequest) } returns goalResponse

        mockMvc.saveGoal(payload)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174111")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.progress", Matchers.`is`(100.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.remainingQuantity", Matchers.`is`("0")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goalQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.moneyNeeded", Matchers.`is`("0")))
    }

    @Test
    fun `should save goal with max quantity with status 200`() {
        val goalRequest = GoalRequest(
            cryptoName = "bitcoin",
            goalQuantity = BigDecimal("9999999999999999.999999999999")
        )
        val goalResponse = getGoalResponse()
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": "9999999999999999.999999999999"
            }
        """

        every { goalServiceMock.saveGoal(goalRequest) } returns goalResponse

        mockMvc.saveGoal(payload)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174111")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.progress", Matchers.`is`(100.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.remainingQuantity", Matchers.`is`("0")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goalQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.moneyNeeded", Matchers.`is`("0")))
    }

    @Test
    fun `should fail with status 400 with 2 messages when saving goal with blank cryptoName`() {
        val payload = """
            {
                "cryptoName": " ",
                "goalQuantity": 1
            }
        """

        mockMvc.saveGoal(payload)
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
                            CRYPTO_NAME_NOT_BLANK,
                            "Invalid crypto name"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 2 messages when saving goal with empty cryptoName`() {
        val payload = """
            {
                "cryptoName": "",
                "goalQuantity": 1
            }
        """

        mockMvc.saveGoal(payload)
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
                            CRYPTO_NAME_NOT_BLANK,
                            CRYPTO_NAME_SIZE,
                            "Invalid crypto name"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 2 messages when saving goal with long cryptoName`() {
        val cryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName"
        val payload = """
            {
                "cryptoName": "$cryptoName",
                "goalQuantity": 1
            }
        """

        mockMvc.saveGoal(payload)
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
                            CRYPTO_NAME_SIZE,
                            "Invalid crypto name"
                        )
                    )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            " bitcoin", "bitcoin ", "bit  coin", "bit!coin"
        ]
    )
    fun `should fail with status 400 with 1 message when saving goal with invalid cryptoName`(cryptoName: String) {
        val payload = """
            {
                "cryptoName": "$cryptoName",
                "goalQuantity": 1
            }
        """

        mockMvc.saveGoal(payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`("Invalid crypto name")))
    }

    @Test
    fun `should fail with status 400 with 2 messages when saving goal with null cryptoName`() {
        val payload = """
            {
                "goalQuantity": 1
            }
        """

        mockMvc.saveGoal(payload)
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
                            CRYPTO_NAME_NOT_BLANK,
                            "Invalid crypto name"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 1 message when saving goal with null goalQuantity`() {
        val payload = """
            {
                "cryptoName" : "bitcoin"
            }
        """

        mockMvc.saveGoal(payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`("Goal quantity can not be null")))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "99999999999999999.999999999999", "9999999999999999.9999999999999"
        ]
    )
    fun `should fail with status 400 with 2 messages when saving goal with invalid goalQuantity`(goalQuantity: String) {
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": $goalQuantity
            }
        """

        mockMvc.saveGoal(payload)
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
                            "Goal quantity must be less than or equal to 9999999999999999.999999999999",
                            "Goal quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 1 message when saving goal with invalid goalQuantity`() {
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": "0.0000000000001"
            }
        """

        mockMvc.saveGoal(payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$[0].detail",
                    Matchers.`is`("Goal quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
                )
            )
    }

    @ParameterizedTest
    @ValueSource(strings = ["-5", "-100", "0"])
    fun `should fail with status 400 with 1 message when saving goal with negative goalQuantity`(goalQuantity: String) {
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": $goalQuantity
            }
        """

        mockMvc.saveGoal(payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$[0].detail",
                    Matchers.`is`("Goal quantity must be greater than 0")
                )
            )
    }

    @Test
    fun `should update goal with status 200`() {
        val goalRequest = GoalRequest(
            cryptoName = "bitcoin",
            goalQuantity = BigDecimal("1")
        )
        val goalResponse = getGoalResponse()
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": 1
            }
        """

        every { goalServiceMock.updateGoal("123e4567-e89b-12d3-a456-426614174111", goalRequest) } returns goalResponse

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174111")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.progress", Matchers.`is`(100.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.remainingQuantity", Matchers.`is`("0")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goalQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.moneyNeeded", Matchers.`is`("0")))
    }

    @Test
    fun `should update goal with max quantity with status 200`() {
        val goalRequest = GoalRequest(
            cryptoName = "bitcoin",
            goalQuantity = BigDecimal("9999999999999999.999999999999")
        )
        val goalResponse = getGoalResponse()
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": "9999999999999999.999999999999"
            }
        """

        every { goalServiceMock.updateGoal("123e4567-e89b-12d3-a456-426614174111", goalRequest) } returns goalResponse

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`("123e4567-e89b-12d3-a456-426614174111")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.progress", Matchers.`is`(100.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.remainingQuantity", Matchers.`is`("0")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.goalQuantity", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.moneyNeeded", Matchers.`is`("0")))
    }

    @Test
    fun `should fail with status 400 with 2 messages when updating goal with blank cryptoName`() {
        val payload = """
            {
                "cryptoName": " ",
                "goalQuantity": 1
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
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
                            CRYPTO_NAME_NOT_BLANK,
                            "Invalid crypto name"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 2 messages when updating goal with empty cryptoName`() {
        val payload = """
            {
                "cryptoName": "",
                "goalQuantity": 1
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
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
                            CRYPTO_NAME_NOT_BLANK,
                            CRYPTO_NAME_SIZE,
                            "Invalid crypto name"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 2 messages when updating goal with long cryptoName`() {
        val cryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName"
        val payload = """
            {
                "cryptoName": "$cryptoName",
                "goalQuantity": 1
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
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
                            CRYPTO_NAME_SIZE,
                            "Invalid crypto name"
                        )
                    )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            " bitcoin", "bitcoin ", "bit  coin", "bit!coin"
        ]
    )
    fun `should fail with status 400 with 1 message when updating goal with invalid cryptoName`(cryptoName: String) {
        val payload = """
            {
                "cryptoName": "$cryptoName",
                "goalQuantity": 1
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`("Invalid crypto name")))
    }

    @Test
    fun `should fail with status 400 with 2 messages when updating goal with null cryptoName`() {
        val payload = """
            {
                "goalQuantity": 1
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
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
                            CRYPTO_NAME_NOT_BLANK,
                            "Invalid crypto name"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 1 message when updating goal with null goalQuantity`() {
        val payload = """
            {
                "cryptoName" : "bitcoin"
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`("Goal quantity can not be null")))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "99999999999999999.999999999999", "9999999999999999.9999999999999"
        ]
    )
    fun `should fail with status 400 with 2 messages when updating goal with invalid goalQuantity`(goalQuantity: String) {
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": $goalQuantity
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
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
                            "Goal quantity must be less than or equal to 9999999999999999.999999999999",
                            "Goal quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
                        )
                    )
            )
    }

    @Test
    fun `should fail with status 400 with 1 message when updating goal with invalid goalQuantity`() {
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": "0.0000000000001"
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$[0].detail",
                    Matchers.`is`("Goal quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
                )
            )
    }

    @ParameterizedTest
    @ValueSource(strings = ["-5", "-100", "0"])
    fun `should fail with status 400 with 1 message when updating goal with negative goalQuantity`(goalQuantity: String) {
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": $goalQuantity
            }
        """

        mockMvc.updateGoal("123e4567-e89b-12d3-a456-426614174111", payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$[0].detail",
                    Matchers.`is`("Goal quantity must be greater than 0")
                )
            )
    }

    @Test
    fun `should fail with status 400 with 1 message when updating goal with invalid goalId`() {
        val invalidId = "123e4567-e89b-12d3-a456-42661417411"
        val payload = """
            {
                "cryptoName": "bitcoin",
                "goalQuantity": 1
            }
        """

        mockMvc.updateGoal(invalidId, payload)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`(INVALID_GOAL_UUID)))
    }

    @Test
    fun `should delete goal`() {
        justRun { goalServiceMock.deleteGoal("123e4567-e89b-12d3-a456-426614174111") }

        mockMvc.deleteGoal("123e4567-e89b-12d3-a456-426614174111")
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
            "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
        ]
    )
    fun `should fail with status 400 with 1 message when deleting goal with invalid goalId`(goalId: String) {
        mockMvc.deleteGoal(goalId)
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].detail", Matchers.`is`(INVALID_GOAL_UUID)))
    }

}