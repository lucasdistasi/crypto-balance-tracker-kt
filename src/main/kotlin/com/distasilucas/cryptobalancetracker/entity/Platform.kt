package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable

@Document("Platforms")
data class Platform(
  @Id
  val id: String,

  @Field
  val name: String
) : Serializable {

  fun toPlatformResponse(): PlatformResponse {
    return PlatformResponse(id, name)
  }
}
