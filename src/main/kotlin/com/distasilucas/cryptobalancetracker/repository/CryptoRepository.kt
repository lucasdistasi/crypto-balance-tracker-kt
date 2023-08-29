package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.Crypto
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface CryptoRepository : MongoRepository<Crypto, String> {

    @Aggregation(
        pipeline = [
            "{ \$match: { 'last_updated_at': { \$lte: ?0 } } }",
            "{ \$sort: { 'last_updated_at': 1 } }",
            "{ \$limit: ?1 }"
        ]
    )
    fun findOldestNCryptosByLastPriceUpdate(dateFilter: LocalDateTime, limit: Int): List<Crypto>

}