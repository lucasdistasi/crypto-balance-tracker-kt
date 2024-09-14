package com.distasilucas.cryptobalancetracker.model.response.insights

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class CryptoInsights(
  val id: String?,
  val cryptoName: String,
  val cryptoId: String?,
  val quantity: String?,
  val balances: BalancesResponse,
  val percentage: Float
): Serializable {
  constructor(cryptoName: String, balances: BalancesResponse, percentage: Float) : this(
    null,
    cryptoName,
    null,
    null,
    balances,
    percentage
  )

  constructor(cryptoName: String, cryptoId: String, quantity: String, balances: BalancesResponse, percentage: Float) : this(
    null,
    cryptoName,
    cryptoId,
    quantity,
    balances,
    percentage
  )
}
