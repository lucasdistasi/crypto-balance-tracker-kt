package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.PageUserCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.service.UserCryptoService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/cryptos")
class UserCryptoController(
    private val userCryptoService: UserCryptoService
) {

    @GetMapping("/{userCryptoId}")
    fun retrieveUserCrypto(@PathVariable @UUID userCryptoId: String): ResponseEntity<UserCryptoResponse> {
        val userCrypto = userCryptoService.retrieveUserCryptoById(userCryptoId)

        return ResponseEntity.ok(userCrypto)
    }

    @GetMapping
    fun retrieveUserCryptosForPage(@RequestParam @Min(0) page: Int): ResponseEntity<PageUserCryptoResponse> {
        val userCryptosPage = userCryptoService.retrieveUserCryptosByPage(page)

        return if (userCryptosPage.cryptos.isEmpty()) ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build() else ResponseEntity.ok(userCryptosPage)
    }

    @PostMapping
    fun saveUserCrypto(@Valid @RequestBody userCryptoRequest: UserCryptoRequest): ResponseEntity<UserCryptoResponse> {
        val userCrypto = userCryptoService.saveUserCrypto(userCryptoRequest)

        return ResponseEntity.ok(userCrypto)
    }

    @PutMapping("/{userCryptoId}")
    fun updateUserCrypto(
        @PathVariable @UUID userCryptoId: String,
        @Valid @RequestBody userCryptoRequest: UserCryptoRequest
    ): ResponseEntity<UserCryptoResponse> {
        val updatedUserCrypto = userCryptoService.updateUserCrypto(userCryptoId, userCryptoRequest)

        return ResponseEntity.ok(updatedUserCrypto)
    }

    @DeleteMapping("/{userCryptoId}")
    fun deleteUserCrypto(@PathVariable @UUID userCryptoId: String): ResponseEntity<Unit> {
        userCryptoService.deleteUserCrypto(userCryptoId)

        return ResponseEntity.ok().build()
    }
}