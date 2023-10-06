package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.ALL_PLATFORMS_CACHE
import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_PLATFORMS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import com.distasilucas.cryptobalancetracker.repository.PlatformRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PlatformService(
    private val platformRepository: PlatformRepository,
    private val cacheService: CacheService
) {

    private val logger = KotlinLogging.logger { }

    fun countPlatforms() = platformRepository.count()

    @Cacheable(cacheNames = [PLATFORM_PLATFORM_ID_CACHE], key = "#platformId")
    fun retrievePlatformById(platformId: String): PlatformResponse {
        logger.info { "Retrieving platformId $platformId" }

        return platformRepository.findById(platformId)
            .orElseThrow { PlatformNotFoundException(PLATFORM_ID_NOT_FOUND.format(platformId)) }
            .toPlatformResponse()
    }

    @Cacheable(cacheNames = [ALL_PLATFORMS_CACHE])
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
        cacheService.invalidatePlatformsCaches()

        val platformResponse = platformEntity.toPlatformResponse()
        logger.info { "Saved platform $platformResponse" }

        return platformResponse
    }

    fun updatePlatform(platformId: String, platformRequest: PlatformRequest): PlatformResponse {
        validatePlatformNotExists(platformRequest.name!!)

        val existingPlatform = findPlatformById(platformId)
            .orElseThrow { PlatformNotFoundException(PLATFORM_ID_NOT_FOUND.format(platformId)) }

        val updatedPlatform = Platform(
            id = existingPlatform.id,
            name = platformRequest.name
        )

        platformRepository.save(updatedPlatform)
        cacheService.invalidatePlatformsCaches()
        val platformResponse = updatedPlatform.toPlatformResponse()
        logger.info { "Updated platform. Before: $existingPlatform  | After: $updatedPlatform" }

        return platformResponse
    }

    fun deletePlatform(platformId: String) {
        findPlatformById(platformId)
            .ifPresentOrElse({
                platformRepository.deleteById(platformId)
                cacheService.invalidatePlatformsCaches()
                logger.info { "Deleted platform $it" }
            },{
                throw PlatformNotFoundException(PLATFORM_ID_NOT_FOUND.format(platformId))
            })
    }

    @Cacheable(cacheNames = [PLATFORMS_PLATFORMS_IDS_CACHE], key = "#ids")
    fun findAllByIds(ids: Collection<String>): List<Platform> {
        logger.info { "Retrieving platforms for ids $ids" }

        return platformRepository.findAllByIdIn(ids)
    }

    private fun findPlatformById(platformId: String) = platformRepository.findById(platformId)

    private fun validatePlatformNotExists(platformName: String) {
        val existingPlatform = platformRepository.findByName(platformName.uppercase())

        if (existingPlatform.isPresent) {
            throw DuplicatedPlatformException(DUPLICATED_PLATFORM.format(existingPlatform.get().name))
        }
    }
}

class PlatformNotFoundException(message: String) : RuntimeException(message)

class DuplicatedPlatformException(message: String) : RuntimeException(message)