package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.Platform
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface PlatformRepository : MongoRepository<Platform, String> {

    fun findByName(platformName: String): Optional<Platform>
    fun findAllByIdIn(ids: Collection<String>): List<Platform>
}