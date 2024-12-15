package com.distasilucas.cryptobalancetracker.model.response.transaction

import java.io.Serializable

data class PageTransactionsResponse(
  val page: Int,
  val totalPages: Int,
  val hasNextPage: Boolean,
  val transactions: List<TransactionResponse>
) : Serializable {

  constructor(page: Int, totalPages: Int, transactions: List<TransactionResponse>) : this(
    page + 1,
    totalPages,
    totalPages - 1 > page,
    transactions
  )
}
