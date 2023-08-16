package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_NOT_FOUND
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import com.distasilucas.cryptobalancetracker.repository.PlatformRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class PlatformService(
    private val platformRepository: PlatformRepository
) {

    fun countPlatforms() = platformRepository.count()

    fun retrievePlatform(platformId: String): PlatformResponse {
        logger.info { "Retrieving platformId $platformId" }

        return platformRepository.findById(platformId)
            .orElseThrow { PlatformNotFoundException() }
            .toPlatformResponse()
    }

    fun retrieveAllPlatforms(): List<PlatformResponse> {
        logger.info { "Retrieving all platforms" }

        return platformRepository.findAll()
            .map { it.toPlatformResponse() }
            .toList()
    }

    fun savePlatform(platformRequest: PlatformRequest): PlatformResponse {
        validatePlatformNotExists(platformRequest.name!!)

        val platform = platformRequest.toEntity()
        val platformEntity = platformRepository.save(platform)

        val platformResponse = platformEntity.toPlatformResponse()
        logger.info { "Saved platform $platformResponse" }

        return platformResponse
    }

    fun updatePlatform(platformId: String, platformRequest: PlatformRequest): PlatformResponse {
        validatePlatformNotExists(platformRequest.name!!)

        val existingPlatform = findPlatformById(platformId)
            .orElseThrow { PlatformNotFoundException() }
        existingPlatform.name = platformRequest.name.uppercase()

        val updatedPlatform = platformRepository.save(existingPlatform)
        val platformResponse = updatedPlatform.toPlatformResponse()
        logger.info { "Updated platform $updatedPlatform" }

        return platformResponse
    }

    fun deletePlatform(platformId: String) {
        val platformEntity = findPlatformById(platformId)
            .orElseThrow { PlatformNotFoundException() }

        platformRepository.delete(platformEntity)
        logger.info { "Deleted platform $platformEntity" }
    }

    private fun findPlatformById(platformId: String) = platformRepository.findById(platformId)

    private fun validatePlatformNotExists(platformName: String) {
        findByPlatformName(platformName).ifPresent { throw DuplicatedPlatformException() }
    }

    private fun findByPlatformName(platformName: String) = platformRepository.findByName(platformName.uppercase())
}

class PlatformNotFoundException : RuntimeException(PLATFORM_NOT_FOUND)

class DuplicatedPlatformException : RuntimeException(DUPLICATED_PLATFORM)