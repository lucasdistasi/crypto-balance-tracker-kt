import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.math.BigDecimal
import java.time.LocalDateTime

private const val PLATFORMS_ENDPOINT = "/api/v1/platforms"
private const val USER_CRYPTOS_ENDPOINT = "/api/v1/cryptos"

fun MockMvc.countPlatforms() = this.perform(
    MockMvcRequestBuilders.get("$PLATFORMS_ENDPOINT/count")
        .contentType(APPLICATION_JSON))

fun MockMvc.retrievePlatform(platformId: String) = this.perform(
    MockMvcRequestBuilders.get("$PLATFORMS_ENDPOINT/$platformId")
        .contentType(APPLICATION_JSON))

fun MockMvc.retrieveAllPlatforms() = this.perform(
    MockMvcRequestBuilders.get(PLATFORMS_ENDPOINT)
        .contentType(APPLICATION_JSON))

fun MockMvc.savePlatform(payload: String) = this.perform(
    MockMvcRequestBuilders.post(PLATFORMS_ENDPOINT)
        .contentType(APPLICATION_JSON)
        .content(payload)
)

fun MockMvc.updatePlatform(platformId: String, payload: String) = this.perform(
    MockMvcRequestBuilders.put("$PLATFORMS_ENDPOINT/$platformId")
        .contentType(APPLICATION_JSON)
        .content(payload)
)

fun MockMvc.deletePlatform(platformId: String) = this.perform(
    MockMvcRequestBuilders.delete("$PLATFORMS_ENDPOINT/$platformId")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveUserCrypto(userCryptoId: String) = this.perform(
    MockMvcRequestBuilders.get("$USER_CRYPTOS_ENDPOINT/$userCryptoId")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveUserCryptosForPage(page: Int) = this.perform(
    MockMvcRequestBuilders.get("$USER_CRYPTOS_ENDPOINT?page=$page")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.saveUserCrypto(payload: String) = this.perform(
    MockMvcRequestBuilders.post(USER_CRYPTOS_ENDPOINT)
        .content(payload)
        .contentType(APPLICATION_JSON)
)

fun MockMvc.updateUserCrypto(userCryptoId: String, payload: String) = this.perform(
    MockMvcRequestBuilders.put("$USER_CRYPTOS_ENDPOINT/$userCryptoId")
        .content(payload)
        .contentType(APPLICATION_JSON)
)

fun MockMvc.deleteUserCrypto(userCryptoId: String) = this.perform(
    MockMvcRequestBuilders.delete("$USER_CRYPTOS_ENDPOINT/$userCryptoId")
        .contentType(APPLICATION_JSON)
)

fun getCryptoEntity(
    id: String = "123e4567-e89b-12d3-a456-426614174000",
    name: String = "Bitcoin",
    ticker: String = "btc",
    lastKnownPrice: BigDecimal = BigDecimal("30000"),
    lastKnownPriceInEUR: BigDecimal = BigDecimal("27000"),
    lastKnownPriceInBTC: BigDecimal = BigDecimal("1"),
    circulatingSupply: BigDecimal = BigDecimal("19000000"),
    maxSupply: BigDecimal = BigDecimal("21000000"),
    lastUpdatedAt: LocalDateTime = LocalDateTime.now()
): Crypto {
    return Crypto(
        id = id,
        name = name,
        ticker = ticker,
        circulatingSupply = circulatingSupply,
        lastKnownPrice = lastKnownPrice,
        lastKnownPriceInBTC = lastKnownPriceInBTC,
        lastKnownPriceInEUR = lastKnownPriceInEUR,
        maxSupply = maxSupply,
        lastUpdatedAt = lastUpdatedAt
    )
}

fun getUserCrypto(
    id: String = "123e4567-e89b-12d3-a456-426614174000",
    coingeckoCryptoId: String = "bitcoin",
    quantity: BigDecimal = BigDecimal("0.25"),
    platformId: String = "123e4567-e89b-12d3-a456-426614174111"
): UserCrypto {
    return UserCrypto(
        id = id,
        coingeckoCryptoId = coingeckoCryptoId,
        quantity = quantity,
        platformId = platformId
    )
}

fun getUserCryptoRequest(
    cryptoName: String = "bitcoin",
    quantity: BigDecimal = BigDecimal("1"),
    platformId: String = "123e4567-e89b-12d3-a456-426614174111"
): UserCryptoRequest {
    return UserCryptoRequest(
        cryptoName = cryptoName,
        quantity = quantity,
        platformId = platformId
    )
}

fun getPlatformResponse(
    id: String = "123e4567-e89b-12d3-a456-426614174111",
    name: String = "BINANCE"
): PlatformResponse {
    return PlatformResponse(
        id = id,
        name = name.uppercase()
    )
}

fun getCoingeckoCrypto(
    id: String = "bitcoin",
    symbol: String = "btc",
    name: String = "Bitcoin"
): CoingeckoCrypto {
    return CoingeckoCrypto(
        id = id,
        symbol = symbol,
        name = name
    )
}