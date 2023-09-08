package com.distasilucas.cryptobalancetracker.model.response.insights

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonInclude(Include.NON_NULL)
data class CryptoInsights(
    val cryptoName: String,
    val cryptoId: String?,
    val quantity: String?,
    val balances: BalancesResponse,
    val percentage: Float
) {
    constructor(cryptoName: String, balances: BalancesResponse, percentage: Float) : this(
        cryptoName,
        null,
        null,
        balances,
        percentage
    )
}