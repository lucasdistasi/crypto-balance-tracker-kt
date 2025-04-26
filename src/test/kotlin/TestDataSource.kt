import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.request.crypto.UserCryptoRequest
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CurrentPrice
import com.distasilucas.cryptobalancetracker.model.response.coingecko.Image
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketCap
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketData
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
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
private const val PRICE_TARGET_ENDPOINT = "$BASE_PATH/price-targets"

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

fun MockMvc.retrieveHomeInsights() = this.perform(
  MockMvcRequestBuilders.get(INSIGHTS_ENDPOINT)
    .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveDatesBalances(dateRange: DateRange) = this.perform(
  MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/dates-balances")
    .param("dateRange", dateRange.name)
    .contentType(APPLICATION_JSON)
)

fun MockMvc.retrieveUserCryptosPlatformsInsights(page: Int) = this.perform(
  MockMvcRequestBuilders.get("$INSIGHTS_ENDPOINT/cryptos?page=$page")
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

fun MockMvc.retrievePriceTarget(priceTargetId: String) = this.perform(
  MockMvcRequestBuilders.get("$PRICE_TARGET_ENDPOINT/$priceTargetId")
    .contentType(APPLICATION_JSON)
)

fun MockMvc.retrievePriceTargetsForPage(page: Int) = this.perform(
  MockMvcRequestBuilders.get("$PRICE_TARGET_ENDPOINT?page=$page")
    .contentType(APPLICATION_JSON)
)

fun MockMvc.savePriceTarget(payload: String) = this.perform(
  MockMvcRequestBuilders.post(PRICE_TARGET_ENDPOINT)
    .content(payload)
    .contentType(APPLICATION_JSON)
)

fun MockMvc.updatePriceTarget(priceTargetId: String, payload: String) = this.perform(
  MockMvcRequestBuilders.put("$PRICE_TARGET_ENDPOINT/$priceTargetId")
    .content(payload)
    .contentType(APPLICATION_JSON)
)

fun MockMvc.deletePriceTarget(priceTargetId: String) = this.perform(
  MockMvcRequestBuilders.delete("$PRICE_TARGET_ENDPOINT/$priceTargetId")
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
  changePercentageIn24h: Double = 10.00,
  changePercentageIn7d: Double = -5.00,
  changePercentageIn30d: Double = 0.00,
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

fun getPlatformEntity(
  id: String = "123e4567-e89b-12d3-a456-426614174111",
  name: String = "BINANCE"
): Platform {
  return Platform(
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
  cryptoInfo: CryptoInfo = getCryptoInfo(),
  actualQuantity: BigDecimal = BigDecimal("1"),
  progress: Float = 100f,
  remainingQuantity: BigDecimal = BigDecimal.ZERO,
  goalQuantity: BigDecimal = BigDecimal("1"),
  moneyNeeded: BigDecimal = BigDecimal.ZERO
): GoalResponse {
  return GoalResponse(
    id = id,
    cryptoInfo = cryptoInfo,
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
    changePercentageIn24h = 10.00,
    changePercentageIn7d = -5.00,
    changePercentageIn30d = 0.00
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

fun balances() = Balances(FiatBalance("100", "70"), "0.1")

fun getCryptoInfo(
  cryptoName: String = "Bitcoin",
  coingeckoCryptoId: String = "bitcoin",
  symbol: String = "btc",
  image: String = getImage().large,
  price: Price? = null,
  priceChange: PriceChange? = null
) = CryptoInfo(cryptoName, coingeckoCryptoId, symbol, image, price, priceChange)

fun userCryptos(): List<UserCrypto> {
  return listOf(
    UserCrypto(
      id = "676fb38a-556e-11ee-b56e-325096b39f47",
      coingeckoCryptoId = "bitcoin",
      quantity = BigDecimal("0.15"),
      platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
    ),
    UserCrypto(
      id = "676fb600-556e-11ee-83b6-325096b39f47",
      coingeckoCryptoId = "tether",
      quantity = BigDecimal("200"),
      platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
    ),
    UserCrypto(
      id = "676fb696-556e-11ee-aa1c-325096b39f47",
      coingeckoCryptoId = "ethereum",
      quantity = BigDecimal("0.26"),
      platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
    ),
    UserCrypto(
      id = "676fba74-556e-11ee-9bff-325096b39f47",
      coingeckoCryptoId = "ethereum",
      quantity = BigDecimal("1.112"),
      platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
    ),
    UserCrypto(
      id = "676fb70e-556e-11ee-8c2c-325096b39f47",
      coingeckoCryptoId = "litecoin",
      quantity = BigDecimal("3.125"),
      platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
    ),
    UserCrypto(
      id = "676fb768-556e-11ee-8b42-325096b39f47",
      coingeckoCryptoId = "binancecoin",
      quantity = BigDecimal("1"),
      platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
    ),
    UserCrypto(
      id = "676fb7c2-556e-11ee-9800-325096b39f47",
      coingeckoCryptoId = "ripple",
      quantity = BigDecimal("50"),
      platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
    ),
    UserCrypto(
      id = "676fb83a-556e-11ee-9731-325096b39f47",
      coingeckoCryptoId = "cardano",
      quantity = BigDecimal("150"),
      platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
    ),
    UserCrypto(
      id = "676fb89e-556e-11ee-b0b8-325096b39f47",
      coingeckoCryptoId = "polkadot",
      quantity = BigDecimal("40"),
      platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
    ),
    UserCrypto(
      id = "676fb8e4-556e-11ee-883e-325096b39f47",
      coingeckoCryptoId = "solana",
      quantity = BigDecimal("10"),
      platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
    ),
    UserCrypto(
      id = "676fb92a-556e-11ee-9de1-325096b39f47",
      coingeckoCryptoId = "matic-network",
      quantity = BigDecimal("100"),
      platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
    ),
    UserCrypto(
      id = "676fb966-556e-11ee-81d6-325096b39f47",
      coingeckoCryptoId = "chainlink",
      quantity = BigDecimal("35"),
      platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
    ),
    UserCrypto(
      id = "676fb9ac-556e-11ee-b4fa-325096b39f47",
      coingeckoCryptoId = "dogecoin",
      quantity = BigDecimal("500"),
      platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
    ),
    UserCrypto(
      id = "676fb9f2-556e-11ee-a929-325096b39f47",
      coingeckoCryptoId = "avalanche-2",
      quantity = BigDecimal("25"),
      platformId = "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
    ),
    UserCrypto(
      id = "676fba2e-556e-11ee-a181-325096b39f47",
      coingeckoCryptoId = "uniswap",
      quantity = BigDecimal("30"),
      platformId = "a76b400e-8ffc-42d6-bf47-db866eb20153"
    )
  )
}

fun cryptos(): List<Crypto> {
  val localDateTime = LocalDateTime.now()

  return listOf(
    getCryptoEntity(),
    Crypto(
      id = "tether",
      name = "Tether",
      ticker = "usdt",
      image = "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663",
      circulatingSupply = BigDecimal("83016246102"),
      lastKnownPrice = BigDecimal("0.999618"),
      lastKnownPriceInBTC = BigDecimal("0.0000388"),
      lastKnownPriceInEUR = BigDecimal("0.933095"),
      marketCapRank = 3,
      marketCap = BigDecimal("95085861049"),
      changePercentageIn24h = 0.00,
      changePercentageIn7d = 0.00,
      changePercentageIn30d = 0.00,
      maxSupply = BigDecimal.ZERO,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "ethereum",
      name = "Ethereum",
      ticker = "eth",
      image = "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
      circulatingSupply = BigDecimal("120220572"),
      lastKnownPrice = BigDecimal("1617.44"),
      lastKnownPriceInBTC = BigDecimal("0.06280356"),
      lastKnownPriceInEUR = BigDecimal("1509.37"),
      maxSupply = BigDecimal.ZERO,
      marketCapRank = 2,
      marketCap = BigDecimal("298219864117"),
      changePercentageIn24h = 10.00,
      changePercentageIn7d = -5.00,
      changePercentageIn30d = 2.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "litecoin",
      name = "Litecoin",
      ticker = "ltc",
      image = "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580",
      circulatingSupply = BigDecimal("73638701"),
      lastKnownPrice = BigDecimal("60.59"),
      lastKnownPriceInBTC = BigDecimal("0.00235292"),
      lastKnownPriceInEUR = BigDecimal("56.56"),
      maxSupply = BigDecimal("84000000"),
      marketCapRank = 19,
      marketCap = BigDecimal("5259205267"),
      changePercentageIn24h = 6.00,
      changePercentageIn7d = -2.00,
      changePercentageIn30d = 12.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "binancecoin",
      name = "BNB",
      ticker = "bnb",
      image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850",
      circulatingSupply = BigDecimal("153856150"),
      lastKnownPrice = BigDecimal("211.79"),
      lastKnownPriceInBTC = BigDecimal("0.00811016"),
      lastKnownPriceInEUR = BigDecimal("197.80"),
      maxSupply = BigDecimal("200000000"),
      marketCapRank = 4,
      marketCap = BigDecimal("48318686968"),
      changePercentageIn24h = 6.00,
      changePercentageIn7d = -2.00,
      changePercentageIn30d = 12.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "ripple",
      name = "XRP",
      ticker = "xrp",
      image = "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1605778731",
      circulatingSupply = BigDecimal("53083046512"),
      lastKnownPrice = BigDecimal("0.478363"),
      lastKnownPriceInBTC = BigDecimal("0.00001833"),
      lastKnownPriceInEUR = BigDecimal("0.446699"),
      maxSupply = BigDecimal("100000000000"),
      marketCapRank = 6,
      marketCap = BigDecimal("29348197308"),
      changePercentageIn24h = 2.00,
      changePercentageIn7d = 3.00,
      changePercentageIn30d = -5.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "cardano",
      name = "Cardano",
      ticker = "ada",
      image = "https://assets.coingecko.com/coins/images/975/large/cardano.png?1547034860",
      circulatingSupply = BigDecimal("35045020830"),
      lastKnownPrice = BigDecimal("0.248915"),
      lastKnownPriceInBTC = BigDecimal("0.0000095"),
      lastKnownPriceInEUR = BigDecimal("0.231985"),
      maxSupply = BigDecimal("45000000000"),
      marketCapRank = 9,
      marketCap = BigDecimal("29348197308"),
      changePercentageIn24h = 7.00,
      changePercentageIn7d = 1.00,
      changePercentageIn30d = -2.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "polkadot",
      name = "Polkadot",
      ticker = "dot",
      image = "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644",
      circulatingSupply = BigDecimal("1274258350"),
      lastKnownPrice = BigDecimal("4.01"),
      lastKnownPriceInBTC = BigDecimal("0.00015302"),
      lastKnownPriceInEUR = BigDecimal("3.73"),
      maxSupply = BigDecimal.ZERO,
      marketCapRank = 13,
      marketCap = BigDecimal("8993575127"),
      changePercentageIn24h = 4.00,
      changePercentageIn7d = -1.00,
      changePercentageIn30d = 2.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "solana",
      name = "Solana",
      ticker = "sol",
      image = "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422",
      circulatingSupply = BigDecimal("410905807"),
      lastKnownPrice = BigDecimal("18.04"),
      lastKnownPriceInBTC = BigDecimal("0.00068809"),
      lastKnownPriceInEUR = BigDecimal("16.82"),
      maxSupply = BigDecimal.ZERO,
      marketCapRank = 5,
      marketCap = BigDecimal("40090766907"),
      changePercentageIn24h = 4.00,
      changePercentageIn7d = 1.00,
      changePercentageIn30d = -2.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "matic-network",
      name = "Polygon",
      ticker = "matic",
      image = "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png?1624446912",
      circulatingSupply = BigDecimal("9319469069"),
      lastKnownPrice = BigDecimal("0.509995"),
      lastKnownPriceInBTC = BigDecimal("0.00001947"),
      lastKnownPriceInEUR = BigDecimal("0.475407"),
      maxSupply = BigDecimal("10000000000"),
      marketCapRank = 16,
      marketCap = BigDecimal("7001911961"),
      changePercentageIn24h = 14.00,
      changePercentageIn7d = -10.00,
      changePercentageIn30d = 2.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "chainlink",
      name = "Chainlink",
      ticker = "link",
      image = "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1547034700",
      circulatingSupply = BigDecimal("538099971"),
      lastKnownPrice = BigDecimal("5.99"),
      lastKnownPriceInBTC = BigDecimal("0.00022866"),
      lastKnownPriceInEUR = BigDecimal("5.58"),
      maxSupply = BigDecimal("1000000000"),
      marketCapRank = 16,
      marketCap = BigDecimal("9021587267"),
      changePercentageIn24h = 4.00,
      changePercentageIn7d = -1.00,
      changePercentageIn30d = 8.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "dogecoin",
      name = "Dogecoin",
      ticker = "doge",
      image = "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1547792256",
      circulatingSupply = BigDecimal("140978466383"),
      lastKnownPrice = BigDecimal("0.061481"),
      lastKnownPriceInBTC = BigDecimal("0.00000235"),
      lastKnownPriceInEUR = BigDecimal("0.057319"),
      maxSupply = BigDecimal.ZERO,
      marketCapRank = 11,
      marketCap = BigDecimal("11195832359"),
      changePercentageIn24h = -4.00,
      changePercentageIn7d = -1.00,
      changePercentageIn30d = -8.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "avalanche-2",
      name = "Avalanche",
      ticker = "avax",
      image = "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574",
      circulatingSupply = BigDecimal("353804673"),
      lastKnownPrice = BigDecimal("9.3"),
      lastKnownPriceInBTC = BigDecimal("0.00035516"),
      lastKnownPriceInEUR = BigDecimal("8.67"),
      maxSupply = BigDecimal("720000000"),
      marketCapRank = 10,
      marketCap = BigDecimal("11953262327"),
      changePercentageIn24h = 4.00,
      changePercentageIn7d = 1.00,
      changePercentageIn30d = 8.00,
      lastUpdatedAt = localDateTime
    ),
    Crypto(
      id = "uniswap",
      name = "Uniswap",
      ticker = "uni",
      image = "https://assets.coingecko.com/coins/images/12504/large/uni.jpg?1687143398",
      circulatingSupply = BigDecimal("753766667"),
      lastKnownPrice = BigDecimal("4.25"),
      lastKnownPriceInBTC = BigDecimal("0.00016197"),
      lastKnownPriceInEUR = BigDecimal("3.96"),
      maxSupply = BigDecimal("1000000000"),
      marketCapRank = 22,
      marketCap = BigDecimal("4772322900"),
      changePercentageIn24h = 2.00,
      changePercentageIn7d = -1.00,
      changePercentageIn30d = 3.00,
      lastUpdatedAt = localDateTime
    )
  )
}
