package com.distasilucas.cryptobalancetracker.model.response.crypto

data class PageUserCryptoResponse(
    val page: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val cryptos: List<UserCryptoResponse>
) {

    constructor(page: Int, totalPages: Int, cryptos: List<UserCryptoResponse>) : this(
        page + 1,
        totalPages,
        totalPages - 1 > page,
        cryptos
    )
}