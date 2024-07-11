package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.ALL_PLATFORMS_CACHE
import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_PLATFORMS_IDS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest
import com.distasilucas.cryptobalancetracker.repository.PlatformRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class PlatformService(
  private val platformRepository: PlatformRepository,
  @Lazy private val userCryptoService: UserCryptoService,
  private val cacheService: CacheService,
  private val _platformService: PlatformService?
) {

  private val logger = KotlinLogging.logger { }

  fun countPlatforms() = platformRepository.count()

  @Cacheable(cacheNames = [PLATFORM_PLATFORM_ID_CACHE], key = "#platformId")
  fun retrievePlatformById(platformId: String): Platform {
    logger.info { "Retrieving platformId $platformId" }

    return platformRepository.findById(platformId)
      .orElseThrow { PlatformNotFoundException(PLATFORM_ID_NOT_FOUND.format(platformId)) }
  }

  @Cacheable(cacheNames = [ALL_PLATFORMS_CACHE])
  fun retrieveAllPlatforms(): List<Platform> {
    logger.info { "Retrieving all platforms" }

    return platformRepository.findAll()
  }

  fun savePlatform(platformRequest: PlatformRequest): Platform {
    validatePlatformNotExists(platformRequest.name!!)

    val platform = platformRequest.toEntity()
    val platformEntity = platformRepository.save(platform)
    cacheService.invalidatePlatformsCaches()

    logger.info { "Saved platform $platformEntity" }

    return platformEntity
  }

  fun updatePlatform(platformId: String, platformRequest: PlatformRequest): Platform {
    validatePlatformNotExists(platformRequest.name!!)

    val existingPlatform = _platformService!!.retrievePlatformById(platformId)
    val updatedPlatform = platformRequest.toEntity(id = existingPlatform.id)

    platformRepository.save(updatedPlatform)
    cacheService.invalidatePlatformsCaches()
    logger.info { "Updated platform. Before: $existingPlatform  | After: $updatedPlatform" }

    return updatedPlatform
  }

  fun deletePlatform(platformId: String) {
    val platform = _platformService!!.retrievePlatformById(platformId)
    val userCryptosToDelete = userCryptoService.findAllByPlatformId(platform.id)
    userCryptoService.deleteUserCryptos(userCryptosToDelete)

    platformRepository.deleteById(platformId)
    cacheService.invalidatePlatformsCaches()
    logger.info { "Deleted platform ${platform.name}" }
  }

  @Cacheable(cacheNames = [PLATFORMS_PLATFORMS_IDS_CACHE], key = "#ids")
  fun findAllByIds(ids: Collection<String>): List<Platform> {
    logger.info { "Retrieving platforms for ids $ids" }

    return platformRepository.findAllByIdIn(ids)
  }

  private fun validatePlatformNotExists(platformName: String) {
    val existingPlatform = platformRepository.findByName(platformName.uppercase())

    if (existingPlatform.isPresent) {
      throw DuplicatedPlatformException(DUPLICATED_PLATFORM.format(existingPlatform.get().name))
    }
  }
}

class PlatformNotFoundException(message: String) : RuntimeException(message)

class DuplicatedPlatformException(message: String) : RuntimeException(message)
