package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_CRYPTO_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_RESPONSE_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Optional

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class UserCryptoService(
  private val userCryptoRepository: UserCryptoRepository,
  private val platformService: PlatformService,
  private val cryptoService: CryptoService,
  private val cacheService: CacheService,
  private val _userCryptoService: UserCryptoService?
) {

  private val logger = KotlinLogging.logger { }

  @Cacheable(cacheNames = [USER_CRYPTO_ID_CACHE], key = "#userCryptoId")
  fun findByUserCryptoId(userCryptoId: String): UserCrypto {
    return userCryptoRepository.findById(userCryptoId)
      .orElseThrow { throw UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.format(userCryptoId)) }
  }

  @Cacheable(cacheNames = [USER_CRYPTO_RESPONSE_USER_CRYPTO_ID_CACHE], key = "#userCryptoId")
  fun retrieveUserCryptoResponseById(userCryptoId: String): UserCryptoResponse {
    logger.info { "Retrieving user crypto with id $userCryptoId" }

    val userCrypto = findByUserCryptoId(userCryptoId)
    val crypto = cryptoService.retrieveCryptoInfoById(userCrypto.coingeckoCryptoId)
    val platform = platformService.retrievePlatformById(userCrypto.platformId)

    return userCrypto.toUserCryptoResponse(crypto.name, platform.name)
  }

  @Cacheable(cacheNames = [USER_CRYPTOS_RESPONSE_PAGE_CACHE], key = "#page")
  fun retrieveUserCryptosByPage(page: Int): PageUserCryptoResponse {
    logger.info { "Retrieving user cryptos for page $page" }

    val pageRequest: Pageable = PageRequest.of(page, 10)
    val entityUserCryptosPage = userCryptoRepository.findAll(pageRequest)
    val userCryptosPage = entityUserCryptosPage.content.map { userCrypto ->
      val platform = platformService.retrievePlatformById(userCrypto.platformId)
      val crypto = cryptoService.retrieveCryptoInfoById(userCrypto.coingeckoCryptoId)

      userCrypto.toUserCryptoResponse(crypto.name, platform.name)
    }

    return PageUserCryptoResponse(page, entityUserCryptosPage.totalPages, userCryptosPage)
  }

  fun saveUserCrypto(userCryptoRequest: UserCryptoRequest): UserCryptoResponse {
    val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(userCryptoRequest.cryptoName!!)
    val platform = platformService.retrievePlatformById(userCryptoRequest.platformId!!)

    val existingUserCrypto =
      userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(coingeckoCrypto.id, userCryptoRequest.platformId)

    if (existingUserCrypto.isPresent) {
      throw DuplicatedCryptoPlatFormException(
        DUPLICATED_CRYPTO_PLATFORM.format(coingeckoCrypto.name, platform.name)
      )
    }

    val userCrypto = UserCrypto(
      coingeckoCryptoId = coingeckoCrypto.id,
      quantity = userCryptoRequest.quantity!!,
      platformId = userCryptoRequest.platformId
    )

    cryptoService.saveCryptoIfNotExists(coingeckoCrypto.id)
    userCryptoRepository.save(userCrypto)
    logger.info { "Saved user crypto $userCrypto" }
    cacheService.invalidateUserCryptosAndInsightsCaches()

    return userCrypto.toUserCryptoResponse(
      cryptoName = coingeckoCrypto.name,
      platformName = platform.name
    )
  }

  fun updateUserCrypto(userCryptoId: String, userCryptoRequest: UserCryptoRequest): UserCryptoResponse {
    val userCrypto = _userCryptoService!!.findByUserCryptoId(userCryptoId)
    val requestPlatform = platformService.retrievePlatformById(userCryptoRequest.platformId!!)
    val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(userCrypto.coingeckoCryptoId)

    if (didChangePlatform(requestPlatform.id, userCrypto.platformId)) {
      val existingUserCrypto =
        userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(coingeckoCrypto.id, userCryptoRequest.platformId)

      if (existingUserCrypto.isPresent) {
        throw DuplicatedCryptoPlatFormException(
          DUPLICATED_CRYPTO_PLATFORM.format(coingeckoCrypto.name, requestPlatform.name)
        )
      }
    }

    val updatedUserCrypto = UserCrypto(
      id = userCrypto.id,
      coingeckoCryptoId = userCrypto.coingeckoCryptoId,
      quantity = userCryptoRequest.quantity!!,
      platformId = userCryptoRequest.platformId
    )

    userCryptoRepository.save(updatedUserCrypto)
    cacheService.invalidateUserCryptosAndInsightsCaches()
    logger.info { "Updated user crypto. Before: $userCrypto | After: $updatedUserCrypto" }

    return updatedUserCrypto.toUserCryptoResponse(
      cryptoName = coingeckoCrypto.name,
      platformName = requestPlatform.name
    )
  }

  fun deleteUserCrypto(userCryptoId: String) {
    val userCrypto = _userCryptoService!!.findByUserCryptoId(userCryptoId)
    userCryptoRepository.deleteById(userCryptoId)
    cacheService.invalidateUserCryptosAndInsightsCaches()
    cryptoService.deleteCryptoIfNotUsed(userCrypto.coingeckoCryptoId)

    logger.info { "Deleted user crypto $userCryptoId" }
  }

  fun deleteUserCryptos(userCryptos: List<UserCrypto>) {
    logger.info { "Deleting user cryptos ${userCryptos.map { it.coingeckoCryptoId }}" }
    userCryptoRepository.deleteAllById(userCryptos.map { it.id })
    cacheService.invalidateUserCryptosAndInsightsCaches()
  }

  fun findByCoingeckoCryptoIdAndPlatformId(cryptoId: String, platformId: String): Optional<UserCrypto> {
    return userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(cryptoId, platformId)
  }

  fun saveOrUpdateAll(userCryptos: List<UserCrypto>) {
    userCryptoRepository.saveAll(userCryptos)
    cacheService.invalidateUserCryptosAndInsightsCaches()
  }

  @Cacheable(cacheNames = [USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE], key = "#coingeckoCryptoId")
  fun findAllByCoingeckoCryptoId(coingeckoCryptoId: String): List<UserCrypto> {
    logger.info { "Retrieving all user cryptos matching coingecko crypto id $coingeckoCryptoId" }

    return userCryptoRepository.findAllByCoingeckoCryptoId(coingeckoCryptoId)
  }

  @Cacheable(cacheNames = [USER_CRYPTOS_PLATFORM_ID_CACHE], key = "#platformId")
  fun findAllByPlatformId(platformId: String): List<UserCrypto> {
    logger.info { "Retrieving all user cryptos for platformId $platformId" }

    return userCryptoRepository.findAllByPlatformId(platformId)
  }

  @Cacheable(cacheNames = [USER_CRYPTOS_CACHE])
  fun findAll(): List<UserCrypto> {
    logger.info { "Retrieving all user cryptos" }

    return userCryptoRepository.findAll()
  }

  private fun didChangePlatform(newPlatform: String, originalPlatform: String) = newPlatform != originalPlatform
}

class UserCryptoNotFoundException(message: String) : RuntimeException(message)
class DuplicatedCryptoPlatFormException(message: String) : RuntimeException(message)
