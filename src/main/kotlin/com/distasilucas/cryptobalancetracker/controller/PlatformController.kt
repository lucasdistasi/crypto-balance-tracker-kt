package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_UUID
import com.distasilucas.cryptobalancetracker.controller.swagger.PlatformControllerAPI
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import com.distasilucas.cryptobalancetracker.service.PlatformService
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/platforms")
@CrossOrigin(origins = ["\${allowed-origins}"])
class PlatformController(private val platformService: PlatformService) : PlatformControllerAPI {

  @GetMapping("/count")
  override fun countPlatforms(): ResponseEntity<Long> {
    val totalPlatforms = platformService.countPlatforms()

    return ResponseEntity.ok(totalPlatforms)
  }

  @GetMapping("/{platformId}")
  override fun retrievePlatform(@PathVariable @UUID(message = PLATFORM_ID_UUID) platformId: String): ResponseEntity<PlatformResponse> {
    val platformResponse = platformService.retrievePlatformById(platformId)

    return ResponseEntity.ok(platformResponse.toPlatformResponse())
  }

  @GetMapping
  override fun retrieveAllPlatforms(): ResponseEntity<List<PlatformResponse>> {
    val allPlatforms = platformService.retrieveAllPlatforms()

    return if (allPlatforms.isEmpty()) ResponseEntity.noContent().build() else
      ResponseEntity.ok(allPlatforms.map { it.toPlatformResponse() })
  }

  @PostMapping
  override fun savePlatform(
    @Valid @RequestBody platformRequest: PlatformRequest
  ): ResponseEntity<PlatformResponse> {
    val platformResponse = platformService.savePlatform(platformRequest)

    return ResponseEntity.ok(platformResponse.toPlatformResponse())
  }

  @PutMapping("/{platformId}")
  override fun updatePlatform(
    @PathVariable @UUID(message = PLATFORM_ID_UUID) platformId: String,
    @Valid @RequestBody platformRequest: PlatformRequest
  ): ResponseEntity<PlatformResponse> {
    val updatedPlatform = platformService.updatePlatform(platformId, platformRequest)

    return ResponseEntity.ok(updatedPlatform.toPlatformResponse())
  }

  @DeleteMapping("/{platformId}")
  override fun deletePlatform(@PathVariable @UUID(message = PLATFORM_ID_UUID) platformId: String): ResponseEntity<Unit> {
    platformService.deletePlatform(platformId)

    return ResponseEntity.noContent().build()
  }
}
