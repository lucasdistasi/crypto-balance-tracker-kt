package com.distasilucas.cryptobalancetracker.model.response.insights.crypto

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights

data class CryptosBalancesInsightsResponse(
    val balances: BalancesResponse,
    val cryptos: List<CryptoInsights>
)