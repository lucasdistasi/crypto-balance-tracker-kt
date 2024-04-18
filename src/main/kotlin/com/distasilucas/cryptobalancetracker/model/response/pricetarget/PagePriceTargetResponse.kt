package com.distasilucas.cryptobalancetracker.model.response.pricetarget

data class PagePriceTargetResponse(
  val page: Int,
  val totalPages: Int,
  val hasNextPage: Boolean,
  val targets: List<PriceTargetResponse>
) {

  constructor(page: Int, totalPages: Int, targets: List<PriceTargetResponse>) : this(
    page + 1,
    totalPages,
    totalPages - 1 > page,
    targets
  )
}
