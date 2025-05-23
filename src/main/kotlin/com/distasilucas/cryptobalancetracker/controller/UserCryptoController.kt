package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.USER_CRYPTO_ID_UUID
import com.distasilucas.cryptobalancetracker.controller.swagger.UserCryptoControllerAPI
import com.distasilucas.cryptobalancetracker.model.request.crypto.TransferCryptoRequest
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.crypto.TransferCryptoResponse
import com.distasilucas.cryptobalancetracker.model.response.crypto.UserCryptoResponse
import com.distasilucas.cryptobalancetracker.service.TransferCryptoService
import com.distasilucas.cryptobalancetracker.service.UserCryptoService
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/cryptos")
@CrossOrigin(origins = ["\${allowed-origins}"])
class UserCryptoController(
  private val userCryptoService: UserCryptoService,
  private val transferCryptoService: TransferCryptoService
) : UserCryptoControllerAPI {

  @GetMapping("/{userCryptoId}")
  override fun retrieveUserCrypto(
    @PathVariable @UUID(message = USER_CRYPTO_ID_UUID) userCryptoId: String
  ): ResponseEntity<UserCryptoResponse> {
    val userCrypto = userCryptoService.retrieveUserCryptoResponseById(userCryptoId)

    return ResponseEntity.ok(userCrypto)
  }

  @PostMapping
  override fun saveUserCrypto(@Valid @RequestBody userCryptoRequest: UserCryptoRequest): ResponseEntity<UserCryptoResponse> {
    val userCrypto = userCryptoService.saveUserCrypto(userCryptoRequest)

    return ResponseEntity.ok(userCrypto)
  }

  @PutMapping("/{userCryptoId}")
  override fun updateUserCrypto(
    @PathVariable @UUID(message = USER_CRYPTO_ID_UUID) userCryptoId: String,
    @Valid @RequestBody userCryptoRequest: UserCryptoRequest
  ): ResponseEntity<UserCryptoResponse> {
    val updatedUserCrypto = userCryptoService.updateUserCrypto(userCryptoId, userCryptoRequest)

    return ResponseEntity.ok(updatedUserCrypto)
  }

  @DeleteMapping("/{userCryptoId}")
  override fun deleteUserCrypto(
    @PathVariable @UUID(message = USER_CRYPTO_ID_UUID) userCryptoId: String
  ): ResponseEntity<Unit> {
    userCryptoService.deleteUserCrypto(userCryptoId)

    return ResponseEntity.noContent().build()
  }

  @PostMapping("/transfer")
  override fun transferUserCrypto(
    @Valid @RequestBody transferCryptoRequest: TransferCryptoRequest
  ): ResponseEntity<TransferCryptoResponse> {
    val transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest)

    return ResponseEntity.ok(transferCryptoResponse)
  }
}
