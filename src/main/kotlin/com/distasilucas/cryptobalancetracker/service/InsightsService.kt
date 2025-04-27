package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.DATES_BALANCES_CACHE
import com.distasilucas.cryptobalancetracker.constants.HOME_INSIGHTS_RESPONSE_CACHE
import com.distasilucas.cryptobalancetracker.entity.Crypto
import com.distasilucas.cryptobalancetracker.entity.DateBalance
import com.distasilucas.cryptobalancetracker.entity.UserCrypto
import com.distasilucas.cryptobalancetracker.exception.ApiException
import com.distasilucas.cryptobalancetracker.model.DateRange
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.Balances
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges
import com.distasilucas.cryptobalancetracker.model.response.insights.FiatBalance
import com.distasilucas.cryptobalancetracker.model.response.insights.HomeInsightsResponse
import com.distasilucas.cryptobalancetracker.model.response.insights.Price
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.LongStream

@Service
class InsightsService(
  private val userCryptoService: UserCryptoService,
  private val cryptoService: CryptoService,
  private val dateBalanceRepository: DateBalanceRepository,
  private val clock: Clock
) {

  private val logger = KotlinLogging.logger { }

  // TODO - Retrieve from Coingecko API
  private val stableCoinsIds = listOf("tether", "usd-coin", "ethena-usde", "dai", "first-digital-usd")

  @Cacheable(cacheNames = [HOME_INSIGHTS_RESPONSE_CACHE])
  fun retrieveHomeInsightsResponse(): HomeInsightsResponse {
    logger.info { "Retrieving home insights" }

    val userCryptos = userCryptoService.findAll()

    if (userCryptos.isEmpty()) {
      throw ApiException(HttpStatus.NOT_FOUND, "No user cryptos were found")
    }

    val userCryptoQuantity = getUserCryptoQuantity(userCryptos)
    val userCryptosIds = userCryptos.map { it.coingeckoCryptoId }.toSet()
    val cryptos = cryptoService.findAllByIds(userCryptosIds)
    val balances = getTotalBalances(cryptos, userCryptoQuantity)
    val stableCoins = cryptos.filter { stableCoinsIds.contains(it.id) }
    val userStableCoins = userCryptos.filter { stableCoinsIds.contains(it.coingeckoCryptoId) }
    val stableCoinsBalance = retrieveStableCoinsBalance(userStableCoins, stableCoins)
    val top24hGainer = with(cryptoService.findTopGainer24h(userCryptosIds)) {
      CryptoInfo(
        coingeckoCryptoId = id,
        symbol = ticker,
        image = image,
        price = Price(lastKnownPrice, lastKnownPriceInEUR),
        priceChange = PriceChange(changePercentageIn24h)
      )
    }

    return HomeInsightsResponse(balances, stableCoinsBalance, top24hGainer)
  }

  @Cacheable(cacheNames = [DATES_BALANCES_CACHE], key = "#dateRange")
  fun retrieveDatesBalances(dateRange: DateRange): DatesBalanceResponse {
    logger.info { "Retrieving balances for date range: $dateRange" }
    val now = LocalDate.now(clock)

    val dateBalances = when (dateRange) {
      DateRange.LAST_DAY -> now.retrieveDatesBalances(1)
      DateRange.THREE_DAYS -> now.retrieveDatesBalances(2)
      DateRange.ONE_WEEK -> now.retrieveDatesBalances(6)
      DateRange.ONE_MONTH -> retrieveDatesBalances(2, 4, now.minusMonths(1), now)
      DateRange.THREE_MONTHS -> retrieveDatesBalances(6, 5, now.minusMonths(3), now)
      DateRange.SIX_MONTHS -> retrieveDatesBalances(10, 6, now.minusMonths(6), now)
      DateRange.ONE_YEAR -> retrieveYearDatesBalances(now)
    }

    val datesBalances = dateBalances.map {
      val localDate = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val formattedDate = localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
      val fiatBalance = FiatBalance(it.usdBalance, it.eurBalance)

      DateBalances(formattedDate, Balances(fiatBalance, it.btcBalance))
    }.toList()

    logger.info { "Balances found: ${datesBalances.size}" }

    if (datesBalances.isEmpty()) {
      throw ApiException(HttpStatus.NO_CONTENT, "No balances found for range $dateRange")
    }

    val changesPair = changesPair(datesBalances)

    return DatesBalanceResponse(datesBalances, changesPair.first, changesPair.second)
  }

  fun getTotalBalances(cryptos: List<Crypto>, userCryptoQuantity: Map<String, BigDecimal>): Balances {
    val cryptosMap = cryptos.associateBy { it.id }
    var totalUSDBalance = BigDecimal.ZERO
    var totalBTCBalance = BigDecimal.ZERO
    var totalEURBalance = BigDecimal.ZERO

    userCryptoQuantity.forEach { (coingeckoCryptoId, quantity) ->
      val crypto = cryptosMap[coingeckoCryptoId] ?: throw ApiException(
        HttpStatus.NOT_FOUND,
        "No crypto with id $coingeckoCryptoId"
      )
      val lastKnownPrice = crypto.lastKnownPrice
      val lastKnownPriceInBTC = crypto.lastKnownPriceInBTC
      val lastKnownPriceInEUR = crypto.lastKnownPriceInEUR

      totalUSDBalance = totalUSDBalance.plus(lastKnownPrice.multiply(quantity))
      totalBTCBalance = totalBTCBalance.plus(lastKnownPriceInBTC.multiply(quantity))
      totalEURBalance = totalEURBalance.plus(lastKnownPriceInEUR.multiply(quantity))
    }

    return Balances(FiatBalance(totalUSDBalance, totalEURBalance), totalBTCBalance)
  }

  fun getCryptoTotalBalances(crypto: Crypto, quantity: BigDecimal): Balances {
    val fiatBalance = FiatBalance(
      crypto.lastKnownPrice.multiply(quantity),
      crypto.lastKnownPriceInEUR.multiply(quantity)
    )
    val btc = crypto.lastKnownPriceInBTC.multiply(quantity)

    return Balances(fiatBalance, btc)
  }

  fun calculatePercentage(totalBalance: String, balance: String) = BigDecimal(balance)
    .multiply(BigDecimal("100"))
    .divide(BigDecimal(totalBalance), 2, RoundingMode.HALF_UP)
    .toFloat()

  // Map<coingecko crypto id, user crypto quantity>
  fun getUserCryptoQuantity(userCryptos: List<UserCrypto>) = userCryptos.groupBy { it.coingeckoCryptoId }
    .mapValues { (_, userCryptos) -> userCryptos.sumOf { it.quantity } }

  private fun LocalDate.retrieveDatesBalances(minusDays: Long): List<DateBalance> {
    val from = this.minusDays(minusDays)
    logger.info { "Retrieving date balances from $from to $this" }

    return dateBalanceRepository.findDateBalancesByInclusiveDateBetween(from.toString(), this.toString())
  }

  private fun retrieveDatesBalances(
    daysSubtraction: Long, minRequired: Int,
    from: LocalDate, to: LocalDate
  ): List<DateBalance> {
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

  private fun retrieveStableCoinsBalance(userStableCoins: List<UserCrypto>, stableCoins: List<Crypto>): String {
    val userCryptoQuantity = getUserCryptoQuantity(userStableCoins)
    val totalBalances = getTotalBalances(stableCoins, userCryptoQuantity)

    return totalBalances.usd()
  }

  private fun getChange(balanceType: BalanceType, dateBalances: List<DateBalances>): Pair<Float, String> {
    val newestValues = dateBalances.last().balances
    val oldestValues = dateBalances.first().balances
    val divisionScale = if (BalanceType.BTC_BALANCE == balanceType) 10 else 4

    val values = when (balanceType) {
      BalanceType.USD_BALANCE -> Pair(BigDecimal(oldestValues.usd()), BigDecimal(newestValues.usd()))
      BalanceType.EUR_BALANCE -> Pair(BigDecimal(oldestValues.eur()), BigDecimal(newestValues.eur()))
      BalanceType.BTC_BALANCE -> Pair(BigDecimal(oldestValues.btc), BigDecimal(newestValues.btc))
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
}

enum class BalanceType {
  USD_BALANCE,
  EUR_BALANCE,
  BTC_BALANCE
}
