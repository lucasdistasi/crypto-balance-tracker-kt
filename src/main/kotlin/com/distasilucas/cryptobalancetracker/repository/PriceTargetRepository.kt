package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import org.springframework.data.mongodb.repository.MongoRepository
import java.math.BigDecimal
import java.util.Optional

interface PriceTargetRepository : MongoRepository<PriceTarget, String> {

  fun findByCoingeckoCryptoIdAndTarget(coingeckoCryptoId: String, target: BigDecimal): Optional<PriceTarget>
}
