package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.entity.Platform
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
  fun `should retrieve number of entities with status 200`() {
    every { platformServiceMock.countPlatforms() } returns 5

    val responseEntity = platformController.countPlatforms()

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.ok(5L))
  }

  @Test
  fun `should retrieve platform with status 200`() {
    val id = UUID.randomUUID().toString()
    val platform = Platform(id, "BINANCE")

    every { platformServiceMock.retrievePlatformById(id) } returns platform

    val responseEntity = platformController.retrievePlatform(id)

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.ok(platform.toPlatformResponse()))
  }

  @Test
  fun `should retrieve all platforms with status 200`() {
    val platform = Platform(UUID.randomUUID().toString(), "BINANCE")

    every { platformServiceMock.retrieveAllPlatforms() } returns listOf(platform)

    val responseEntity = platformController.retrieveAllPlatforms()

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.ok(listOf(platform.toPlatformResponse())))
  }

  @Test
  fun `should retrieve empty platforms with status 204`() {
    every { platformServiceMock.retrieveAllPlatforms() } returns emptyList()

    val responseEntity = platformController.retrieveAllPlatforms()

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.noContent().build<List<PlatformResponse>>())
  }

  @Test
  fun `should save platform with status 200`() {
    val platformRequest = PlatformRequest("BINANCE")
    val platformEntity = platformRequest.toEntity()
    val platformResponse = platformEntity.toPlatformResponse()

    every { platformServiceMock.savePlatform(platformRequest) } returns platformEntity

    val responseEntity = platformController.savePlatform(platformRequest)

    assertThat(responseEntity)
      .isEqualTo(ResponseEntity.ok(platformResponse))
  }

  @Test
  fun `should update platform with status 200`() {
    val id = UUID.randomUUID().toString()
    val platformRequest = PlatformRequest("OKX")
    val platformEntity = platformRequest.toEntity()
    val platformResponse = platformEntity.toPlatformResponse()

    every { platformServiceMock.updatePlatform(id, platformRequest) } returns platformEntity

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
      .isEqualTo(ResponseEntity.noContent().build<ResponseEntity<Unit>>())
  }
}
