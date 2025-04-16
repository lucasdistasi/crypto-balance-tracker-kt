package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.Goal
import org.springframework.data.mongodb.repository.MongoRepository

interface GoalRepository : MongoRepository<Goal, String> {

  fun findByCoingeckoCryptoId(cryptoId: String): Goal?
}
