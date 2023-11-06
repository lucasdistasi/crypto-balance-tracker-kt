package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.USERNAME_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.User
import com.distasilucas.cryptobalancetracker.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class UserService(private val userRepository: UserRepository) {

    private val logger = KotlinLogging.logger { }

    fun findByUsername(username: String): User {
        logger.info { "Searching for username $username" }

        return userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException() }
    }
}

class UsernameNotFoundException : RuntimeException(USERNAME_NOT_FOUND)