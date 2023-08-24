package com.distasilucas.cryptobalancetracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@EnableCaching
@SpringBootApplication
class CryptoBalanceTrackerApplication

fun main(args: Array<String>) {
	runApplication<CryptoBalanceTrackerApplication>(*args)
}
