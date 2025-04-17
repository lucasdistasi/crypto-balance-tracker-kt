package com.distasilucas.cryptobalancetracker.model.response.insights.crypto

import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptoInsights

data class PageUserCryptosInsightsResponse(
    val page: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val balances: Balances,
    val cryptos: List<UserCryptoInsights>
) {

  constructor(page: Int, totalPages: Int, balances: Balances, cryptos: List<UserCryptoInsights>) : this(
    page = page + 1,
    totalPages = totalPages,
    hasNextPage = totalPages - 1 > page,
    balances = balances,
    cryptos = cryptos
  )

  companion object {
    val EMPTY = PageUserCryptosInsightsResponse(0, 0, false, Balances.EMPTY, emptyList())
  }

}
