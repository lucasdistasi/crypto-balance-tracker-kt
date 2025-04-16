package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import org.springframework.data.mongodb.repository.MongoRepository

interface UserCryptoRepository : MongoRepository<UserCrypto, String> {

  fun findByCoingeckoCryptoIdAndPlatformId(coingeckoCryptoId: String, platformId: String): UserCrypto?
  fun findAllByCoingeckoCryptoId(coingeckoCryptoId: String): List<UserCrypto>
  fun findAllByPlatformId(platformId: String): List<UserCrypto>
}
