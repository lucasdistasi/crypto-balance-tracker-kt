package com.distasilucas.cryptobalancetracker.repository

import com.distasilucas.cryptobalancetracker.entity.Crypto
import org.springframework.data.mongodb.repository.MongoRepository

interface CryptoRepository : MongoRepository<Crypto, String>