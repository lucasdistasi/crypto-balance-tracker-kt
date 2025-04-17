package com.distasilucas.cryptobalancetracker.model.response.insights.crypto

import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import java.io.Serializable

data class CryptoInsightResponse(
    val cryptoName: String,
    val balances: Balances,
    val platforms: List<PlatformInsight>
): Serializable

data class PlatformInsight(
    val quantity: String,
    val balances: Balances,
    val percentage: Float,
    val platformName: String
): Serializable
