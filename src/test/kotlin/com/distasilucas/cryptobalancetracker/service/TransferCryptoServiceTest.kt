package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.NOT_ENOUGH_BALANCE
import com.distasilucas.cryptobalancetracker.constants.SAME_FROM_TO_PLATFORM
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.request.crypto.TransferCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.FromPlatform
import com.distasilucas.cryptobalancetracker.model.response.crypto.ToPlatform
import com.distasilucas.cryptobalancetracker.model.response.crypto.TransferCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.*

class TransferCryptoServiceTest {

    private val userCryptoServiceMock = mockk<UserCryptoService>()
    private val platformServiceMock = mockk<PlatformService>()

    private val transferCryptoService = TransferCryptoService(userCryptoServiceMock, platformServiceMock)

    //      FROM        |       TO             |
    //  has remaining   |   has the crypto     | ---> Update FROM and TO.
    //  has remaining   |   hasn't the crypto  | ---> Update FROM. Add to TO.
    //  no remaining    |   has the crypto     | ---> Remove it from FROM. Update TO.
    //  no remaining    |   hasn't the crypto  | ---> It's easier to update FROM with the new platform and quantity.

    /*
        If there is remaining in from platform, we need to check full quantity toggle to perform some operations based on that

        totalToSubtract (full quantity enabled) = quantityToTransfer + networkFee
        totalToSubtract (full quantity disabled) = quantityToTransfer
        quantityToSendReceive (full quantity enabled) = quantityToTransfer
        quantityToSendReceive (full quantity disabled) = quantityToTransfer - networkFee
        newQuantity = quantity (actual quantity from toPlatformUserCrypto if there is or ZERO) + quantityToSendReceive
     */

