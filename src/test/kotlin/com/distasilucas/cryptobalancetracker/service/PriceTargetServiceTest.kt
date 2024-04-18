package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.PriceTarget
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse
import com.distasilucas.cryptobalancetracker.repository.PriceTargetRepository
import getCryptoEntity
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.*

class PriceTargetServiceTest {

  private val priceTargetRepositoryMock = mockk<PriceTargetRepository>()
  private val cryptoServiceMock = mockk<CryptoService>()
  private val priceTargetService = PriceTargetService(priceTargetRepositoryMock, cryptoServiceMock)

  @Test
  fun `should retrieve price target by id`() {
    val priceTarget = PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", BigDecimal("120000"))

    every { priceTargetRepositoryMock.findById(priceTarget.id) } returns Optional.of(priceTarget)
    every {
      cryptoServiceMock.retrieveCryptoInfoById(priceTarget.coingeckoCryptoId)
    } returns getCryptoEntity(lastKnownPrice = BigDecimal("60000"))

    val priceTargetResponse = priceTargetService.retrievePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")

    assertThat(priceTargetResponse)
      .usingRecursiveComparison()
      .isEqualTo(PriceTargetResponse(
        "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
        "Bitcoin",
        "60000",
        "120000",
        100F
      ))
  }

  @Test
  fun `should throw PriceTargetNotFoundException when retrieving price target by id`() {
    every { priceTargetRepositoryMock.findById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08") } returns Optional.empty()

    val exception = assertThrows<PriceTargetNotFoundException> {
      priceTargetService.retrievePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")
    }

    assertEquals("Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found", exception.message)
  }

  @Test
  fun `should retrieve price targets by page`() {
    val pageRequest = PageRequest.of(0, 10)
    val priceTargets = listOf(
      PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", BigDecimal("120000")),
      PriceTarget("ff738ff7-6f9a-400a-8b06-36b7e1fef81e", "ethereum", BigDecimal("15000")),
    )
    val pagePriceTargetResponse = PagePriceTargetResponse(
      0,
      1,
      listOf(
        PriceTargetResponse(
          "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
          "Bitcoin",
          "60000",
          "120000",
          100F
        ),
        PriceTargetResponse(
          "ff738ff7-6f9a-400a-8b06-36b7e1fef81e",
          "Ethereum",
          "4000",
          "15000",
          275F
        )
      )
    )

    every { priceTargetRepositoryMock.findAll(pageRequest) } returns PageImpl(priceTargets)
    every {
      cryptoServiceMock.retrieveCryptoInfoById("bitcoin")
    } returns getCryptoEntity(lastKnownPrice = BigDecimal("60000"))
    every {
      cryptoServiceMock.retrieveCryptoInfoById("ethereum")
    } returns getCryptoEntity(name = "Ethereum", lastKnownPrice = BigDecimal("4000"))

    val priceTargetResponse = priceTargetService.retrievePriceTargetsByPage(0)

    assertThat(priceTargetResponse)
      .usingRecursiveComparison()
      .isEqualTo(pagePriceTargetResponse)
  }

  @Test
  fun `should save price target`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("120000"))
    val coingeckoCrypto = CoingeckoCrypto("bitcoin", "btc", "Bitcoin")
    val priceTargetEntity = PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", priceTargetRequest.priceTarget!!)

