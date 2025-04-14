package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import org.springframework.data.mongodb.repository.MongoRepository
import java.math.BigDecimal

interface PriceTargetRepository : MongoRepository<PriceTarget, String> {

  fun findByCoingeckoCryptoIdAndTarget(coingeckoCryptoId: String, target: BigDecimal): PriceTarget?
  fun findAllByCoingeckoCryptoId(coingeckoCryptoId: String): List<PriceTarget>
}
