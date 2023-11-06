package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.User
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface UserRepository: MongoRepository<User, String> {

    fun findByUsername(username: String): Optional<User>
}