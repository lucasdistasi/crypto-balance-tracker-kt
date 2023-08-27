package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface UserCryptoRepository : MongoRepository<UserCrypto, String> {

    fun findByCoingeckoCryptoIdAndPlatformId(coingeckoCryptoId: String, platformId: String): Optional<UserCrypto>
    fun findAllByCoingeckoCryptoId(coingeckoCryptoId: String): List<UserCrypto>
}