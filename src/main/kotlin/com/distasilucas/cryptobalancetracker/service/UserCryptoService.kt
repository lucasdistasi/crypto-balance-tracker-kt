package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DUPLICATED_CRYPTO_PLATFORM
import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserCryptoService(
    val userCryptoRepository: UserCryptoRepository,
    val platformService: PlatformService,
    val cryptoService: CryptoService
) {

    private val logger = KotlinLogging.logger { }

    fun retrieveUserCryptoById(userCryptoId: String): UserCryptoResponse {
        logger.info { "Retrieving user crypto with id $userCryptoId" }

        val userCrypto = userCryptoRepository.findById(userCryptoId)
            .orElseThrow { UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.format(userCryptoId)) }

        val crypto = cryptoService.retrieveCryptoInfoById(userCrypto.coingeckoCryptoId)
        val platform = platformService.retrievePlatformById(userCrypto.platformId)

        return userCrypto.toUserCryptoResponse(crypto.name, platform.name)
    }

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
        val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByName(userCryptoRequest.cryptoName!!)
        val platform = platformService.retrievePlatformById(userCryptoRequest.platformId!!)

        userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(coingeckoCrypto.id, userCryptoRequest.platformId)
            .ifPresent {
                throw DuplicatedCryptoPlatFormException(
                    DUPLICATED_CRYPTO_PLATFORM.format(
                        coingeckoCrypto.name,
                        platform.name
                    )
                )
            }

        val userCrypto = UserCrypto(
            coingeckoCryptoId = coingeckoCrypto.id,
            quantity = userCryptoRequest.quantity!!,
            platformId = userCryptoRequest.platformId
        )

        userCryptoRepository.save(userCrypto)
        logger.info { "Saved user crypto $userCrypto" }
        cryptoService.saveCryptoIfNotExists(coingeckoCrypto.id)

        return userCrypto.toUserCryptoResponse(
            cryptoName = coingeckoCrypto.name,
            platformName = platform.name
        )
    }

    fun updateUserCrypto(userCryptoId: String, userCryptoRequest: UserCryptoRequest): UserCryptoResponse {
        val userCrypto = userCryptoRepository.findById(userCryptoId)
            .orElseThrow { UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.format(userCryptoId)) }
        val platform = platformService.retrievePlatformById(userCryptoRequest.platformId!!)

        val coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByName(userCryptoRequest.cryptoName!!)
        userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(coingeckoCrypto.id, userCryptoRequest.platformId)
            .ifPresent {
                throw DuplicatedCryptoPlatFormException(
                    DUPLICATED_CRYPTO_PLATFORM.format(
                        coingeckoCrypto.name,
                        platform.name
                    )
                )
            }

        val updatedUserCrypto = UserCrypto(
            id = userCrypto.id,
            coingeckoCryptoId = userCrypto.coingeckoCryptoId,
            quantity = userCryptoRequest.quantity!!,
            platformId = userCryptoRequest.platformId
        )

        userCryptoRepository.save(updatedUserCrypto)
        logger.info { "Updated user crypto. Before: $userCrypto | After: $updatedUserCrypto" }

        return updatedUserCrypto.toUserCryptoResponse(
            cryptoName = coingeckoCrypto.name,
            platformName = platform.name
        )
    }

    fun deleteUserCrypto(userCryptoId: String) {
        val userCrypto = userCryptoRepository.findById(userCryptoId)

        userCrypto.ifPresentOrElse({
            userCryptoRepository.deleteById(userCryptoId)
            logger.info { "Deleted user crypto $userCrypto" }
        }, { throw UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.format(userCryptoId)) })

        // TODO (prob delete crypto if not used (need goal table for this))
    }
}

class UserCryptoNotFoundException(message: String) : RuntimeException(message)
class DuplicatedCryptoPlatFormException(message: String) : RuntimeException(message)