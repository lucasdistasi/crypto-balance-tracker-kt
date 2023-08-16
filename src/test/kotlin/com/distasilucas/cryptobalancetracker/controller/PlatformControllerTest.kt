package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import com.distasilucas.cryptobalancetracker.service.PlatformService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.util.UUID

class PlatformControllerTest {

    private val platformServiceMock = mockk<PlatformService>()
    private val platformController = PlatformController(platformServiceMock)

    @Test
    fun `should retrieve number of entities`() {
        every { platformServiceMock.countPlatforms() } returns 5

        val responseEntity = platformController.countPlatforms()

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(5L))
    }

    @Test
    fun `should retrieve platform`() {
        val id = UUID.randomUUID().toString()
        val platformResponse = PlatformResponse(id, "BINANCE")

        every { platformServiceMock.retrievePlatform(id) } returns platformResponse

        val responseEntity = platformController.retrievePlatform(id)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(platformResponse))
    }

    @Test
    fun `should return 200 when retrieving all platforms`() {
        val platformResponse = PlatformResponse(UUID.randomUUID().toString(), "BINANCE")

        every { platformServiceMock.retrieveAllPlatforms() } returns listOf(platformResponse)

        val responseEntity = platformController.retrieveAllPlatforms()

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(listOf(platformResponse)))
    }

    @Test
    fun `should return 204 when retrieving all platforms`() {
        every { platformServiceMock.retrieveAllPlatforms() } returns emptyList()

        val responseEntity = platformController.retrieveAllPlatforms()

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.noContent().build<List<PlatformResponse>>())
    }

    @Test
    fun `should save platform`() {
        val platformRequest = PlatformRequest("BINANCE")
        val platformEntity = platformRequest.toEntity()
        val platformResponse = platformEntity.toPlatformResponse()

        every { platformServiceMock.savePlatform(platformRequest) } returns platformResponse

        val responseEntity = platformController.savePlatform(platformRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(platformResponse))
    }

    @Test
    fun `should update platform`() {
        val id = UUID.randomUUID().toString()
        val platformRequest = PlatformRequest("OKX")
        val platformEntity = platformRequest.toEntity()
        val platformResponse = platformEntity.toPlatformResponse()

        every { platformServiceMock.updatePlatform(id, platformRequest) } returns platformResponse

        val responseEntity = platformController.updatePlatform(id, platformRequest)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(platformResponse))
    }

    @Test
    fun `should delete platform`() {
        val id = UUID.randomUUID().toString()

        every { platformServiceMock.deletePlatform(id) } just runs

        val responseEntity = platformController.deletePlatform(id)

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok().build<ResponseEntity<Unit>>())
    }
}