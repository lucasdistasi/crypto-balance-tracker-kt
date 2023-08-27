# Crypto Balance Tracker :rocket:

|               |                                                                                                                                                                                         |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Pipeline      | [![Pipeline Status](https://github.com/lucasdistasi/crypto-balance-tracker-kt/actions/workflows/main.yml/badge.svg)](https://github.com/lucasdistasi/crypto-balance-tracker-kt/actions) |
| Code Coverage | [![Code Coverage](https://github.com/lucasdistasi/crypto-balance-tracker-kt/blob/gh-pages/badges/jacoco.svg)](https://lucasdistasi.github.io/crypto-balance-tracker-kt/)                |
| Project views | [![Project views](https://hits.dwyl.com/lucasdistasi/crypto-balance-tracker-kt.svg)]()                                                                                                  |

Crypto Balance Tracker is a Kotlin-Spring application that acts as a portfolio tracker for monitoring your crypto assets.
It allows you to retrieve data such as the percentage of each crypto owned, the total value of your assets,
the current price of each crypto, the balance per platform, and many more data! The application makes use of the
[Coingecko](https://www.coingecko.com) API to fetch all the required information about the cryptos.

:warning: Please note that the Coingecko API
has [rate limits for the Free Plan](https://www.coingecko.com/en/api/pricing).
To avoid hitting the rate limit, a scheduler retrieves and updates the price of the saved cryptos every 180 seconds.
This ensures that the end-users do not exceed the rate limit by making multiple API calls.

Keep in mind that the balances displayed in the app might not be 100% accurate due to variations in price data
from different exchanges. However, any discrepancies should be minimal.
<br>

## Approach and Challenges

Initially, the idea was to allow users to add their wallet addresses to track their cryptos. However, this approach
posed
some challenges, such as specifying the network for the address and the difficulty of tracking cryptos held in exchange
addresses shared by multiple users. Due to these complexities, tracking cryptos based on non cold/hard-wallet addresses
became unfeasible.
Instead, the current approach was adopted to provide a more reliable and feasible solution.
<br>

## IMPORTANT :fire:

Investing in cryptocurrencies comes with high risk and volatility. This app was made only for educational purposes.
Do your own research before investing money you are not willing to loss.