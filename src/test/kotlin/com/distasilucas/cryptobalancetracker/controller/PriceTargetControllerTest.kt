package com.distasilucas.cryptobalancetracker.controller

import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import com.distasilucas.cryptobalancetracker.service.PriceTargetService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.math.BigDecimal

class PriceTargetControllerTest {

  private val priceTargetServiceMock = mockk<PriceTargetService>()
  private val priceTargetController = PriceTargetController(priceTargetServiceMock)

  @Test
  fun `should retrieve price target with status 200`() {
    val priceTargetResponse = PriceTargetResponse(
      "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
      "Bitcoin",
      "60000",
      "100000",
      35.30F
    )

    every { priceTargetServiceMock.retrievePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08") } returns priceTargetResponse

    val responseEntity = priceTargetController.retrievePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(priceTargetResponse))
  }

  @Test
  fun `should retrieve price targets for page with status 200`() {
    val pagePriceTargetResponse = PagePriceTargetResponse(
      0,
      1,
      listOf(
        PriceTargetResponse(
          "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
          "Bitcoin",
          "60000",
          "100000",
          35.30F
        )
      )
    )

    every { priceTargetServiceMock.retrievePriceTargetsByPage(0) } returns pagePriceTargetResponse

    val responseEntity = priceTargetController.retrievePriceTargetsByPage(0)

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(pagePriceTargetResponse))
  }

  @Test
  fun `should retrieve empty price targets for page with status 204`() {
    val pagePriceTargetResponse = PagePriceTargetResponse(0, 1, emptyList())

    every { priceTargetServiceMock.retrievePriceTargetsByPage(0) } returns pagePriceTargetResponse

    val responseEntity = priceTargetController.retrievePriceTargetsByPage(0)

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<PagePriceTargetResponse>())
  }

  @Test
  fun `should save price target with status 201`() {
    val priceTargetRequest = PriceTargetRequest("Bitcoin", BigDecimal("120000"))
    val priceTargetResponse = PriceTargetResponse("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "Bitcoin", "60000", "120000", 35F)

    every { priceTargetServiceMock.savePriceTarget(priceTargetRequest) } returns priceTargetResponse

    val responseEntity = priceTargetController.savePriceTarget(priceTargetRequest)

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.status(HttpStatus.CREATED).body(priceTargetResponse))
  }

  @Test
  fun `should update price target with status 200`() {
    val priceTargetRequest = PriceTargetRequest("Bitcoin", BigDecimal("150000"))
    val priceTargetResponse = PriceTargetResponse("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "Bitcoin", "60000", "150000", 40F)

    every {
      priceTargetServiceMock.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)
    } returns priceTargetResponse

    val responseEntity = priceTargetController.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.ok(priceTargetResponse))
  }

  @Test
  fun `should delete price target with status 204`() {
    justRun { priceTargetServiceMock.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08") }

    val responseEntity = priceTargetController.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")

    assertThat(responseEntity)
      .usingRecursiveComparison()
      .isEqualTo(ResponseEntity.noContent().build<Unit>())
  }

}
