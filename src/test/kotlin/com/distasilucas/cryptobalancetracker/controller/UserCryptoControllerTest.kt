package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.model.request.crypto.TransferCryptoRequest
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.FromPlatform
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.ToPlatform
import com.distasilucas.cryptobalancetracker.model.response.crypto.TransferCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.service.TransferCryptoService
import com.distasilucas.cryptobalancetracker.service.UserCryptoService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.math.BigDecimal

class UserCryptoControllerTest {

    private val userCryptoServiceMock = mockk<UserCryptoService>()
    private val transferCryptoServiceMock = mockk<TransferCryptoService>()

    private val userCryptoController = UserCryptoController(userCryptoServiceMock, transferCryptoServiceMock)

    @Test
    fun `should retrieve user crypto with status 200`() {
        val userCryptoResponse = UserCryptoResponse(
            id = "123e4567-e89b-12d3-a456-426614174000",
            cryptoName = "bitcoin",
            quantity = "0.25",
            platform = "Coinbase"
        )

        every {
            userCryptoServiceMock.retrieveUserCryptoById("123e4567-e89b-12d3-a456-426614174000")
        } returns userCryptoResponse

        val responseEntity = userCryptoController.retrieveUserCrypto("123e4567-e89b-12d3-a456-426614174000")

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(userCryptoResponse))
    }

    @Test
    fun `should retrieve user cryptos for page with status 200`() {
        val userCryptoResponse = UserCryptoResponse(
            id = "123e4567-e89b-12d3-a456-426614174000",
            cryptoName = "bitcoin",
            quantity = "0.25",
            platform = "Coinbase"
        )
        val pageUserCryptoResponse = PageUserCryptoResponse(
            page = 1,
            totalPages = 1,
            hasNextPage = false,
            listOf(userCryptoResponse)
        )

        every { userCryptoServiceMock.retrieveUserCryptosByPage(0) } returns pageUserCryptoResponse

        val responseEntity = userCryptoController.retrieveUserCryptosForPage(0)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(pageUserCryptoResponse))
    }

    @Test
    fun `should retrieve empty user cryptos with status 204`() {
        val pageUserCryptoResponse = PageUserCryptoResponse(
            page = 1,
            totalPages = 1,
            hasNextPage = false,
            emptyList()
        )

        every { userCryptoServiceMock.retrieveUserCryptosByPage(0) } returns pageUserCryptoResponse

        val responseEntity = userCryptoController.retrieveUserCryptosForPage(0)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NO_CONTENT).build<PageUserCryptoResponse>())
    }

    @Test
    fun `should save user crypto with status 200`() {
        val userCryptoRequest = UserCryptoRequest(
            "bitcoin",
            BigDecimal("0.25"),
            "42661417-12d3-e89b-a456-123e45674000"
        )
        val userCryptoEntity = userCryptoRequest.toEntity("bitcoin")
        val userCryptoResponse = userCryptoEntity.toUserCryptoResponse("bitcoin", "Coinbase")

        every { userCryptoServiceMock.saveUserCrypto(userCryptoRequest) } returns userCryptoResponse

        val responseEntity = userCryptoController.saveUserCrypto(userCryptoRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(userCryptoResponse))
    }

    @Test
    fun `should update user crypto with status 200`() {
        val userCryptoRequest = UserCryptoRequest(
            "bitcoin",
            BigDecimal("0.25"),
            "42661417-12d3-e89b-a456-123e45674111"
        )
        val userCryptoEntity = userCryptoRequest.toEntity("bitcoin")
        val userCryptoResponse = userCryptoEntity.toUserCryptoResponse("bitcoin", "Binance")

        every {
            userCryptoServiceMock.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)
        } returns userCryptoResponse

        val responseEntity = userCryptoController.updateUserCrypto("123e4567-e89b-12d3-a456-426614174000", userCryptoRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(userCryptoResponse))
    }

    @Test
    fun `should delete user crypto with status 200`() {
        justRun { userCryptoServiceMock.deleteUserCrypto("123e4567-e89b-12d3-a456-426614174000") }

        val responseEntity = userCryptoController.deleteUserCrypto("123e4567-e89b-12d3-a456-426614174000")

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok().build<Unit>())
    }

    @Test
    fun `should transfer crypto and return 200`() {
        val transferCryptoRequest = TransferCryptoRequest(
            userCryptoId = "9da7b110-8937-4c3a-82d2-bc1923a43278",
            quantityToTransfer = BigDecimal("0.2"),
            networkFee = BigDecimal("0.0005"),
            toPlatformId = "4f996b86-d9b1-4386-af49-5f6a9c66d6ef"
        )
        val transferCryptoResponse = TransferCryptoResponse(
            fromPlatform = getFromPlatform(),
            toPlatform = getToPlatform()
        )

        every { transferCryptoServiceMock.transferCrypto(transferCryptoRequest) } returns transferCryptoResponse

        val responseEntity = userCryptoController.transferUserCrypto(transferCryptoRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(transferCryptoResponse))
    }

    private fun getFromPlatform() = FromPlatform(
        userCryptoId = "9da7b110-8937-4c3a-82d2-bc1923a43278",
        networkFee = "0.0005",
        quantityToTransfer = "0.2",
        totalToSubtract = "0.1",
        quantityToSendReceive = "0.105",
        remainingCryptoQuantity = "0.1",
        sendFullQuantity = true
    )

    private fun getToPlatform() = ToPlatform(
        platformId = "4f996b86-d9b1-4386-af49-5f6a9c66d6ef",
        newQuantity = "1.15"
    )
}