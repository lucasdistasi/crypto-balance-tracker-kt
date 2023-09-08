package com.distasilucas.cryptobalancetracker.model.response.insights.crypto

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights

data class PageUserCryptosInsightsResponse(
    val page: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val balances: BalancesResponse,
    val cryptos: List<UserCryptosInsights>
) {

    constructor(page: Int, totalPages: Int, balances: BalancesResponse, cryptos: List<UserCryptosInsights>) : this(
        page = page + 1,
        totalPages = totalPages,
        hasNextPage = totalPages - 1 > page,
        balances = balances,
        cryptos = cryptos
    )

}