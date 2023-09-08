package com.distasilucas.cryptobalancetracker.model.response.insights

data class BalancesResponse(
    val totalUSDBalance: String,
    val totalEURBalance: String,
    val totalBTCBalance: String
)