package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.SortBy
import com.distasilucas.cryptobalancetracker.model.SortParams
import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.CirculatingSupply
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.CurrentPrice
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.MarketData
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.LongStream
import kotlin.math.ceil

private const val ELEMENTS_PER_PAGE = 10.0
private const val INT_ELEMENTS_PER_PAGE = ELEMENTS_PER_PAGE.toInt()

@Service
class InsightsService(
  @Value("\${crypto.insights.max-single-items-count}")
  private val maxSingleItemsCount: Int,
  private val platformService: PlatformService,
  private val userCryptoService: UserCryptoService,
  private val cryptoService: CryptoService,
  private val dateBalanceRepository: DateBalanceRepository,
  private val clock: Clock
) {

  private val logger = KotlinLogging.logger { }

  fun retrieveTotalBalances(): Optional<BalancesResponse> {
    logger.info { "Retrieving total balances" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      return Optional.empty()
    }

    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

    return Optional.of(totalBalances)
  }

  fun retrieveDatesBalances(dateRange: DateRange): Optional<DatesBalanceResponse> {
    logger.info { "Retrieving balances for date range: $dateRange" }
    val now = LocalDateTime.now(clock).toLocalDate().atTime(LocalTime.of(23, 59, 59, 0))

    val dateBalances = when (dateRange) {
      DateRange.LAST_DAY -> retrieveDatesBalances(now.minusDays(2), now)
      DateRange.THREE_DAYS -> retrieveDatesBalances(now.minusDays(3), now)
      DateRange.ONE_WEEK -> retrieveDatesBalances(now.minusWeeks(1), now)
      DateRange.ONE_MONTH -> retrieveDatesBalances(2, 4, now.minusMonths(1), now)
      DateRange.THREE_MONTHS -> retrieveDatesBalances(6, 5, now.minusMonths(3), now)
      DateRange.SIX_MONTHS -> retrieveDatesBalances(10, 6, now.minusMonths(6), now)
      DateRange.ONE_YEAR -> retrieveYearDatesBalances(now)
    }

    val datesBalances = dateBalances.map {
      val formattedDate = it.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
      DatesBalances(formattedDate, it.balance)
    }.toList()
    logger.info { "Balances found: ${datesBalances.size}" }

    if (datesBalances.isEmpty()) return Optional.empty()

    val newestValue = BigDecimal(datesBalances.last().balance)
    val oldestValue = BigDecimal(datesBalances.first().balance)
    val change = newestValue
      .subtract(oldestValue)
      .divide(oldestValue, 4, RoundingMode.HALF_UP)
      .multiply(BigDecimal("100"))
      .setScale(2, RoundingMode.HALF_UP)
      .toFloat()
    val priceDifference = newestValue.subtract(oldestValue).toPlainString()

    return Optional.of(DatesBalanceResponse(datesBalances, change, priceDifference))
  }

  fun retrievePlatformInsights(platformId: String): Optional<PlatformInsightsResponse> {
    logger.info { "Retrieving insights for platform with id $platformId" }

    val userCryptosInPlatform = userCryptoService.findAllByPlatformId(platformId)

    if (userCryptosInPlatform.isEmpty()) {
      return Optional.empty()
    }

    val platformResponse = platformService.retrievePlatformById(platformId)
    val cryptosIds = userCryptosInPlatform.map { it.coingeckoCryptoId }
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val userCryptosQuantity = getUserCryptoQuantity(userCryptosInPlatform)
    val totalBalances = getTotalBalances(cryptos, userCryptosQuantity)

    val cryptosInsights = userCryptosInPlatform.map { userCrypto ->
      val crypto = cryptos.first { userCrypto.coingeckoCryptoId == it.id }
      val quantity = userCryptosQuantity[userCrypto.coingeckoCryptoId]
      val cryptoTotalBalances = getCryptoTotalBalances(crypto, quantity!!)

      CryptoInsights(
        id = userCrypto.id,
        cryptoName = crypto.name,
        cryptoId = crypto.id,
        quantity = quantity.toPlainString(),
        balances = cryptoTotalBalances,
        percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoTotalBalances.totalUSDBalance)
      )
    }.sortedByDescending { it.percentage }

    val platformInsightsResponse = PlatformInsightsResponse(
      platformName = platformResponse.name,
      balances = totalBalances,
      cryptos = cryptosInsights
    )

    return Optional.of(platformInsightsResponse)
  }

  fun retrieveCryptoInsights(coingeckoCryptoId: String): Optional<CryptoInsightResponse> {
    logger.info { "Retrieving insights for crypto with coingeckoCryptoId $coingeckoCryptoId" }

    val userCryptos = userCryptoService.findAllByCoingeckoCryptoId(coingeckoCryptoId)

    if (userCryptos.isEmpty()) {
      return Optional.empty()
    }

    val platformsIds = userCryptos.map { it.platformId }
    val platforms = platformService.findAllByIds(platformsIds)
    val crypto = cryptoService.retrieveCryptoInfoById(coingeckoCryptoId)

    val platformUserCryptoQuantity = userCryptos.associateBy({ it.platformId }, { it.quantity })
    val totalCryptoQuantity = userCryptos.map { it.quantity }.fold(BigDecimal.ZERO, BigDecimal::add)
    val totalBalances = getTotalBalances(listOf(crypto), mapOf(coingeckoCryptoId to totalCryptoQuantity))

    val platformInsights = platforms.map { platform ->
      val quantity = platformUserCryptoQuantity[platform.id]
      val cryptoTotalBalances = getCryptoTotalBalances(crypto, quantity!!)

      val platformInsight = PlatformInsight(
        quantity = quantity.toPlainString(),
        balances = cryptoTotalBalances,
        percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoTotalBalances.totalUSDBalance),
        platformName = platform.name
      )

      platformInsight
    }.sortedByDescending { it.percentage }

    return Optional.of(
      CryptoInsightResponse(
        cryptoName = crypto.name,
        balances = totalBalances,
        platforms = platformInsights
      )
    )
  }

  fun retrievePlatformsBalancesInsights(): Optional<PlatformsBalancesInsightsResponse> {
    logger.info { "Retrieving all platforms balances insights" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      return Optional.empty()
    }

    val platformsIds = userCryptos.map { it.platformId }.toSet()
    val platforms = platformService.findAllByIds(platformsIds)
    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val platformsUserCryptos = getPlatformsUserCryptos(userCryptos, platforms)
    val cryptosIds = platformsUserCryptos.values.flatMap { it.map { it.coingeckoCryptoId } }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

    val platformsInsights = platformsUserCryptos.map { (platformName, userCryptos) ->
      var totalUSDBalance = BigDecimal.ZERO
      var totalBTCBalance = BigDecimal.ZERO
      var totalEURBalance = BigDecimal.ZERO

      userCryptos.forEach { crypto ->
        val balance = getCryptoTotalBalances(cryptos.first { it.id == crypto.coingeckoCryptoId }, crypto.quantity)
        totalUSDBalance = totalUSDBalance.plus(BigDecimal(balance.totalUSDBalance))
        totalBTCBalance = totalBTCBalance.plus(BigDecimal(balance.totalBTCBalance))
        totalEURBalance = totalEURBalance.plus(BigDecimal(balance.totalEURBalance))
      }

      val platformInsight = PlatformsInsights(
        platformName = platformName,
        balances = BalancesResponse(
          totalUSDBalance = totalUSDBalance.toPlainString(),
          totalBTCBalance = totalBTCBalance.toPlainString(),
          totalEURBalance = totalEURBalance.toPlainString()
        ),
        percentage = calculatePercentage(totalBalances.totalUSDBalance, totalUSDBalance.toPlainString())
      )

      platformInsight
    }.sortedByDescending { it.percentage }

    return Optional.of(
      PlatformsBalancesInsightsResponse(
        balances = totalBalances,
        platforms = platformsInsights
      )
    )
  }

  fun retrieveCryptosBalancesInsights(): Optional<CryptosBalancesInsightsResponse> {
    logger.info { "Retrieving all cryptos balances insights" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      return Optional.empty()
    }

    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

    val cryptosInsights = userCryptoQuantity.map { (coingeckoCryptoId, quantity) ->
      val crypto = cryptos.first { it.id == coingeckoCryptoId }
      val cryptoBalances = getCryptoTotalBalances(crypto, quantity)

      CryptoInsights(
        cryptoName = crypto.name,
        cryptoId = coingeckoCryptoId,
        quantity = quantity.toPlainString(),
        balances = cryptoBalances,
        percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoBalances.totalUSDBalance)
      )
    }.sortedByDescending { it.percentage }

    return Optional.of(
      CryptosBalancesInsightsResponse(
        balances = totalBalances,
        cryptos = if (cryptosInsights.size > maxSingleItemsCount)
          getCryptoInsightsWithOthers(totalBalances, cryptosInsights) else cryptosInsights
      )
    )
  }

  fun retrieveUserCryptosInsights(
    page: Int,
    sortParams: SortParams = SortParams(SortBy.PERCENTAGE, SortType.DESC)
  ): Optional<PageUserCryptosInsightsResponse> {
    logger.info { "Retrieving user cryptos insights for page $page" }

    // Not the best because I'm paginating, but I need total balances to calculate individual percentages
    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      return Optional.empty()
    }

    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val platformsIds = userCryptos.map { it.platformId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val platforms = platformService.findAllByIds(platformsIds)

    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

    val userCryptosInsights = userCryptos.map {
      val crypto = cryptos.first { crypto -> crypto.id == it.coingeckoCryptoId }
      val platform = platforms.first { platform -> platform.id == it.platformId }
      val balances = getCryptoTotalBalances(crypto, it.quantity)
      val circulatingSupply = getCirculatingSupply(crypto.maxSupply, crypto.circulatingSupply)

      UserCryptosInsights(
        cryptoInfo = CryptoInfo(
          id = it.id,
          cryptoName = crypto.name,
          coingeckoCryptoId = crypto.id,
          symbol = crypto.ticker,
          image = crypto.image
        ),
        quantity = it.quantity.toPlainString(),
        percentage = calculatePercentage(totalBalances.totalUSDBalance, balances.totalUSDBalance),
        balances = balances,
        marketCapRank = crypto.marketCapRank,
        marketData = MarketData(
          circulatingSupply,
          maxSupply = crypto.maxSupply.toPlainString(),
          currentPrice = CurrentPrice(
            usd = crypto.lastKnownPrice.toPlainString(),
            eur = crypto.lastKnownPriceInEUR.toPlainString(),
            btc = crypto.lastKnownPriceInBTC.toPlainString()
          ),
          marketCap = crypto.marketCap.toPlainString(),
          priceChange = PriceChange(
            crypto.changePercentageIn24h,
            crypto.changePercentageIn7d,
            crypto.changePercentageIn30d
          )
        ),
        platforms = listOf(platform.name)
      )
    }.sortedWith(sortParams.cryptosInsightsResponseComparator())

    val startIndex = page * INT_ELEMENTS_PER_PAGE

    if (startIndex > userCryptosInsights.size) {
      return Optional.empty()
    }

    val totalPages = ceil(userCryptos.size.toDouble() / ELEMENTS_PER_PAGE).toInt()
    val endIndex = if (isLastPage(page, totalPages)) userCryptosInsights.size else startIndex + INT_ELEMENTS_PER_PAGE
    val cryptosInsights = userCryptosInsights.subList(startIndex, endIndex)

    return Optional.of(
      PageUserCryptosInsightsResponse(
        page = page,
        totalPages = totalPages,
        balances = totalBalances,
        cryptos = cryptosInsights
      )
    )
  }

  fun retrieveUserCryptosPlatformsInsights(
    page: Int,
    sortParams: SortParams = SortParams(SortBy.PERCENTAGE, SortType.DESC)
  ): Optional<PageUserCryptosInsightsResponse> {
    logger.info { "Retrieving user cryptos in platforms insights for page $page" }

    // If one of the user cryptos happens to be at the end, and another of the same (i.e: bitcoin), at the start
    // using findAllByPage() will display the same crypto twice (in this example), and the idea of this insight
    // it's to display total balances and percentage for each individual crypto.
    // So I need to calculate everything from all the user cryptos.
    // Maybe create a query that returns the coingeckoCryptoId summing all balances for that crypto and
    // returning an array of the platforms for that crypto and then paginate the results
    // would be a better approach so I don't need to retrieve all.
    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      return Optional.empty()
    }

    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val platformsIds = userCryptos.map { it.platformId }.toSet()
    val platforms = platformService.findAllByIds(platformsIds)
    val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)
    val userCryptosQuantityPlatforms = getUserCryptosQuantityPlatforms(userCryptos, platforms)

    val userCryptosInsights = userCryptosQuantityPlatforms.map {
      val (cryptoTotalQuantity, cryptoPlatforms) = it.value
      val crypto = cryptos.first { crypto -> crypto.id == it.key }
      val cryptoTotalBalances = getCryptoTotalBalances(crypto, cryptoTotalQuantity)
      val circulatingSupply = getCirculatingSupply(crypto.maxSupply, crypto.circulatingSupply)

      UserCryptosInsights(
        cryptoInfo = CryptoInfo(
          cryptoName = crypto.name,
          coingeckoCryptoId = crypto.id,
          symbol = crypto.ticker,
          image = crypto.image
        ),
        quantity = cryptoTotalQuantity.toPlainString(),
        percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoTotalBalances.totalUSDBalance),
        balances = cryptoTotalBalances,
        marketCapRank = crypto.marketCapRank,
        marketData = MarketData(
          circulatingSupply,
          maxSupply = crypto.maxSupply.toPlainString(),
          currentPrice = CurrentPrice(
            usd = crypto.lastKnownPrice.toPlainString(),
            eur = crypto.lastKnownPriceInEUR.toPlainString(),
            btc = crypto.lastKnownPriceInBTC.toPlainString()
          ),
          marketCap = crypto.marketCap.toPlainString(),
          priceChange = PriceChange(
            crypto.changePercentageIn24h,
            crypto.changePercentageIn7d,
            crypto.changePercentageIn30d
          )
        ),
        platforms = cryptoPlatforms
      )
    }.sortedWith(sortParams.cryptosInsightsResponseComparator())

    val startIndex = page * INT_ELEMENTS_PER_PAGE

    if (startIndex > userCryptosInsights.size) {
      return Optional.empty()
    }

    val totalPages = ceil(userCryptosInsights.size.toDouble() / ELEMENTS_PER_PAGE).toInt()
    val endIndex = if (isLastPage(page, totalPages)) userCryptosInsights.size else startIndex + INT_ELEMENTS_PER_PAGE
    val cryptosInsights = userCryptosInsights.subList(startIndex, endIndex)

    return Optional.of(
      PageUserCryptosInsightsResponse(
        page = page,
        totalPages = totalPages,
        balances = totalBalances,
        cryptos = cryptosInsights
      )
    )
  }

  private fun retrieveDatesBalances(from: LocalDateTime, to: LocalDateTime): List<DateBalance> {
    val toMax = to.toLocalDate().atTime(LocalTime.MAX)
    logger.info { "Retrieving date balances from $from to $toMax" }

    return dateBalanceRepository.findDateBalancesByDateBetween(from, toMax)
  }

  private fun retrieveDatesBalances(daysSubtraction: Long, minRequired: Int,
                                    from: LocalDateTime, to: LocalDateTime): List<DateBalance> {
    val dates = mutableListOf<LocalDateTime>()
    var toDate = to

    while (from.isBefore(toDate)) {
      dates.add(toDate)
      toDate = toDate.minusDays(daysSubtraction)
    }

    logger.info { "Searching balances for dates $dates" }

    val datesBalances = dateBalanceRepository.findAllByDateIn(dates)
    logger.info { "Found balances for dates ${datesBalances.map { it.date }}" }

    return if (datesBalances.size >= minRequired) {
      datesBalances
    } else {
      retrieveLastTwelveDaysBalances()
    }
  }

  private fun retrieveYearDatesBalances(now: LocalDateTime): List<DateBalance> {
    val dates = mutableListOf<LocalDateTime>()
    dates.add(now)

    LongStream.range(1, 12)
      .forEach { dates.add(now.minusMonths(it)) }

    logger.info { "Searching balances for dates $dates" }

    val datesBalances = dateBalanceRepository.findAllByDateIn(dates)

    return if (datesBalances.size > 3) {
      datesBalances
    } else {
      retrieveLastTwelveDaysBalances()
    }
  }

  fun retrieveLastTwelveDaysBalances(): List<DateBalance> {
    val to = LocalDateTime.now(clock).toLocalDate().atTime(LocalTime.MAX)
    val from = to.toLocalDate().minusDays(12).atTime(23, 59, 59, 0)
    logger.info { "Not enough balances. Retrieving balances for the last twelve days from $from to $to" }

    return dateBalanceRepository.findDateBalancesByDateBetween(from, to)
  }

  private fun getTotalBalances(cryptos: List<Crypto>, userCryptoQuantity: Map<String, BigDecimal>): BalancesResponse {
    var totalUSDBalance = BigDecimal.ZERO
    var totalBTCBalance = BigDecimal.ZERO
    var totalEURBalance = BigDecimal.ZERO

    userCryptoQuantity.forEach { (coingeckoCryptoId, quantity) ->
      val crypto = cryptos.first { it.id.equals(coingeckoCryptoId, true) }
      val lastKnownPrice = crypto.lastKnownPrice
      val lastKnownPriceInBTC = crypto.lastKnownPriceInBTC
      val lastKnownPriceInEUR = crypto.lastKnownPriceInEUR

      totalUSDBalance = totalUSDBalance.plus(lastKnownPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP))
      totalBTCBalance = totalBTCBalance.plus(lastKnownPriceInBTC.multiply(quantity)).stripTrailingZeros()
      totalEURBalance = totalEURBalance.plus(lastKnownPriceInEUR.multiply(quantity).setScale(2, RoundingMode.HALF_UP))
    }

    return BalancesResponse(
      totalUSDBalance = totalUSDBalance.toPlainString(),
      totalBTCBalance = totalBTCBalance.setScale(12, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString(),
      totalEURBalance = totalEURBalance.toPlainString()
    )
  }

  private fun getCryptoTotalBalances(crypto: Crypto, quantity: BigDecimal): BalancesResponse {
    return BalancesResponse(
      totalUSDBalance = crypto.lastKnownPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString(),
      totalBTCBalance = crypto.lastKnownPriceInBTC.multiply(quantity).setScale(12, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString(),
      totalEURBalance = crypto.lastKnownPriceInEUR.multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString()
    )
  }

  private fun getCirculatingSupply(maxSupply: BigDecimal, circulatingSupply: BigDecimal): CirculatingSupply {
    var circulatingSupplyPercentage = 0f

    if (BigDecimal.ZERO < maxSupply) {
      circulatingSupplyPercentage = circulatingSupply.multiply(BigDecimal("100"))
        .divide(maxSupply, 2, RoundingMode.HALF_UP)
        .toFloat()
    }

    return CirculatingSupply(circulatingSupply.toPlainString(), circulatingSupplyPercentage)
  }

  private fun calculatePercentage(totalUSDBalance: String, cryptoBalance: String) = BigDecimal(cryptoBalance)
    .multiply(BigDecimal("100"))
    .divide(BigDecimal(totalUSDBalance), 2, RoundingMode.HALF_UP)
    .toFloat()

  private fun getUserCryptoQuantity(userCryptos: List<UserCrypto>): Map<String, BigDecimal> {
    val userCryptoQuantity = HashMap<String, BigDecimal>()

    userCryptos.forEach { userCrypto ->
      if (userCryptoQuantity.containsKey(userCrypto.coingeckoCryptoId)) {
        val quantity = userCryptoQuantity[userCrypto.coingeckoCryptoId]
        userCryptoQuantity[userCrypto.coingeckoCryptoId] = quantity!!.plus(userCrypto.quantity)
      } else {
        userCryptoQuantity[userCrypto.coingeckoCryptoId] = userCrypto.quantity
      }
    }

    return userCryptoQuantity
  }

  private fun getPlatformsUserCryptos(
    userCryptos: List<UserCrypto>,
    platforms: List<Platform>
  ): Map<String, List<UserCrypto>> {
    val platformsUserCryptos = HashMap<String, List<UserCrypto>>()

    userCryptos.forEach { userCrypto ->
      val platform = platforms.first { it.id == userCrypto.platformId }

      if (platformsUserCryptos.containsKey(platform.name)) {
        val cryptos = platformsUserCryptos[platform.name]
        platformsUserCryptos[platform.name] = cryptos!!.plus(userCrypto)
      } else {
        platformsUserCryptos[platform.name] = listOf(userCrypto)
      }
    }

    return platformsUserCryptos
  }

  private fun getCryptoInsightsWithOthers(
    totalBalances: BalancesResponse,
    cryptosInsights: List<CryptoInsights>
  ): List<CryptoInsights> {
    var cryptosInsightsWithOthers: List<CryptoInsights> = ArrayList()
    val topCryptos = cryptosInsights.subList(0, maxSingleItemsCount)
    val others = cryptosInsights.subList(maxSingleItemsCount, cryptosInsights.size)

    var totalUSDBalance = BigDecimal.ZERO
    var totalBTCBalance = BigDecimal.ZERO
    var totalEURBalance = BigDecimal.ZERO
    others.forEach {
      totalUSDBalance = totalUSDBalance.plus(BigDecimal(it.balances.totalUSDBalance))
      totalBTCBalance = totalBTCBalance.plus(BigDecimal(it.balances.totalBTCBalance))
      totalEURBalance = totalEURBalance.plus(BigDecimal(it.balances.totalEURBalance))
    }
    val othersTotalPercentage = calculatePercentage(totalBalances.totalUSDBalance, totalUSDBalance.toPlainString())

    val othersCryptoInsights = CryptoInsights(
      cryptoName = "Others",
      balances = BalancesResponse(
        totalUSDBalance = totalUSDBalance.toPlainString(),
        totalBTCBalance = totalBTCBalance.toPlainString(),
        totalEURBalance = totalEURBalance.toPlainString()
      ),
      percentage = othersTotalPercentage
    )

    cryptosInsightsWithOthers = cryptosInsightsWithOthers.plus(topCryptos).plus(othersCryptoInsights)

    return cryptosInsightsWithOthers
  }

  private fun getUserCryptosQuantityPlatforms(
    userCryptos: List<UserCrypto>,
    platforms: List<Platform>
  ): Map<String, Pair<BigDecimal, List<String>>> {
    val map = HashMap<String, Pair<BigDecimal, List<String>>>()

    userCryptos.forEach { userCrypto ->
      val platformName = platforms.first { it.id == userCrypto.platformId }.name

      if (map.containsKey(userCrypto.coingeckoCryptoId)) {
        val crypto = map[userCrypto.coingeckoCryptoId]
        val actualQuantity = crypto!!.first
        val actualPlatforms = crypto.second

        val newQuantity = actualQuantity.plus(userCrypto.quantity)
        val newPlatforms = actualPlatforms.plus(platformName)

        map[userCrypto.coingeckoCryptoId] = Pair(newQuantity, newPlatforms)
      } else {
        map[userCrypto.coingeckoCryptoId] = Pair(userCrypto.quantity, listOf(platformName))
      }
    }

    return map
  }

  private fun isLastPage(page: Int, totalPages: Int) = page + 1 >= totalPages
}
