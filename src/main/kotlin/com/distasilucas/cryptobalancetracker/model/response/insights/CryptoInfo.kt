package com.distasilucas.cryptobalancetracker.model.response.insights

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CryptoInfo(
    val id: String? = null,
    val cryptoName: String,

    @JsonProperty("cryptoId")
    val coingeckoCryptoId: String,
    val symbol: String,
    val image: String
)