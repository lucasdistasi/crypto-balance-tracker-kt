package com.distasilucas.cryptobalancetracker.constants

// Cache
const val COINGECKO_CRYPTOS_CACHE = "COINGECKO_CRYPTOS_CACHE"
const val CRYPTO_INFO_CACHE = "CRYPTO_INFO_CACHE"
const val USER_CRYPTOS_CACHE = "USER_CRYPTOS_CACHE"
const val USER_CRYPTOS_PAGE_CACHE = "USER_CRYPTOS_PAGE_CACHE"
const val USER_CRYPTOS_PLATFORM_ID_CACHE = "USER_CRYPTOS_PLATFORM_ID_CACHE"
const val USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE = "USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE"
const val PLATFORMS_PLATFORMS_IDS_CACHE = "PLATFORMS_PLATFORMS_IDS_CACHE"
const val CRYPTO_COINGECKO_CRYPTO_ID_CACHE = "CRYPTO_COINGECKO_CRYPTO_ID_CACHE"
const val CRYPTOS_CRYPTOS_IDS_CACHE = "CRYPTOS_CRYPTOS_IDS_CACHE"

// Validations
const val INVALID_PLATFORM_UUID = "Platform id must be a valid UUID"
const val INVALID_USER_CRYPTO_UUID = "User crypto id must be a valid UUID"
const val INVALID_GOAL_UUID = "Goal id must be a valid UUID"
const val INVALID_PAGE_NUMBER = "Page must be greater than or equal to 0"
const val INVALID_TO_PLATFORM_UUID = "To platform id must be a valid UUID"

// Exceptions
const val UNKNOWN_ERROR = "Unknown error"
const val PLATFORM_ID_NOT_FOUND = "Platform with id %s not found"
const val DUPLICATED_PLATFORM = "Platform %s already exists"
const val USER_CRYPTO_ID_NOT_FOUND = "User crypto with id %s not found"
const val COINGECKO_CRYPTO_NOT_FOUND = "Coingecko crypto with name %s not found"
const val DUPLICATED_CRYPTO_PLATFORM = "You already have %s in %s"
const val GOAL_ID_NOT_FOUND = "Goal with id %s not found"
const val DUPLICATED_GOAL = "You already have a goal for %s"
const val NOT_ENOUGH_BALANCE = "You don't have enough balance to perform this action"
const val SAME_FROM_TO_PLATFORM = "From platform and to platform cannot be the same"
const val TOKEN_EXPIRED = "Token is expired"
const val USERNAME_NOT_FOUND = "Username not found"

// Regex validations
const val PLATFORM_NAME_REGEX = "^[a-zA-Z]{1,24}$"
const val CRYPTO_NAME_REGEX = "^(?! )(?!.* {2})[a-zA-Z0-9]{1,64}(?: [a-zA-Z0-9]{1,64})*$(?<! )"