    @Test
    fun `should transfer from platform with remaining to platform with existing crypto and full quantity disabled`() {
        val transferCryptoRequest = getTransferCryptoRequest(sendFullQuantity = false)
        val userCryptoToTransfer = getUserCryptoToTransfer()
        val toPlatformUserCrypto = getToPlatformUserCrypto()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.of(toPlatformUserCrypto)
        justRun {
            userCryptoServiceMock.saveOrUpdateAll(
                listOf(
                    UserCrypto(
                        id = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("1.865321283"),
                        platformId = "d5f63c4d-98e7-4d26-b380-e7d0f5c423e9"
                    ),
                    UserCrypto(
                        id = "a6b9f1e8-c1d5-4a8b-bf52-836e6a2e4c3d",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("2.261938292"),
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
                    )
                )
            )
        }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                TransferCryptoResponse(
                    fromPlatform = FromPlatform(
                        userCryptoId = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        networkFee = "0.0005",
                        quantityToTransfer = "0.51",
                        totalToSubtract = "0.51",
                        quantityToSendReceive = "0.5095",
                        remainingCryptoQuantity = "1.865321283",
                        sendFullQuantity = false
                    ),
                    toPlatform = ToPlatform(
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
                        newQuantity = "2.261938292"
                    )
                )
            )
    }

    @Test
    fun `should transfer from platform with remaining to platform without existing crypto and full quantity disabled`() {
        val transferCryptoRequest = getTransferCryptoRequest(sendFullQuantity = false)
        val userCryptoToTransfer = getUserCryptoToTransfer()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                    "bitcoin",
                    "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.empty()
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "60560fe6-8be2-460f-89ba-ef2e1c2e405b"
        justRun {
            userCryptoServiceMock.saveOrUpdateAll(
                    listOf(
                            UserCrypto(
                                    id = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                    coingeckoCryptoId = "bitcoin",
                                    quantity = BigDecimal("1.865321283"),
                                    platformId = "d5f63c4d-98e7-4d26-b380-e7d0f5c423e9"
                            ),
                            UserCrypto(
                                    id = "60560fe6-8be2-460f-89ba-ef2e1c2e405b",
                                    coingeckoCryptoId = "bitcoin",
                                    quantity = BigDecimal("0.5095"),
                                    platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
                            )
                    )
            )
        }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        assertThat(transferCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(
                        TransferCryptoResponse(
                                fromPlatform = FromPlatform(
                                        userCryptoId = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                        networkFee = "0.0005",
                                        quantityToTransfer = "0.51",
                                        totalToSubtract = "0.51",
                                        quantityToSendReceive = "0.5095",
                                        remainingCryptoQuantity = "1.865321283",
                                        sendFullQuantity = false
                                ),
                                toPlatform = ToPlatform(
                                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
                                        newQuantity = "0.5095"
                                )
                        )
                )
    }

    @Test
    fun `should transfer from platform with remaining to platform with existing crypto and full quantity enabled`() {
        val transferCryptoRequest = getTransferCryptoRequest()
        val userCryptoToTransfer = getUserCryptoToTransfer()
        val toPlatformUserCrypto = getToPlatformUserCrypto()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.of(toPlatformUserCrypto)
        justRun {
            userCryptoServiceMock.saveOrUpdateAll(
                listOf(
                    UserCrypto(
                        id = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("1.864821283"),
                        platformId = "d5f63c4d-98e7-4d26-b380-e7d0f5c423e9"
                    ),
                    UserCrypto(
                        id = "a6b9f1e8-c1d5-4a8b-bf52-836e6a2e4c3d",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("2.262438292"),
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
                    )
                )
            )
        }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                TransferCryptoResponse(
                    fromPlatform = FromPlatform(
                        userCryptoId = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        networkFee = "0.0005",
                        quantityToTransfer = "0.51",
                        totalToSubtract = "0.5105",
                        quantityToSendReceive = "0.51",
                        remainingCryptoQuantity = "1.864821283",
                        sendFullQuantity = true
                    ),
                    toPlatform = ToPlatform(
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
                        newQuantity = "2.262438292"
                    )
                )
            )
    }

    @Test
    fun `should transfer from platform with remaining to platform without existing crypto and full quantity enabled`() {
        val transferCryptoRequest = getTransferCryptoRequest()
        val userCryptoToTransfer = getUserCryptoToTransfer()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.empty()
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "60560fe6-8be2-460f-89ba-ef2e1c2e405b"
        justRun {
            userCryptoServiceMock.saveOrUpdateAll(
                listOf(
                    UserCrypto(
                        id = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("1.864821283"),
                        platformId = "d5f63c4d-98e7-4d26-b380-e7d0f5c423e9"
                    ),
                    UserCrypto(
                        id = "60560fe6-8be2-460f-89ba-ef2e1c2e405b",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("0.51"),
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
                    )
                )
            )
        }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                TransferCryptoResponse(
                    fromPlatform = FromPlatform(
                        userCryptoId = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        networkFee = "0.0005",
                        quantityToTransfer = "0.51",
                        totalToSubtract = "0.5105",
                        quantityToSendReceive = "0.51",
                        remainingCryptoQuantity = "1.864821283",
                        sendFullQuantity = true
                    ),
                    toPlatform = ToPlatform(
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
                        newQuantity = "0.51"
                    )
                )
            )
    }

    @Test
    fun `should transfer from platform with remaining to platform without existing crypto and full quantity disabled and update only one crypto`() {
        val transferCryptoRequest = getTransferCryptoRequest(
                sendFullQuantity = false,
                networkFee = BigDecimal("0.51")
        )
        val userCryptoToTransfer = getUserCryptoToTransfer()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                    "bitcoin",
                    "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.empty()
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "60560fe6-8be2-460f-89ba-ef2e1c2e405b"
        justRun { userCryptoServiceMock.saveOrUpdateAll(
                listOf(
                        UserCrypto(
                                "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                "bitcoin",
                                BigDecimal("1.865321283"),
                                "d5f63c4d-98e7-4d26-b380-e7d0f5c423e9"
                        )
                )
        ) }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        verify(exactly = 1) { userCryptoServiceMock.saveOrUpdateAll(
                listOf(
                        UserCrypto(
                                "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                "bitcoin",
                                BigDecimal("1.865321283"),
                                "d5f63c4d-98e7-4d26-b380-e7d0f5c423e9"
                        )
                )
        ) }
        assertThat(transferCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(
                        TransferCryptoResponse(
                                FromPlatform(
                                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                        "0.51",
                                        "0.51",
                                        "0.51",
                                        "0",
                                        "1.865321283",
                                        false
                                ),
                                ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "0")
                        )
                )
    }

    /*
        If there is no remaining in from platform, it does not matter if full quantity is true or false.

        totalToSubtract = quantityToTransfer + networkFee
        quantityToSendReceive = quantityToTransfer - networkFee
        newQuantity = quantity (actual quantity from toPlatformUserCrypto if there is or ZERO) + quantityToSendReceive
     */

    @Test
    fun `should transfer from platform without remaining to platform with existing crypto and full quantity enabled`() {
        val transferCryptoRequest = getTransferCryptoRequest(quantityToTransfer = BigDecimal("1.105734142"))
        val userCryptoToTransfer = getUserCryptoToTransfer(quantity = BigDecimal("1.105734142"))
        val toPlatformUserCrypto = getToPlatformUserCrypto(quantity = BigDecimal("0.2512"))
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.of(toPlatformUserCrypto)
        justRun {
            userCryptoServiceMock.deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479")
        }
        justRun {
            userCryptoServiceMock.saveOrUpdateAll(
                listOf(
                    UserCrypto(
                        id = "a6b9f1e8-c1d5-4a8b-bf52-836e6a2e4c3d",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("1.356434142"),
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
                    )
                )
            )
        }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                TransferCryptoResponse(
                    fromPlatform = FromPlatform(
                        userCryptoId = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        networkFee = "0.0005",
                        quantityToTransfer = "1.105734142",
                        totalToSubtract = "1.105734142",
                        quantityToSendReceive = "1.105234142",
                        remainingCryptoQuantity = "0",
                        sendFullQuantity = true
                    ),
                    toPlatform = ToPlatform(
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
                        newQuantity = "1.356434142"
                    )
                )
            )
    }

    @Test
    fun `should transfer from platform without remaining to platform without existing crypto and full quantity enabled`() {
        val transferCryptoRequest = getTransferCryptoRequest(quantityToTransfer = BigDecimal("1.105734142"))
        val userCryptoToTransfer = getUserCryptoToTransfer(quantity = BigDecimal("1.105734142"))
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                    "bitcoin",
                    "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.empty()
        justRun {
            userCryptoServiceMock.saveOrUpdateAll(
                    listOf(
                            UserCrypto(
                                    id = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                    coingeckoCryptoId = "bitcoin",
                                    quantity = BigDecimal("1.105234142"),
                                    platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
                            )
                    )
            )
        }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        assertThat(transferCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(
                        TransferCryptoResponse(
                                fromPlatform = FromPlatform(
                                        userCryptoId = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                        networkFee = "0.0005",
                                        quantityToTransfer = "1.105734142",
                                        totalToSubtract = "1.105734142",
                                        quantityToSendReceive = "1.105234142",
                                        remainingCryptoQuantity = "0",
                                        sendFullQuantity = true
                                ),
                                toPlatform = ToPlatform(
                                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
                                        newQuantity = "1.105234142"
                                )
                        )
                )
    }

    @Test
    fun `should transfer from platform without remaining to platform with existing crypto and full quantity disabled`() {
        val transferCryptoRequest = getTransferCryptoRequest(
            quantityToTransfer = BigDecimal("1.105734142"),
            sendFullQuantity = false
        )
        val userCryptoToTransfer = getUserCryptoToTransfer(quantity = BigDecimal("1.105734142"))
        val toPlatformUserCrypto = getToPlatformUserCrypto()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.of(toPlatformUserCrypto)
        justRun {
            userCryptoServiceMock.deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479")
        }
        justRun {
            userCryptoServiceMock.saveOrUpdateAll(
                listOf(
                    UserCrypto(
                        id = "a6b9f1e8-c1d5-4a8b-bf52-836e6a2e4c3d",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("2.857672434"),
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
                    )
                )
            )
        }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                TransferCryptoResponse(
                    fromPlatform = FromPlatform(
                        userCryptoId = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        networkFee = "0.0005",
                        quantityToTransfer = "1.105734142",
                        totalToSubtract = "1.105734142",
                        quantityToSendReceive = "1.105234142",
                        remainingCryptoQuantity = "0",
                        sendFullQuantity = false
                    ),
                    toPlatform = ToPlatform(
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
                        newQuantity = "2.857672434"
                    )
                )
            )
    }

    @Test
    fun `should transfer from platform without remaing to platform without existing crypto and full quantity disabled`() {
        val transferCryptoRequest = getTransferCryptoRequest(
            quantityToTransfer = BigDecimal("2.375321283"),
            sendFullQuantity = false
        )
        val userCryptoToTransfer = getUserCryptoToTransfer()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.empty()
        justRun {
            userCryptoServiceMock.saveOrUpdateAll(
                listOf(
                    UserCrypto(
                        id = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        coingeckoCryptoId = "bitcoin",
                        quantity = BigDecimal("2.374821283"),
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
                    )
                )
            )
        }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                TransferCryptoResponse(
                    fromPlatform = FromPlatform(
                        userCryptoId = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        networkFee = "0.0005",
                        quantityToTransfer = "2.375321283",
                        totalToSubtract = "2.375321283",
                        quantityToSendReceive = "2.374821283",
                        remainingCryptoQuantity = "0",
                        sendFullQuantity = false
                    ),
                    toPlatform = ToPlatform(
                        platformId = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
                        newQuantity = "2.374821283"
                    )
                )
            )
    }

    @Test
    fun `should transfer from platform without remaining to platform without existing crypto and full quantity disabled and delete one crypto`() {
        val transferCryptoRequest = getTransferCryptoRequest(
                quantityToTransfer = BigDecimal("2.375321283"),
                networkFee = BigDecimal("2.375321283"),
                sendFullQuantity = false
        )
        val userCryptoToTransfer = getUserCryptoToTransfer()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer
        every {
            userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId(
                    "bitcoin",
                    "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
            )
        } returns Optional.empty()
        justRun { userCryptoServiceMock.deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479") }

        val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

        verify(exactly = 0) { userCryptoServiceMock.saveOrUpdateAll(any()) }
        verify(exactly = 1) { userCryptoServiceMock.deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479") }
        assertThat(transferCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(
                        TransferCryptoResponse(
                                FromPlatform(
                                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                        "2.375321283",
                                        "2.375321283",
                                        "2.375321283",
                                        "0",
                                        "0",
                                        false
                                ),
                                ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "0")
                        )
                )
    }

    /*
        Exceptions
     */
    @Test
    fun `should throw ApiException if fromPlatform and toPlatform are the same`() {
        val transferCryptoRequest = getTransferCryptoRequest()
        val userCryptoToTransfer = getUserCryptoToTransfer()
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse(
            id = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        )

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer

        val exception = assertThrows<ApiException> { transferCryptoService.transferCrypto(transferCryptoRequest) }

        assertThat(exception)
            .usingRecursiveComparison()
            .isEqualTo(ApiException(HttpStatus.BAD_REQUEST, SAME_FROM_TO_PLATFORM))
    }

    @Test
    fun `should throw InsufficientBalanceException if totalToSubtract is higher than availableQuantity when sendFullQuantity = false`() {
        val transferCryptoRequest = getTransferCryptoRequest(
            quantityToTransfer = BigDecimal("1"),
            sendFullQuantity = false
        )
        val userCryptoToTransfer = getUserCryptoToTransfer(quantity = BigDecimal("0.5"))
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer

        val exception = assertThrows<InsufficientBalanceException> {
            transferCryptoService.transferCrypto(transferCryptoRequest)
        }

        assertThat(exception.message).isEqualTo(NOT_ENOUGH_BALANCE)
    }

    @Test
    fun `should throw InsufficientBalanceException if totalToSubtract is higher than availableQuantity when sendFullQuantity = true`() {
        val transferCryptoRequest = getTransferCryptoRequest(
            quantityToTransfer = BigDecimal("1"),
            sendFullQuantity = true
        )
        val userCryptoToTransfer = getUserCryptoToTransfer(quantity = BigDecimal("0.5"))
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer

        val exception = assertThrows<InsufficientBalanceException> {
            transferCryptoService.transferCrypto(transferCryptoRequest)
        }

        assertThat(exception.message).isEqualTo(NOT_ENOUGH_BALANCE)
    }

    @Test
    fun `should throw InsufficientBalanceException if networkFee is higher than availableQuantity`() {
        val transferCryptoRequest = getTransferCryptoRequest(
            quantityToTransfer = BigDecimal("1.001"),
            networkFee = BigDecimal("5")
        )
        val userCryptoToTransfer = getUserCryptoToTransfer(
            quantity = BigDecimal("1")
        )
        val toPlatformResponse = getToPlatformResponse()
        val fromPlatformResponse = getFromPlatformResponse()

        every { platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b") } returns toPlatformResponse
        every { platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9") } returns fromPlatformResponse
        every { userCryptoServiceMock.findByUserCryptoId("f47ac10b-58cc-4372-a567-0e02b2c3d479") } returns userCryptoToTransfer

        val exception = assertThrows<InsufficientBalanceException> {
            transferCryptoService.transferCrypto(transferCryptoRequest)
        }

        assertThat(exception.message).isEqualTo(NOT_ENOUGH_BALANCE)
    }

    private fun getTransferCryptoRequest(
        userCryptoId: String = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
        quantityToTransfer: BigDecimal = BigDecimal("0.51"),
        networkFee: BigDecimal = BigDecimal("0.0005"),
        sendFullQuantity: Boolean = true,
        toPlatform: String = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
    ) = TransferCryptoRequest(
        userCryptoId = userCryptoId,
        quantityToTransfer = quantityToTransfer,
        networkFee = networkFee,
        sendFullQuantity = sendFullQuantity,
        toPlatformId = toPlatform
    )

    private fun getUserCryptoToTransfer(
        id: String = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
        coingeckoCryptoId: String = "bitcoin",
        quantity: BigDecimal = BigDecimal("2.375321283"),
        platformId: String = "d5f63c4d-98e7-4d26-b380-e7d0f5c423e9"
    ) = UserCrypto(
        id = id,
        coingeckoCryptoId = coingeckoCryptoId,
        quantity = quantity,
        platformId = platformId
    )

    private fun getToPlatformUserCrypto(
        id: String = "a6b9f1e8-c1d5-4a8b-bf52-836e6a2e4c3d",
        coingeckoCryptoId: String = "bitcoin",
        quantity: BigDecimal = BigDecimal("1.752438292"),
        platformId: String = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
    ) = UserCrypto(
        id = id,
        coingeckoCryptoId = coingeckoCryptoId,
        quantity = quantity,
        platformId = platformId
    )

    private fun getToPlatformResponse(
        id: String = "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b",
        name: String = "BINANCE"
    ) = PlatformResponse(
        id = id,
        name = name
    )

    private fun getFromPlatformResponse(
        id: String = "d5f63c4d-98e7-4d26-b380-e7d0f5c423e9",
        name: String = "BYBIT"
    ) = PlatformResponse(
        id = id,
        name = name
    )
}