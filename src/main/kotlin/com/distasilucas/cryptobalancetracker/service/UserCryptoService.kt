package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_CRYPTO_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PAGE_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTOS_PLATFORM_ID_CACHE
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UserCryptoService(
    private val userCryptoRepository: UserCryptoRepository,
    private val platformService: PlatformService,
    private val cryptoService: CryptoService,
    private val cacheService: CacheService
) {

    private val logger = KotlinLogging.logger { }

    fun retrieveUserCryptoById(userCryptoId: String): UserCryptoResponse {
        logger.info { "Retrieving user crypto with id $userCryptoId" }

        val userCrypto = findByUserCryptoId(userCryptoId)
        val crypto = cryptoService.retrieveCryptoInfoById(userCrypto.coingeckoCryptoId)
        val platform = platformService.retrievePlatformById(userCrypto.platformId)

        return userCrypto.toUserCryptoResponse(crypto.name, platform.name)
    }

    fun retrieveUserCryptosByPage(page: Int): PageUserCryptoResponse { // TODO - ADD CACHE??
        logger.info { "Retrieving user cryptos for page $page" }

        val pageRequest: Pageable = PageRequest.of(page, 10)
        val entityUserCryptosPage = userCryptoRepository.findAll(pageRequest) // TODO - ADD CACHE??
        val userCryptosPage = entityUserCryptosPage.content.map { userCrypto ->
            val platform = platformService.retrievePlatformById(userCrypto.platformId)
            val crypto = cryptoService.retrieveCryptoInfoById(userCrypto.coingeckoCryptoId)

            userCrypto.toUserCryptoResponse(crypto.name, platform.name)
        }

        return PageUserCryptoResponse(page, entityUserCryptosPage.totalPages, userCryptosPage)
    }

    fun saveUserCrypto(userCryptoRequest: UserCryptoRequest): UserCryptoResponse {
        val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByName(userCryptoRequest.cryptoName!!)
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

        userCryptoRepository.save(userCrypto)
        cacheService.invalidateUserCryptosCaches()
        logger.info { "Saved user crypto $userCrypto" }
        cryptoService.saveCryptoIfNotExists(coingeckoCrypto.id)

        return userCrypto.toUserCryptoResponse(
            cryptoName = coingeckoCrypto.name,
            platformName = platform.name
        )
    }

    fun updateUserCrypto(userCryptoId: String, userCryptoRequest: UserCryptoRequest): UserCryptoResponse {
        val userCrypto = findByUserCryptoId(userCryptoId)
        val requestPlatform = platformService.retrievePlatformById(userCryptoRequest.platformId!!)
        val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByName(userCryptoRequest.cryptoName!!)

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
        cacheService.invalidateUserCryptosCaches()
        logger.info { "Updated user crypto. Before: $userCrypto | After: $updatedUserCrypto" }

        return updatedUserCrypto.toUserCryptoResponse(
            cryptoName = coingeckoCrypto.name,
            platformName = requestPlatform.name
        )
    }

    fun deleteUserCrypto(userCryptoId: String) {
        val userCrypto = findByUserCryptoId(userCryptoId)
        userCryptoRepository.deleteById(userCryptoId)
        cacheService.invalidateUserCryptosCaches()
        cryptoService.deleteCryptoIfNotUsed(userCrypto.coingeckoCryptoId)

        logger.info { "Deleted user crypto $userCryptoId" }
    }

    fun findByUserCryptoId(userCryptoId: String): UserCrypto {
        return userCryptoRepository.findById(userCryptoId)
            .orElseThrow { throw UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.format(userCryptoId)) }
    }

    fun findByCoingeckoCryptoIdAndPlatformId(cryptoId: String, platformId: String): Optional<UserCrypto> {
        return userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(cryptoId, platformId)
    }

    fun saveOrUpdateAll(userCryptos: List<UserCrypto>) {
        userCryptoRepository.saveAll(userCryptos)
        cacheService.invalidateUserCryptosCaches()
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

    @Cacheable(cacheNames = [USER_CRYPTOS_PAGE_CACHE], key = "#page")
    fun findAllByPage(page: Int): Page<UserCrypto> {
        logger.info { "Retrieving all user cryptos for page $page" }

        return userCryptoRepository.findAll(PageRequest.of(page, 10))
    }

    private fun didChangePlatform(newPlatform: String, originalPlatform: String) = newPlatform != originalPlatform
}

class UserCryptoNotFoundException(message: String) : RuntimeException(message)
class DuplicatedCryptoPlatFormException(message: String) : RuntimeException(message)