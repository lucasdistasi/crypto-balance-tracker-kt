package com.distasilucas.cryptobalancetracker.constants

// Cache
const val COINGECKO_CRYPTOS_CACHE = "COINGECKO_CRYPTOS_CACHE"
const val CRYPTO_INFO_CACHE = "CRYPTO_INFO_CACHE"
const val USER_CRYPTOS_CACHE = "USER_CRYPTOS_CACHE"
const val USER_CRYPTOS_PLATFORM_ID_CACHE = "USER_CRYPTOS_PLATFORM_ID_CACHE"
const val USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE = "USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE"
const val PLATFORMS_PLATFORMS_IDS_CACHE = "PLATFORMS_PLATFORMS_IDS_CACHE"
const val CRYPTO_COINGECKO_CRYPTO_ID_CACHE = "CRYPTO_COINGECKO_CRYPTO_ID_CACHE"
const val CRYPTOS_CRYPTOS_IDS_CACHE = "CRYPTOS_CRYPTOS_IDS_CACHE"
const val ALL_PLATFORMS_CACHE = "ALL_PLATFORMS_CACHE"
const val PLATFORM_PLATFORM_ID_CACHE = "PLATFORM_PLATFORM_ID_CACHE"

// Validations
const val PLATFORM_ID_UUID = "Platform id must be a valid UUID"
const val PLATFORM_ID_NOT_BLANK = "Platform id can not be null or blank"
const val USER_CRYPTO_ID_UUID = "User crypto id must be a valid UUID"
const val USER_CRYPTO_ID_NOT_BLANK = "User crypto id can not be null or blank"
const val QUANTITY_TO_TRANSFER_NOT_NULL = "Quantity to transfer can not be null"
const val QUANTITY_TO_TRANSFER_DIGITS = "Quantity to transfer must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
const val QUANTITY_TO_TRANSFER_DECIMAL_MAX = "Quantity to transfer must be less than or equal to 9999999999999999.999999999999"
const val QUANTITY_TO_TRANSFER_POSITIVE = "Quantity to transfer must be greater than 0"
const val NETWORK_FEE_NOT_NULL = "Network fee can not be null"
const val NETWORK_FEE_DIGITS = "Network fee must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
const val NETWORK_FEE_MIN = "Network fee must be greater than or equal to 0"
const val TO_PLATFORM_ID_NOT_BLANK = "To platform id can not be null or blank"
const val TO_PLATFORM_ID_UUID = "To platform id must be a valid UUID"
const val INVALID_GOAL_UUID = "Goal id must be a valid UUID"
const val INVALID_PAGE_NUMBER = "Page must be greater than or equal to 0"
const val CRYPTO_NAME_NOT_BLANK = "Crypto name can not be null or blank"
const val CRYPTO_NAME_SIZE = "Crypto name must be between 1 and 64 characters"
const val CRYPTO_QUANTITY_NOT_NULL = "Crypto quantity can not be null"
const val CRYPTO_QUANTITY_DIGITS = "Crypto quantity must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
const val CRYPTO_QUANTITY_DECIMAL_MAX = "Crypto quantity must be less than or equal to 9999999999999999.999999999999"
const val CRYPTO_QUANTITY_POSITIVE = "Crypto quantity must be greater than 0"

// Exceptions
const val UNKNOWN_ERROR = "Unknown error"
const val PLATFORM_ID_NOT_FOUND = "Platform with id %s not found"
const val DUPLICATED_PLATFORM = "Platform %s already exists"
const val USER_CRYPTO_ID_NOT_FOUND = "User crypto with id %s not found"
const val COINGECKO_CRYPTO_NOT_FOUND = "Coingecko crypto [%s] not found"
const val DUPLICATED_CRYPTO_PLATFORM = "You already have %s in %s"
const val GOAL_ID_NOT_FOUND = "Goal with id %s not found"
const val DUPLICATED_GOAL = "You already have a goal for %s"
const val NOT_ENOUGH_BALANCE = "You don't have enough balance to perform this action"
const val SAME_FROM_TO_PLATFORM = "From platform and to platform cannot be the same"
const val TOKEN_EXPIRED = "Token is expired"
const val USERNAME_NOT_FOUND = "Username not found"
const val REQUEST_LIMIT_REACHED = "Request limit reached"
const val INVALID_VALUE_FOR = "Invalid value %s for %s. Available values: %s"

// Regex validations
const val PLATFORM_NAME_REGEX = "^[a-zA-Z](?:(?!\\s{2,})[a-zA-Z\\s]){0,22}[a-zA-Z]$"
const val CRYPTO_NAME_REGEX = "^(?!\\s)(?!.*\\s{2,})[^\\s].*?(?<!\\s)$"
