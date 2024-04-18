package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.constants.INVALID_PAGE_NUMBER
import com.distasilucas.cryptobalancetracker.constants.INVALID_PRICE_TARGET_UUID
import com.distasilucas.cryptobalancetracker.controller.swagger.PriceTargetControllerAPI
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import com.distasilucas.cryptobalancetracker.service.PriceTargetService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.UUID
import org.springframework.http.HttpStatus
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/price-targets")
@CrossOrigin(origins = ["\${allowed-origins}"])
class PriceTargetController(
  private val priceTargetService: PriceTargetService
): PriceTargetControllerAPI {

  @GetMapping("/{priceTargetId}")
  override fun retrievePriceTarget(
    @PathVariable @UUID(message = INVALID_PRICE_TARGET_UUID) priceTargetId: String
  ): ResponseEntity<PriceTargetResponse> {
    val priceTarget = priceTargetService.retrievePriceTarget(priceTargetId)

    return ResponseEntity.ok(priceTarget)
  }

  @GetMapping
  override fun retrievePriceTargetsByPage(
    @RequestParam @Min(value = 0, message = INVALID_PAGE_NUMBER) page: Int
  ): ResponseEntity<PagePriceTargetResponse> {
    val priceTargets = priceTargetService.retrievePriceTargetsByPage(page)

    return if (priceTargets.targets.isEmpty())
      ResponseEntity.noContent().build() else ResponseEntity.ok(priceTargets)
  }

  @PostMapping
  override fun savePriceTarget(
    @RequestBody @Valid priceTargetRequest: PriceTargetRequest
  ): ResponseEntity<PriceTargetResponse> {
    val priceTarget = priceTargetService.savePriceTarget(priceTargetRequest)

    return ResponseEntity.status(HttpStatus.CREATED).body(priceTarget)
  }

  @PutMapping("/{priceTargetId}")
  override fun updatePriceTarget(
    @PathVariable @UUID(message = INVALID_PRICE_TARGET_UUID) priceTargetId: String,
    @Valid @RequestBody priceTargetRequest: PriceTargetRequest
  ): ResponseEntity<PriceTargetResponse> {
    val priceTarget = priceTargetService.updatePriceTarget(priceTargetId, priceTargetRequest)

    return ResponseEntity.ok(priceTarget)
  }

  @DeleteMapping("/{priceTargetId}")
  override fun deletePriceTarget(
    @PathVariable @UUID(message = INVALID_PRICE_TARGET_UUID) priceTargetId: String
  ): ResponseEntity<Unit> {
    priceTargetService.deletePriceTarget(priceTargetId)

    return ResponseEntity.noContent().build()
  }
}
