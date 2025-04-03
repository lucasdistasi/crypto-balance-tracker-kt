package com.distasilucas.cryptobalancetracker.model

import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights
import java.math.BigDecimal

enum class SortBy(
  private val userCryptosInsightsComparator: Comparator<UserCryptosInsights>
) {
  PERCENTAGE(Comparator.comparing { it.percentage }),
  CURRENT_PRICE(Comparator.comparing { BigDecimal(it.cryptoInfo.currentPrice.usd) }),
  CHANGE_PRICE_IN_24H(Comparator.comparing { it.cryptoInfo.priceChange.changePercentageIn24h }),
  CHANGE_PRICE_IN_7D(Comparator.comparing { it.cryptoInfo.priceChange.changePercentageIn7d }),
  CHANGE_PRICE_IN_30D(Comparator.comparing { it.cryptoInfo.priceChange.changePercentageIn30d });

  fun getUserCryptosInsightsComparator(sortType: SortType): Comparator<UserCryptosInsights> {
    return if (sortType == SortType.ASC) userCryptosInsightsComparator else userCryptosInsightsComparator.reversed()
  }
}

enum class SortType {
  ASC,
  DESC
}

data class SortParams(
  val sortBy: SortBy,
  val sortType: SortType
) {

  fun cryptosInsightsResponseComparator(): Comparator<UserCryptosInsights> {
    return sortBy.getUserCryptosInsightsComparator(sortType)
  }
}
