package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.UUID

@Document("Platforms")
data class Platform(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Field
    val name: String
) {

    fun toPlatformResponse(): PlatformResponse {
        return PlatformResponse(id, name)
    }
}