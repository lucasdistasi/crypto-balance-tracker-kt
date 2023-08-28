package com.distasilucas.cryptobalancetracker.constants

// Cache
const val COINGECKO_CRYPTOS_CACHE = "COINGECKO_CRYPTOS"
const val CRYPTO_INFO_CACHE = "CRYPTO_INFO"

// Validations
const val INVALID_PLATFORM_UUID = "Platform id must be a valid UUID"
const val INVALID_USER_CRYPTO_UUID = "User crypto id must be a valid UUID"
const val INVALID_GOAL_UUID = "Goal id must be a valid UUID"
const val INVALID_PAGE_NUMBER = "Page must be greater than or equal to 0"

// Exceptions
const val PLATFORM_ID_NOT_FOUND = "Platform with id %s not found"
const val DUPLICATED_PLATFORM = "Platform %s already exists"
const val USER_CRYPTO_ID_NOT_FOUND = "User crypto with id %s not found"
const val COINGECKO_CRYPTO_NOT_FOUND = "Coingecko crypto with name %s not found"
const val DUPLICATED_CRYPTO_PLATFORM = "You already have %s in %s"
const val GOAL_ID_NOT_FOUND = "Goal with id %s not found"
const val DUPLICATED_GOAL = "You already have a goal for %s"

// Regex validations
const val PLATFORM_NAME_REGEX = "^[a-zA-Z]{1,24}$"
const val CRYPTO_NAME_REGEX = "^(?! )(?!.* {2})[a-zA-Z0-9]{1,64}(?: [a-zA-Z0-9]{1,64})*$(?<! )"