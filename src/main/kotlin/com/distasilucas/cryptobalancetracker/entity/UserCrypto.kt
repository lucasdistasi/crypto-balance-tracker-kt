package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable
import java.math.BigDecimal
import java.util.UUID

@Document("UserCryptos")
data class UserCrypto(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Field("crypto_id")
    val coingeckoCryptoId: String,

    val quantity: BigDecimal,

    @Field("platform_id")
    val platformId: String

): Serializable {

    fun toUserCryptoResponse(cryptoName: String, platformName: String): UserCryptoResponse {
        return UserCryptoResponse(
            id = id,
            quantity = quantity.toPlainString(),
            cryptoName = cryptoName,
            platform = platformName
        )
    }
}