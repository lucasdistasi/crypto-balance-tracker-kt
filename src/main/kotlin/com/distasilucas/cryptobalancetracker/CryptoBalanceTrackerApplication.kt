package com.distasilucas.cryptobalancetracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@EnableRetry
@EnableCaching
@EnableScheduling
@EnableMethodSecurity
@SpringBootApplication
class CryptoBalanceTrackerApplication

fun main(args: Array<String>) {
	runApplication<CryptoBalanceTrackerApplication>(*args)
}
