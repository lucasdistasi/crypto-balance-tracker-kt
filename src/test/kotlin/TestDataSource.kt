import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CurrentPrice
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketData
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.math.BigDecimal
import java.time.LocalDateTime

private const val PLATFORMS_ENDPOINT = "/api/v1/platforms"
private const val USER_CRYPTOS_ENDPOINT = "/api/v1/cryptos"
private const val GOALS_ENDPONT = "/api/v1/goals"

fun MockMvc.countPlatforms() = this.perform(
    MockMvcRequestBuilders.get("$PLATFORMS_ENDPOINT/count")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrievePlatform(platformId: String) = this.perform(
    MockMvcRequestBuilders.get("$PLATFORMS_ENDPOINT/$platformId")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveAllPlatforms() = this.perform(
    MockMvcRequestBuilders.get(PLATFORMS_ENDPOINT)
        .contentType(APPLICATION_JSON)
)

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

fun MockMvc.retrieveGoal(goalId: String) = this.perform(
    MockMvcRequestBuilders.get("$GOALS_ENDPONT/$goalId")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveGoalsForPage(page: Int) = this.perform(
    MockMvcRequestBuilders.get("$GOALS_ENDPONT?page=$page")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.saveGoal(payload: String) = this.perform(
    MockMvcRequestBuilders.post(GOALS_ENDPONT)
        .content(payload)
        .contentType(APPLICATION_JSON)
)

fun MockMvc.updateGoal(goalId: String, payload: String) = this.perform(
    MockMvcRequestBuilders.put("$GOALS_ENDPONT/$goalId")
        .content(payload)
        .contentType(APPLICATION_JSON)
)

fun MockMvc.deleteGoal(goalId: String) = this.perform(
    MockMvcRequestBuilders.delete("$GOALS_ENDPONT/$goalId")
        .contentType(APPLICATION_JSON)
)

fun getCryptoEntity(
    id: String = "bitcoin",
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

fun getGoalResponse(
    id: String = "123e4567-e89b-12d3-a456-426614174111",
    cryptoName: String = "Bitcoin",
    actualQuantity: BigDecimal = BigDecimal("1"),
    progress: BigDecimal = BigDecimal("100"),
    remainingQuantity: BigDecimal = BigDecimal.ZERO,
    goalQuantity: BigDecimal = BigDecimal("1"),
    moneyNeeded: BigDecimal = BigDecimal.ZERO
): GoalResponse {
    return GoalResponse(
        id = id,
        cryptoName = cryptoName,
        actualQuantity = actualQuantity,
        progress = progress,
        remainingQuantity = remainingQuantity,
        goalQuantity = goalQuantity,
        moneyNeeded = moneyNeeded
    )
}

fun getCoingeckoCryptoInfo(
    id: String = "bitcoin",
    symbol: String = "btc",
    name: String = "Bitcoin",
    marketData: MarketData = getMarketData()
): CoingeckoCryptoInfo {
    return CoingeckoCryptoInfo(
        id = id,
        symbol = symbol,
        name = name,
        marketData = marketData
    )
}

fun getMarketData(
    currentPrice: CurrentPrice = getCurrentPrince(),
    circulatingSupply: BigDecimal = BigDecimal("19000000"),
    maxSupply: BigDecimal? = BigDecimal("21000000")
): MarketData {
    return MarketData(
        currentPrice = currentPrice,
        circulatingSupply = circulatingSupply,
        maxSupply = maxSupply
    )
}

fun getCurrentPrince(
    usd: BigDecimal = BigDecimal("30000"),
    eur: BigDecimal = BigDecimal("27000"),
    btc: BigDecimal = BigDecimal("1")
) = CurrentPrice(
    usd = usd,
    eur = eur,
    btc = btc
)