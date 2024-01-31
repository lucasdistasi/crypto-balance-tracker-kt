import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.coingecko.*
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.math.BigDecimal
import java.time.LocalDateTime

private const val BASE_PATH = "/api/v1"
private const val PLATFORMS_ENDPOINT = "$BASE_PATH/platforms"
private const val USER_CRYPTOS_ENDPOINT = "$BASE_PATH/cryptos"
private const val GOALS_ENDPOINT = "$BASE_PATH/goals"
private const val INSIGHTS_ENDPOINT = "$BASE_PATH/insights"

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
    MockMvcRequestBuilders.get("$GOALS_ENDPOINT/$goalId")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveGoalsForPage(page: Int) = this.perform(
    MockMvcRequestBuilders.get("$GOALS_ENDPOINT?page=$page")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.saveGoal(payload: String) = this.perform(
    MockMvcRequestBuilders.post(GOALS_ENDPOINT)
        .content(payload)
        .contentType(APPLICATION_JSON)
)

fun MockMvc.updateGoal(goalId: String, payload: String) = this.perform(
    MockMvcRequestBuilders.put("$GOALS_ENDPOINT/$goalId")
        .content(payload)
        .contentType(APPLICATION_JSON)
)

fun MockMvc.deleteGoal(goalId: String) = this.perform(
    MockMvcRequestBuilders.delete("$GOALS_ENDPOINT/$goalId")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.transferUserCrypto(payload: String) = this.perform(
    MockMvcRequestBuilders.post("$USER_CRYPTOS_ENDPOINT/transfer")
        .content(payload)
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveTotalBalancesInsights() = this.perform(
    MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/balances")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveUserCryptosInsights(page: Int) = this.perform(
    MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/cryptos?page=$page")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveUserCryptosPlatformsInsights(page: Int) = this.perform(
    MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/cryptos/platforms?page=$page")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveCryptosBalancesInsights() = this.perform(
    MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/cryptos/balances")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrievePlatformsBalancesInsights() = this.perform(
    MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/platforms/balances")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveCryptoInsights(coingeckoCryptoId: String) = this.perform(
    MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/cryptos/$coingeckoCryptoId")
        .contentType(APPLICATION_JSON)
)

fun MockMvc.retrievePlatformInsights(platformId: String) = this.perform(
    MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/platforms/$platformId")
        .contentType(APPLICATION_JSON)
)

fun getCryptoEntity(
    id: String = "bitcoin",
    name: String = "Bitcoin",
    ticker: String = "btc",
    image: String = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
    lastKnownPrice: BigDecimal = BigDecimal("30000"),
    lastKnownPriceInEUR: BigDecimal = BigDecimal("27000"),
    lastKnownPriceInBTC: BigDecimal = BigDecimal("1"),
    circulatingSupply: BigDecimal = BigDecimal("19000000"),
    maxSupply: BigDecimal? = BigDecimal("21000000"),
    marketCapRank: Int = 1,
    marketCap: BigDecimal = BigDecimal("813208997089"),
    changePercentageIn24h: BigDecimal = BigDecimal("10.00"),
    changePercentageIn7d: BigDecimal = BigDecimal("-5.00"),
    changePercentageIn30d: BigDecimal = BigDecimal("0.00"),
    lastUpdatedAt: LocalDateTime = LocalDateTime.now()
): Crypto {
    return Crypto(
        id = id,
        name = name,
        ticker = ticker,
        image = image,
        circulatingSupply = circulatingSupply,
        lastKnownPrice = lastKnownPrice,
        lastKnownPriceInBTC = lastKnownPriceInBTC,
        lastKnownPriceInEUR = lastKnownPriceInEUR,
        maxSupply = maxSupply ?: BigDecimal.ZERO,
        marketCapRank = marketCapRank,
        marketCap = marketCap,
        changePercentageIn24h = changePercentageIn24h,
        changePercentageIn7d = changePercentageIn7d,
        changePercentageIn30d = changePercentageIn30d,
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
    progress: Float = 100f,
    remainingQuantity: BigDecimal = BigDecimal.ZERO,
    goalQuantity: BigDecimal = BigDecimal("1"),
    moneyNeeded: BigDecimal = BigDecimal.ZERO
): GoalResponse {
    return GoalResponse(
        id = id,
        cryptoName = cryptoName,
        actualQuantity = actualQuantity.toPlainString(),
        progress = progress,
        remainingQuantity = remainingQuantity.toPlainString(),
        goalQuantity = goalQuantity.toPlainString(),
        moneyNeeded = moneyNeeded.toPlainString()
    )
}

fun getCoingeckoCryptoInfo(
    id: String = "bitcoin",
    symbol: String = "btc",
    name: String = "Bitcoin",
    marketData: MarketData = getMarketData(),
    image: Image = getImage(),
    marketCapRank: Int = 1
): CoingeckoCryptoInfo {
    return CoingeckoCryptoInfo(
        id = id,
        symbol = symbol,
        name = name,
        image = image,
        marketCapRank = marketCapRank,
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
        maxSupply = maxSupply,
        marketCap = MarketCap(BigDecimal("813208997089")),
        changePercentageIn24h = BigDecimal("10.00"),
        changePercentageIn7d = BigDecimal("-5.00"),
        changePercentageIn30d = BigDecimal("0.00")
    )
}

fun getImage() = Image("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")

fun getCurrentPrince(
    usd: BigDecimal = BigDecimal("30000"),
    eur: BigDecimal = BigDecimal("27000"),
    btc: BigDecimal = BigDecimal("1")
) = CurrentPrice(
    usd = usd,
    eur = eur,
    btc = btc
)

fun balances() = BalancesResponse(
    totalUSDBalance = "100",
    totalBTCBalance = "0.1",
    totalEURBalance = "70"
)