    every {
      cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId!!)
    } returns coingeckoCrypto
    every {
      priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget!!)
    } returns Optional.empty()
    every {
      cryptoServiceMock.retrieveCryptoInfoById("bitcoin")
    } returns getCryptoEntity(lastKnownPrice = BigDecimal("60000"))
    mockkStatic(UUID::class)
    every { UUID.randomUUID().toString() } returns priceTargetEntity.id
    every { priceTargetRepositoryMock.save(priceTargetEntity) } returns priceTargetEntity

    val priceTargetResponse = priceTargetService.savePriceTarget(priceTargetRequest)

    assertThat(priceTargetResponse)
      .usingRecursiveComparison()
      .isEqualTo(PriceTargetResponse(
        "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
        "Bitcoin",
        "60000",
        "120000",
        100F
      ))
    verify(exactly = 1) { priceTargetRepositoryMock.save(priceTargetEntity) }
  }

  @Test
  fun `should throw DuplicatedPriceTargetException when adding price target`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("120000"))
    val coingeckoCrypto = CoingeckoCrypto("bitcoin", "btc", "Bitcoin")
    val priceTargetEntity = PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", priceTargetRequest.priceTarget!!)

    every {
      cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId!!)
    } returns coingeckoCrypto
    every {
      priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget!!)
    } returns Optional.of(priceTargetEntity)

    val exception = assertThrows<DuplicatedPriceTargetException> { priceTargetService.savePriceTarget(priceTargetRequest) }

    assertEquals("You already have a price target for ${coingeckoCrypto.id} at that price", exception.message)
  }

  @Test
  fun `should update price target`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("100000"))
    val priceTargetEntity = PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", priceTargetRequest.priceTarget!!)

    every { priceTargetRepositoryMock.findById(priceTargetEntity.id) } returns Optional.of(priceTargetEntity)
    every {
      priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget!!)
    } returns Optional.empty()
    every {
      cryptoServiceMock.retrieveCryptoInfoById(priceTargetRequest.cryptoNameOrId!!)
    } returns getCryptoEntity(lastKnownPrice = BigDecimal("60000"))
    every { priceTargetRepositoryMock.save(priceTargetEntity) } returns priceTargetEntity

    val priceTargetResponse = priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)

    assertThat(priceTargetResponse)
      .usingRecursiveComparison()
      .isEqualTo(PriceTargetResponse(
        "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
        "Bitcoin",
        "60000",
        "100000",
        66.70F
      ))
    verify(exactly = 1) { priceTargetRepositoryMock.save(priceTargetEntity) }
  }

  @Test
  fun `should throw DuplicatedPriceTargetException when updating price target`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("100000"))
    val priceTargetEntity = PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", priceTargetRequest.priceTarget!!)
    val anotherSamePriceTargetEntity = PriceTarget("6fda6f49-9070-4ffa-b9ea-ac52316110d7", "bitcoin", priceTargetRequest.priceTarget!!)

    every { priceTargetRepositoryMock.findById(priceTargetEntity.id) } returns Optional.of(priceTargetEntity)
    every {
      priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget!!)
    } returns Optional.of(anotherSamePriceTargetEntity)

    val exception = assertThrows<DuplicatedPriceTargetException> {
      priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)
    }

    assertEquals("You already have a price target for ${priceTargetEntity.coingeckoCryptoId} at that price", exception.message)
  }

  @Test
  fun `should throw PriceTargetNotFoundException when updating price target`() {
    val priceTargetRequest = PriceTargetRequest("bitcoin", BigDecimal("100000"))

    every { priceTargetRepositoryMock.findById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08") } returns Optional.empty()

    val exception = assertThrows<PriceTargetNotFoundException> {
      priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)
    }

    assertEquals("Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found", exception.message)
    verify(exactly = 0) { priceTargetRepositoryMock.save(any()) }
  }

  @Test
  fun `should delete price target`() {
    val priceTargetEntity = PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", BigDecimal("120000"))

    every { priceTargetRepositoryMock.findById(priceTargetEntity.id) } returns Optional.of(priceTargetEntity)
    justRun { priceTargetRepositoryMock.delete(priceTargetEntity) }
    justRun { cryptoServiceMock.deleteCryptoIfNotUsed("bitcoin") }

    priceTargetService.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")

    verify(exactly = 1) { priceTargetRepositoryMock.delete(priceTargetEntity) }
    verify(exactly = 1) { cryptoServiceMock.deleteCryptoIfNotUsed("bitcoin") }
  }

  @Test
  fun `should throw PriceTargetNotFoundException when deleting price target`() {
    every { priceTargetRepositoryMock.findById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08") } returns Optional.empty()

    val exception = assertThrows<PriceTargetNotFoundException> {
      priceTargetService.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")
    }

    assertEquals("Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found", exception.message)
    verify(exactly = 0) { priceTargetRepositoryMock.delete(any()) }
    verify(exactly = 0) { cryptoServiceMock.deleteCryptoIfNotUsed(any()) }
  }
}
