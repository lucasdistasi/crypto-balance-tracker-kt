package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.CRYPTOS_BALANCES_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.CRYPTO_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.DATES_BALANCES_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORMS_BALANCES_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.PLATFORM_INSIGHTS_CACHE
import com.distasilucas.cryptobalancetracker.constants.TOTAL_BALANCES_CACHE
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.entity.Platform
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.SortBy
import com.distasilucas.cryptobalancetracker.model.SortParams
import com.distasilucas.cryptobalancetracker.model.SortType
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesChartResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.CryptoInsights
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate
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

  @Cacheable(cacheNames = [TOTAL_BALANCES_CACHE])
  fun retrieveTotalBalances(): BalancesResponse {
    logger.info { "Retrieving total balances" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      return BalancesResponse.EMPTY
    }

    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)

    return getTotalBalances(cryptos, userCryptoQuantity)
  }

  @Cacheable(cacheNames = [DATES_BALANCES_CACHE], key = "#dateRange")
  fun retrieveDatesBalances(dateRange: DateRange): Optional<DatesBalanceResponse> {
    logger.info { "Retrieving balances for date range: $dateRange" }
    val now = LocalDate.now(clock)

    val dateBalances = when (dateRange) {
      DateRange.LAST_DAY -> retrieveDatesBalances(now.minusDays(1), now)
      DateRange.THREE_DAYS -> retrieveDatesBalances(now.minusDays(2), now)
      DateRange.ONE_WEEK -> retrieveDatesBalances(now.minusDays(6), now)
      DateRange.ONE_MONTH -> retrieveDatesBalances(2, 4, now.minusMonths(1), now)
      DateRange.THREE_MONTHS -> retrieveDatesBalances(6, 5, now.minusMonths(3), now)
      DateRange.SIX_MONTHS -> retrieveDatesBalances(10, 6, now.minusMonths(6), now)
      DateRange.ONE_YEAR -> retrieveYearDatesBalances(now)
    }

    val datesBalances = dateBalances.map {
      val localDate = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val formattedDate = localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
      DateBalances(formattedDate, BalancesResponse(it.usdBalance, it.eurBalance, it.btcBalance))
    }.toList()

    logger.info { "Balances found: ${datesBalances.size}" }

    if (datesBalances.isEmpty()) return Optional.empty()

    val changesPair = changesPair(datesBalances)

    return Optional.of(DatesBalanceResponse(datesBalances, changesPair.first, changesPair.second))
  }

  @Cacheable(cacheNames = [PLATFORM_INSIGHTS_CACHE], key = "#platformId")
  fun retrievePlatformInsights(platformId: String): Optional<PlatformInsightsResponse> {
    logger.info { "Retrieving insights for platform with id $platformId" }

    val userCryptosInPlatform = userCryptoService.findAllByPlatformId(platformId)

    if (userCryptosInPlatform.isEmpty()) return Optional.empty()

    val platform = platformService.retrievePlatformById(platformId)
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
        cryptoInfo = crypto.toCryptoInfo(),
        quantity = quantity.toPlainString(),
        percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoTotalBalances.totalUSDBalance),
        balances = cryptoTotalBalances
      )
    }.sortedByDescending { it.percentage }

    return Optional.of(PlatformInsightsResponse(platform.name, totalBalances, cryptosInsights))
  }

  @Cacheable(cacheNames = [CRYPTO_INSIGHTS_CACHE], key = "#coingeckoCryptoId")
  fun retrieveCryptoInsights(coingeckoCryptoId: String): Optional<CryptoInsightResponse> {
    logger.info { "Retrieving insights for crypto with coingeckoCryptoId $coingeckoCryptoId" }

    val userCryptos = userCryptoService.findAllByCoingeckoCryptoId(coingeckoCryptoId)

    if (userCryptos.isEmpty()) return Optional.empty()

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

    return Optional.of(CryptoInsightResponse(crypto.name, totalBalances, platformInsights))
  }

  @Cacheable(cacheNames = [PLATFORMS_BALANCES_INSIGHTS_CACHE])
  fun retrievePlatformsBalancesInsights(): List<BalancesChartResponse> {
    logger.info { "Retrieving all platforms balances insights" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) return emptyList()

    val platformsIds = userCryptos.map { it.platformId }.toSet()
    val platforms = platformService.findAllByIds(platformsIds)
    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val platformsUserCryptos = getPlatformsUserCryptos(userCryptos, platforms)
    val cryptosIds = platformsUserCryptos.values.flatMap { it.map { it.coingeckoCryptoId } }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

    return platformsUserCryptos.map { (platformName, userCryptos) ->
      var totalUSDBalance = BigDecimal.ZERO

      userCryptos.forEach { crypto ->
        val balance = getCryptoTotalBalances(cryptos.first { it.id == crypto.coingeckoCryptoId }, crypto.quantity)
        totalUSDBalance = totalUSDBalance.plus(BigDecimal(balance.totalUSDBalance))
      }
      val percentage = calculatePercentage(totalBalances.totalUSDBalance, totalUSDBalance.toPlainString())

      BalancesChartResponse(platformName, totalUSDBalance.toPlainString(), percentage)
    }.sortedByDescending { it.percentage }
  }

  @Cacheable(cacheNames = [CRYPTOS_BALANCES_INSIGHTS_CACHE])
  fun retrieveCryptosBalancesInsights(): List<BalancesChartResponse> {
    logger.info { "Retrieving all cryptos balances insights" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) return emptyList()

    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)

    val cryptosInsights = userCryptoQuantity.map { (coingeckoCryptoId, quantity) ->
      val crypto = cryptos.first { it.id == coingeckoCryptoId }
      val cryptoBalances = getCryptoTotalBalances(crypto, quantity)
      val percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoBalances.totalUSDBalance)

      BalancesChartResponse(crypto.name, cryptoBalances.totalUSDBalance, percentage)
    }.sortedByDescending { it.percentage }

    return if (cryptosInsights.size > maxSingleItemsCount) {
      getCryptoInsightsWithOthers(totalBalances, cryptosInsights)
    } else {
      cryptosInsights
    }
  }

  fun retrieveUserCryptosInsights(
    page: Int,
    sortParams: SortParams = SortParams(SortBy.PERCENTAGE, SortType.DESC)
  ): PageUserCryptosInsightsResponse {
    logger.info { "Retrieving user cryptos insights for page $page" }

    // If one of the user cryptos happens to be at the end, and another of the same (i.e: bitcoin), at the start
    // using findAllByPage() will display the same crypto twice (in this example), and the idea of this insight
    // it's to display total balances and percentage for each individual crypto.
    // So I need to calculate everything from all the user cryptos.
    // Maybe create a query that returns the coingeckoCryptoId summing all balances for that crypto and
    // returning an array of the platforms for that crypto and then paginate the results
    // would be a better approach so I don't need to retrieve all.
    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      return PageUserCryptosInsightsResponse.EMPTY
    }

    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val cryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(cryptosIds)
    val platformsIds = userCryptos.map { it.platformId }.toSet()
    val platforms = platformService.findAllByIds(platformsIds)
    val totalBalances = getTotalBalances(cryptos, userCryptoQuantity)
    val userCryptosQuantityPlatforms = getUserCryptosQuantityPlatforms(userCryptos, platforms)

    val userCryptoInsights = userCryptosQuantityPlatforms.map {
      val (cryptoTotalQuantity, _) = it.value
      val crypto = cryptos.first { crypto -> crypto.id == it.key }
      val cryptoTotalBalances = getCryptoTotalBalances(crypto, cryptoTotalQuantity)
      val price = Price(crypto.lastKnownPrice, crypto.lastKnownPriceInEUR, crypto.lastKnownPriceInBTC)
      val priceChange = PriceChange(crypto.changePercentageIn24h, crypto.changePercentageIn7d, crypto.changePercentageIn30d)
      val cryptoInfo = crypto.toCryptoInfo(price, priceChange)

      UserCryptoInsights(
        cryptoInfo = cryptoInfo,
        quantity = cryptoTotalQuantity.toPlainString(),
        percentage = calculatePercentage(totalBalances.totalUSDBalance, cryptoTotalBalances.totalUSDBalance),
        balances = cryptoTotalBalances
      )
    }.sortedWith(sortParams.cryptosInsightsResponseComparator())

    val startIndex = page * INT_ELEMENTS_PER_PAGE

    if (startIndex > userCryptoInsights.size) {
      return PageUserCryptosInsightsResponse.EMPTY
    }

    val totalPages = ceil(userCryptoInsights.size.toDouble() / ELEMENTS_PER_PAGE).toInt()
    val endIndex = if (isLastPage(page, totalPages)) userCryptoInsights.size else startIndex + INT_ELEMENTS_PER_PAGE
    val cryptosInsights = userCryptoInsights.subList(startIndex, endIndex)

    return PageUserCryptosInsightsResponse(page, totalPages, totalBalances, cryptosInsights)
  }

  private fun retrieveDatesBalances(from: LocalDate, to: LocalDate): List<DateBalance> {
    logger.info { "Retrieving date balances from $from to $to" }

    return dateBalanceRepository.findDateBalancesByInclusiveDateBetween(from.toString(), to.toString())
  }

  private fun retrieveDatesBalances(daysSubtraction: Long, minRequired: Int,
                                    from: LocalDate, to: LocalDate): List<DateBalance> {
    val dates = mutableListOf<String>()
    var toDate = to

    while (from.isBefore(toDate)) {
      dates.add(toDate.toString())
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

  private fun retrieveYearDatesBalances(now: LocalDate): List<DateBalance> {
    val dates = mutableListOf<String>()
    dates.add(now.toString())

    LongStream.range(1, 12)
      .forEach { dates.add(now.minusMonths(it).toString()) }

    logger.info { "Searching balances for dates $dates" }

    val datesBalances = dateBalanceRepository.findAllByDateIn(dates)

    return if (datesBalances.size > 3) {
      datesBalances
    } else {
      retrieveLastTwelveDaysBalances()
    }
  }

  private fun changesPair(dateBalances: List<DateBalances>): Pair<BalanceChanges, DifferencesChanges> {
    val usdChange = getChange(BalanceType.USD_BALANCE, dateBalances)
    val eurChange = getChange(BalanceType.EUR_BALANCE, dateBalances)
    val btcChange = getChange(BalanceType.BTC_BALANCE, dateBalances)

    return Pair(
      BalanceChanges(usdChange.first, eurChange.first, btcChange.first),
      DifferencesChanges(usdChange.second, eurChange.second, btcChange.second)
    )
  }

  private fun getChange(balanceType: BalanceType, dateBalances: List<DateBalances>): Pair<Float, String> {
    val newestValues = dateBalances.last().balances
    val oldestValues = dateBalances.first().balances
    val divisionScale = if (BalanceType.BTC_BALANCE == balanceType) 10 else 4

    val values = when (balanceType) {
      BalanceType.USD_BALANCE -> Pair(BigDecimal(oldestValues.totalUSDBalance), BigDecimal(newestValues.totalUSDBalance))
      BalanceType.EUR_BALANCE -> Pair(BigDecimal(oldestValues.totalEURBalance), BigDecimal(newestValues.totalEURBalance))
      BalanceType.BTC_BALANCE -> Pair(BigDecimal(oldestValues.totalBTCBalance), BigDecimal(newestValues.totalBTCBalance))
    }

    val newestValue = values.second
    val oldestValue = values.first

    val change = newestValue
      .subtract(oldestValue)
      .divide(oldestValue, divisionScale, RoundingMode.HALF_UP)
      .multiply(BigDecimal("100"))
      .setScale(2, RoundingMode.HALF_UP)
      .toFloat()
    val difference = newestValue.subtract(oldestValue).toPlainString()

    return Pair(change, difference)
  }

  private fun retrieveLastTwelveDaysBalances(): List<DateBalance> {
    val to = LocalDate.now(clock)
    val from = to.minusDays(12)
    logger.info { "Not enough balances. Retrieving balances for the last twelve days from $from to $to" }

    return dateBalanceRepository.findDateBalancesByInclusiveDateBetween(from.toString(), to.toString())
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
      totalBTCBalance = totalBTCBalance.setScale(8, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString(),
      totalEURBalance = totalEURBalance.toPlainString()
    )
  }

  private fun getCryptoTotalBalances(crypto: Crypto, quantity: BigDecimal): BalancesResponse {
    return BalancesResponse(
      totalUSDBalance = crypto.lastKnownPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString(),
      totalBTCBalance = crypto.lastKnownPriceInBTC.multiply(quantity).setScale(8, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString(),
      totalEURBalance = crypto.lastKnownPriceInEUR.multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString()
    )
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
    balances: List<BalancesChartResponse>
  ): List<BalancesChartResponse> {
    var cryptosInsightsWithOthers: List<BalancesChartResponse> = LinkedList()
    val topCryptos = balances.subList(0, maxSingleItemsCount)
    val others = balances.subList(maxSingleItemsCount, balances.size)

    var totalUSDBalance = BigDecimal.ZERO
    others.forEach {
      totalUSDBalance = totalUSDBalance.plus(BigDecimal(it.balance))
    }
    val othersTotalPercentage = calculatePercentage(totalBalances.totalUSDBalance, totalUSDBalance.toPlainString())
    val othersCryptoInsights = BalancesChartResponse("Others", totalUSDBalance.toPlainString(), othersTotalPercentage)

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

enum class BalanceType {
  USD_BALANCE,
  EUR_BALANCE,
  BTC_BALANCE
}
