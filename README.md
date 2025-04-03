# Crypto Balance Tracker :rocket:

|               |                                                                                                                                                                                         |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Pipeline      | [![Pipeline Status](https://github.com/lucasdistasi/crypto-balance-tracker-kt/actions/workflows/main.yml/badge.svg)](https://github.com/lucasdistasi/crypto-balance-tracker-kt/actions) |
| License       | [![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)                                                                                 |
| Code Coverage | [![Code Coverage](https://github.com/lucasdistasi/crypto-balance-tracker-kt/blob/gh-pages/badges/jacoco.svg)](https://lucasdistasi.github.io/crypto-balance-tracker-kt/)                |
| Project views | [![Project views](https://hits.dwyl.com/lucasdistasi/crypto-balance-tracker-kt.svg)]()                                                                                                  |

Crypto Balance Tracker is a Kotlin-Spring application that acts as a portfolio tracker for monitoring your crypto
assets.
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

## Technologies and stuff used :sparkles:

- Java 21
- Kotlin 2.1.20
- Spring 6 & Spring Boot 3.2.2
    - Spring WebFlux
    - Hibernate
    - OpenAPI
- Ehcache
- MongoDB
- JUnit 5 - Mockk
- oshai kotlin-logging
- JaCoCo

## Features

- Add/Update/Delete Platforms, Cryptos, Goals.
- Transfer crypto and automatically calculate new quantities.
- View crypto/s and platform/s insights.

### I want to try this API on my local machine. What should I do? :tada:

---

Before starting, you must know that this application can be used with security, so by enabling security, 
all endpoints will require a JWT token. This is not yet implemented in the front-end application.
Also, a Coingecko api key is required, you can get one by creating a demo account [here](https://www.coingecko.com/en/api/pricing)

That being said, below you can find the instructions to run the application.

1. Have installed and running.
   - Docker
   - Docker Compose
   - TypeScript
2. Clone needed repositories.
    - [crypto-balance-tracker](https://github.com/lucasdistasi/crypto-balance-tracker-kt)
    - [crypto-balance-tracker-ui](https://github.com/lucasdistasi/crypto-balance-tracker-ui)
    - [cbt-mongo-seed](https://github.com/lucasdistasi/cbt-mongo.seed) (not needed if security is disabled)
    - [crypto-balance-tracker-login](https://github.com/lucasdistasi/crypto-balance-tracker-login) (not needed if security is disabled)
3. DEMO_COINGECKO_API_KEY. API Key from Coingecko. If you have a PRO account fill PRO_COINGECKO_API_KEY and leave this one empty.
4. If you want to secure the app, set the _security.enabled_ property in application.yml from this project to true.
   Default value is false.
5. Set up environment variables in _.env_ file.
   1. MONGODB_DATABASE. The name of the database.
   2. JWT_SIGNING_KEY. The signing key. Leave empty if security is disabled.
   3. COINGECKO_API_KEY. API Key from PRO Account. If you don't have one, leave it empty.
6. Set your desired values in [cbt-mongo-seed](https://github.com/lucasdistasi/cbt-mongo.seed)  **(remember this is not needed if security is disabled)**.
7. Run `./gradlew bootJar` on the root of this project to create the executable jar that's going to be used by Docker to
   build the image.
8. Create docker images (`docker build`) for the projects. Bear in mind that the docker image must match the project name.
   - [crypto-balance-tracker-kt](https://github.com/lucasdistasi/crypto-balance-tracker-kt)
   - [crypto-balance-tracker-ui](https://github.com/lucasdistasi/crypto-balance-tracker-ui)
   - [cbt-mongo-seed](https://github.com/lucasdistasi/cbt-mongo.seed) (not needed if security is disabled)
   - [crypto-balance-tracker-login](https://github.com/lucasdistasi/crypto-balance-tracker-login) (not needed if security is disabled)
9. On this project folder run `docker ompose up` if you don't want to use it with security
   or `docker-compose -f docker-compose-security.yml up` if you want to use it with security.
10. Open the URL `http://localhost:5173` on your favourite web browser.
11. Boila!

## Contributing :coffee:

Feel free to star, fork, or study from the code! If you'd like to contribute, you can gift me a coffee.

| Crypto | Network | Address                                    | QR            |
|--------|---------|--------------------------------------------|---------------|
| BTC    | Bitcoin | 15gJYCyCwoHVE3MpjwDYLM51zLRoKo2Q9h         | [BTC-bitcoin] |
| BTC    | TRC20   | TFVmahp7YQiEwd9bh4dEgF7fZyGjrQ7TRW         | [BTC-trc20]   |
| ETH    | BEP20   | 0x304714FDA2060c570B1afb1BC231C0973abBEC23 | [ETH-bep20]   |
| ETH    | ERC20   | 0x304714FDA2060c570B1afb1BC231C0973abBEC23 | [ETH-erc20]   |
| USDT   | TRC20   | TFVmahp7YQiEwd9bh4dEgF7fZyGjrQ7TRW         | [USDT-trc20]  |
| USDT   | BEP20   | 0x304714FDA2060c570B1afb1BC231C0973abBEC23 | [USDT-bep20]  |
| USDT   | ERC20   | 0x304714FDA2060c570B1afb1BC231C0973abBEC23 | [USDT-erc20]  |

[BTC-bitcoin]: https://imgur.com/Hs0DYDk

[BTC-trc20]: https://imgur.com/kdROHrE

[ETH-bep20]: https://imgur.com/DIOiJrL

[ETH-erc20]: https://imgur.com/REXkDmu

[USDT-trc20]: https://imgur.com/ubUWdpI

[USDT-bep20]: https://imgur.com/rrrYd9j

[USDT-erc20]: https://imgur.com/G9DPKvU

### Below you can find some examples with random data of the information that each endpoint retrieves :memo:

Bear in mind that the below ones aren't all the endpoints, but only the ones used to display data in tables and charts (GET endpoints).
You can check [this repository](https://github.com/lucasdistasi/postman-collections) to find a Postman collection with all the available methods.

<details>
  <summary>Response examples</summary>

## Insights

### Retrieve total balances

`/api/v1/insights/balances`

```json
{
  "totalUSDBalance": "6127.00",
  "totalEURBalance": "5737.71",
  "totalBTCBalance": "0.165174680229"
}
```

### Retrieve insights for the given platformId

`/api/v1/insights/platforms/{platformId}`

```json
{
  "platformName": "BINANCE",
  "balances": {
    "totalUSDBalance": "4462.45",
    "totalEURBalance": "4177.32",
    "totalBTCBalance": "0.121175776909"
  },
  "cryptos": [
    {
      "id": "3f64cb0b-844a-4f7e-b19d-7a158ecd7f05",
      "cryptoName": "Bitcoin",
      "cryptoId": "bitcoin",
      "quantity": "0.112371283",
      "balances": {
        "totalUSDBalance": "4138.07",
        "totalEURBalance": "3873.66",
        "totalBTCBalance": "0.112371283"
      },
      "percentage": 92.73
    },
    {
      "id": "412e2361-a650-468b-b21e-b26053be6dcf", 
      "cryptoName": "Ethereum",
      "cryptoId": "ethereum",
      "quantity": "0.12349",
      "balances": {
        "totalUSDBalance": "258.62",
        "totalEURBalance": "242.10",
        "totalBTCBalance": "0.007019493909"
      },
      "percentage": 5.8
    },
    {
      "id": "42241c9c-eda8-45c1-a603-7ad815ffed7b",
      "cryptoName": "XRP",
      "cryptoId": "ripple",
      "quantity": "100",
      "balances": {
        "totalUSDBalance": "65.76",
        "totalEURBalance": "61.56",
        "totalBTCBalance": "0.001785"
      },
      "percentage": 1.47
    }
  ]
}
```

### Retrieve user crypto insights for the given coingeckoCryptoId

`/api/v1/insights/cryptos/{coingeckoCryptoId}`

```json
{
  "cryptoName": "Tether",
  "balances": {
    "totalUSDBalance": "384.78",
    "totalEURBalance": "360.16",
    "totalBTCBalance": "0.0104412"
  },
  "platforms": [
    {
      "quantity": "200",
      "balances": {
        "totalUSDBalance": "199.88",
        "totalEURBalance": "187.10",
        "totalBTCBalance": "0.005424"
      },
      "percentage": 51.95,
      "platformName": "OKX"
    },
    {
      "quantity": "185",
      "balances": {
        "totalUSDBalance": "184.89",
        "totalEURBalance": "173.07",
        "totalBTCBalance": "0.0050172"
      },
      "percentage": 48.05,
      "platformName": "KRAKEN"
    }
  ]
}
```

### Retrieve user cryptos insights in all platforms by page

`/api/v1/insights/cryptos?page={page}&sortBy={sortBy}&sortType={sortType}`

```json
{
  "page": 1,
  "totalPages": 2,
  "hasNextPage": true,
  "balances": {
    "totalUSDBalance": "6088.78",
    "totalEURBalance": "5699.70",
    "totalBTCBalance": "0.165316546142"
  },
  "cryptos": [
    {
      "cryptoInfo": {
        "cryptoName": "Bitcoin",
        "cryptoId": "bitcoin",
        "symbol": "btc",
        "image": "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1696501400"
      },
      "quantity": "0.112371283",
      "percentage": 67.96,
      "balances": {
        "totalUSDBalance": "4138.07",
        "totalEURBalance": "3873.66",
        "totalBTCBalance": "0.112371283"
      },
      "marketCapRank": 1,
      "marketData": {
        "circulatingSupply": "19538343.0",
        "maxSupply": "21000000.0",
        "currentPrice": {
          "usd": "36825",
          "eur": "34472",
          "btc": "1.0"
        },
        "marketCap": "819249388691",
        "priceChange": {
           "changePercentageIn24h": 1.04,
           "changePercentageIn7d": 0.53,
           "changePercentageIn30d": -2.99
        }
      },
      "platforms": [
        "BINANCE"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "Ethereum",
        "cryptoId": "ethereum",
        "symbol": "eth",
        "image": "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1696501628"
      },
      "quantity": "0.3816173123",
      "percentage": 13.13,
      "balances": {
        "totalUSDBalance": "799.21",
        "totalEURBalance": "748.14",
        "totalBTCBalance": "0.021692124052"
      },
      "marketCapRank": 2,
      "marketData": {
        "circulatingSupply": "120263563.630836",
        "maxSupply": "0",
        "currentPrice": {
          "usd": "2094.27",
          "eur": "1960.45",
          "btc": "0.05684261"
        },
        "marketCap": "272316630944",
        "priceChange": {
           "changePercentageIn24h": 0.55,
           "changePercentageIn7d": -8.24,
           "changePercentageIn30d": -5.64
        }
      },
      "platforms": [
        "COINBASE",
        "BINANCE"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "Tether",
        "cryptoId": "tether",
        "symbol": "usdt",
        "image": "https://assets.coingecko.com/coins/images/325/large/Tether.png?1696501661"
      },
      "quantity": "385",
      "percentage": 6.32,
      "balances": {
        "totalUSDBalance": "384.78",
        "totalEURBalance": "360.16",
        "totalBTCBalance": "0.0104412"
      },
      "marketCapRank": 3,
      "marketData": {
        "circulatingSupply": "86517250035.3132",
        "maxSupply": "0",
        "currentPrice": {
          "usd": "0.999419",
          "eur": "0.935491",
          "btc": "0.00002712"
        },
        "marketCap": "96022661565",
        "priceChange": {
           "changePercentageIn24h": 0.01,
           "changePercentageIn7d": 0.01,
           "changePercentageIn30d": 0.00
        }
      },
      "platforms": [
        "OKX",
        "KRAKEN"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "BNB",
        "cryptoId": "binancecoin",
        "symbol": "bnb",
        "image": "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1696501970"
      },
      "quantity": "0.75",
      "percentage": 3.11,
      "balances": {
        "totalUSDBalance": "189.50",
        "totalEURBalance": "177.40",
        "totalBTCBalance": "0.0051435"
      },
      "marketCapRank": 4,
      "marketData": {
        "circulatingSupply": "153856150.0",
        "maxSupply": "200000000.0",
        "currentPrice": {
          "usd": "252.67",
          "eur": "236.53",
          "btc": "0.006858"
        },
        "marketCap": "46704503158",
        "priceChange": {
           "changePercentageIn24h": 0.95,
           "changePercentageIn7d": -3.65,
           "changePercentageIn30d": -9.73
        }
      },
      "platforms": [
        "BYBIT"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "Solana",
        "cryptoId": "solana",
        "symbol": "sol",
        "image": "https://assets.coingecko.com/coins/images/4128/large/solana.png?1696504756"
      },
      "quantity": "2",
      "percentage": 1.61,
      "balances": {
        "totalUSDBalance": "98.10",
        "totalEURBalance": "91.84",
        "totalBTCBalance": "0.00266284"
      },
      "marketCapRank": 5,
      "marketData": {
        "circulatingSupply": "421017098.503324",
        "maxSupply": "0",
        "currentPrice": {
          "usd": "49.05",
          "eur": "45.92",
          "btc": "0.00133142"
        },
        "marketCap": "39892642944",
        "priceChange": {
           "changePercentageIn24h": 1.26,
           "changePercentageIn7d": 0.17,
           "changePercentageIn30d": -9.40
        }
      },
      "platforms": [
        "KRAKEN"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "Litecoin",
        "cryptoId": "litecoin",
        "symbol": "ltc",
        "image": "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1696501400"
      },
      "quantity": "1.123891239",
      "percentage": 1.35,
      "balances": {
        "totalUSDBalance": "82.26",
        "totalEURBalance": "77.01",
        "totalBTCBalance": "0.002231811983"
      },
      "marketCapRank": 21,
      "marketData": {
        "circulatingSupply": "73857601.9834713",
        "maxSupply": "84000000.0",
        "currentPrice": {
          "usd": "73.19",
          "eur": "68.52",
          "btc": "0.00198579"
        },
        "marketCap": "4973877592",
        "priceChange": {
           "changePercentageIn24h": 0.59,
           "changePercentageIn7d": -5.55,
           "changePercentageIn30d": -6.22
        }
      },
      "platforms": [
        "COINBASE"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "Polkadot",
        "cryptoId": "polkadot",
        "symbol": "dot",
        "image": "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1696512008"
      },
      "quantity": "15",
      "percentage": 1.27,
      "balances": {
        "totalUSDBalance": "77.25",
        "totalEURBalance": "72.30",
        "totalBTCBalance": "0.0020943"
      },
      "marketCapRank": 13,
      "marketData": {
        "circulatingSupply": "1294982120.91743",
        "maxSupply": "0",
        "currentPrice": {
          "usd": "5.15",
          "eur": "4.82",
          "btc": "0.00013962"
        },
        "marketCap": "8816873309",
        "priceChange": {
           "changePercentageIn24h": 1.43,
           "changePercentageIn7d": -2.87,
           "changePercentageIn30d": -21.80
        }
      },
      "platforms": [
        "TREZOR"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "Dogecoin",
        "cryptoId": "dogecoin",
        "symbol": "doge",
        "image": "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1696501409"
      },
      "quantity": "1000.21381",
      "percentage": 1.22,
      "balances": {
        "totalUSDBalance": "74.32",
        "totalEURBalance": "69.57",
        "totalBTCBalance": "0.002020431896"
      },
      "marketCapRank": 11,
      "marketData": {
        "circulatingSupply": "141771566383.705",
        "maxSupply": "0",
        "currentPrice": {
          "usd": "0.0743",
          "eur": "0.069553",
          "btc": "0.00000202"
        },
        "marketCap": "11390548620",
        "priceChange": {
           "changePercentageIn24h": 0.62,
           "changePercentageIn7d": 0.57,
           "changePercentageIn30d": -13.80
        }
      },
      "platforms": [
        "COINBASE"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "NEO",
        "cryptoId": "neo",
        "symbol": "neo",
        "image": "https://assets.coingecko.com/coins/images/480/large/NEO_512_512.png?1696501735"
      },
      "quantity": "5",
      "percentage": 1.13,
      "balances": {
        "totalUSDBalance": "69.00",
        "totalEURBalance": "64.60",
        "totalBTCBalance": "0.0018731"
      },
      "marketCapRank": 91,
      "marketData": {
        "circulatingSupply": "70530000.0",
        "maxSupply": "0",
        "currentPrice": {
          "usd": "13.8",
          "eur": "12.92",
          "btc": "0.00037462"
        },
        "marketCap": "773235131",
        "priceChange": {
           "changePercentageIn24h": 1.66,
           "changePercentageIn7d": -4.16,
           "changePercentageIn30d": -20.91
        }
      },
      "platforms": [
        "OKX"
      ]
    },
    {
      "cryptoInfo": {
        "cryptoName": "XRP",
        "cryptoId": "ripple",
        "symbol": "xrp",
        "image": "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1696501442"
      },
      "quantity": "100",
      "percentage": 1.08,
      "balances": {
        "totalUSDBalance": "65.76",
        "totalEURBalance": "61.56",
        "totalBTCBalance": "0.001785"
      },
      "marketCapRank": 6,
      "marketData": {
        "circulatingSupply": "53652766196.0",
        "maxSupply": "100000000000.0",
        "currentPrice": {
          "usd": "0.657608",
          "eur": "0.615587",
          "btc": "0.00001785"
        },
        "marketCap": "28823399406",
        "priceChange": {
           "changePercentageIn24h": 2.02,
           "changePercentageIn7d": -3.52,
           "changePercentageIn30d": -16.65
        }
      },
      "platforms": [
        "BINANCE"
      ]
    }
  ]
}
```

### Retrieve user cryptos insights

`/api/v1/insights/cryptos/balances`

```json
[
   {
      "name": "Bitcoin",
      "balance": "8535.23",
      "percentage": 70.69
   },
   {
      "name": "Ethereum",
      "balance": "1996.03",
      "percentage": 16.53
   },
   {
      "name": "Solana",
      "balance": "918.47",
      "percentage": 7.61
   },
   {
      "name": "XRP",
      "balance": "361.50",
      "percentage": 2.99
   },
   {
      "name": "Chainlink",
      "balance": "215.70",
      "percentage": 1.79
   },
   {
      "name": "TRON",
      "balance": "23.01",
      "percentage": 0.19
   },
   {
      "name": "USDC",
      "balance": "12.00",
      "percentage": 0.1
   },
   {
      "name": "Illuvium",
      "balance": "9.75",
      "percentage": 0.08
   },
   {
      "name": "Polygon",
      "balance": "2.12",
      "percentage": 0.02
   },
   {
      "name": "Dogecoin",
      "balance": "0.86",
      "percentage": 0.01
   }
]
```

### Retrieve balances insights for all platforms

`/api/v1/insights/platforms/balances`

```json
[
  {
    "name": "BINANCE",
    "balance": "8526.70",
    "percentage": 70.62
  },
  {
    "name": "MEXC",
    "balance": "1996.03",
    "percentage": 16.53
  },
  {
    "name": "BINGX",
    "balance": "862.63",
    "percentage": 7.14
  },
  {
    "name": "TEST",
    "balance": "689.31",
    "percentage": 5.71
  }
]
```

## User Cryptos

### Retrieve user cryptos by page

`/api/v1/cryptos?page={page}`

```json
{
  "page": 1,
  "totalPages": 2,
  "hasNextPage": true,
  "cryptos": [
    {
      "id": "597ee816-416e-4b78-b9ce-ed16313a6e8a",
      "cryptoName": "Bitcoin",
      "quantity": "0.112371283",
      "platform": "BINANCE"
    },
    {
      "id": "0d40df86-5d39-42af-8762-cdf90d2753ad",
      "cryptoName": "Ethereum",
      "quantity": "0.2581273123",
      "platform": "COINBASE"
    },
    {
      "id": "2e206c40-4453-4a51-9146-926100c1e7cd",
      "cryptoName": "Tether",
      "quantity": "200",
      "platform": "OKX"
    },
    {
      "id": "5bf2dd37-bcc6-4d15-8468-fcdada3d838a",
      "cryptoName": "BNB",
      "quantity": "0.75",
      "platform": "BYBIT"
    },
    {
      "id": "597735e5-4ee9-4d07-bb93-04308381db5e",
      "cryptoName": "XRP",
      "quantity": "100",
      "platform": "BINANCE"
    },
    {
      "id": "19b6efa6-31d9-4d63-82c8-c252a7c33bba",
      "cryptoName": "Solana",
      "quantity": "2",
      "platform": "KRAKEN"
    },
    {
      "id": "efea34ce-eca4-4b76-8357-7190ffe2cae6",
      "cryptoName": "Cardano",
      "quantity": "100.501",
      "platform": "COINBASE"
    },
    {
      "id": "a7297b23-68b5-46aa-b91c-ff6d022be59e",
      "cryptoName": "Dogecoin",
      "quantity": "1000.21381",
      "platform": "COINBASE"
    },
    {
      "id": "34eb45cb-1bd4-4a42-827f-69022c5ebdd2",
      "cryptoName": "Polygon",
      "quantity": "50",
      "platform": "KRAKEN"
    },
    {
      "id": "14c60428-8761-4859-a8fb-485505f3dbd0",
      "cryptoName": "Polkadot",
      "quantity": "15",
      "platform": "TREZOR"
    }
  ]
}
```

### Retrieve user crypto by userCryptoId

`/api/v1/cryptos/{userCryptoId}`

```json
{
  "id": "597ee816-416e-4b78-b9ce-ed16313a6e8a",
  "cryptoName": "Bitcoin",
  "quantity": "0.112371283",
  "platform": "BINANCE"
}
```

## Goals

### Retrieve goals by page

`/api/v1/goals?page={page}`

```json
{
  "page": 1,
  "totalPages": 1,
  "hasNextPage": false,
  "goals": [
    {
      "id": "bd0b9c6f-305e-45bb-9536-3fe9dd1a9a2f",
      "cryptoName": "Bitcoin",
      "actualQuantity": "0.112371283",
      "progress": 100.0,
      "remainingQuantity": "0",
      "goalQuantity": "0.1",
      "moneyNeeded": "0.00"
    },
    {
      "id": "0a8a0416-0392-4a07-91cb-80a7bde1acdf",
      "cryptoName": "Ethereum",
      "actualQuantity": "0.3816173123",
      "progress": 38.16,
      "remainingQuantity": "0.6183826877",
      "goalQuantity": "1",
      "moneyNeeded": "1298.57"
    },
    {
      "id": "eab99e1f-ac21-45ea-8c85-200087b0c081",
      "cryptoName": "Bitcoin Cash",
      "actualQuantity": "0",
      "progress": 0.0,
      "remainingQuantity": "4",
      "goalQuantity": "4",
      "moneyNeeded": "965.36"
    }
  ]
}
```

### Retrieve goal by goalId

`/api/v1/goals/{goalId}`

```json
{
  "id": "bd0b9c6f-305e-45bb-9536-3fe9dd1a9a2f",
  "cryptoName": "Bitcoin",
  "actualQuantity": "0.112371283",
  "progress": 100.0,
  "remainingQuantity": "0",
  "goalQuantity": "0.1",
  "moneyNeeded": "0.00"
}
```

## Platforms

### Retrieve all platforms

`/api/v1/platforms`

```json
[
  {
    "id": "db13cdbc-f33e-4ca3-acd0-7357bb99e0e2",
    "name": "BINANCE"
  },
  {
    "id": "97363a3c-35e3-49fd-b183-4ee5e881c99e",
    "name": "COINBASE"
  },
  {
    "id": "5991df81-66f9-4b5f-8067-dbf559caaae9",
    "name": "TREZOR"
  },
  {
    "id": "bb4fc7ad-1aaa-431f-9ca7-71c6f246a98c",
    "name": "OKX"
  },
  {
    "id": "05b5071d-4897-44e8-9b6b-343ceabd4d05",
    "name": "BYBIT"
  },
  {
    "id": "7a9dc422-ee15-4a8e-9ca6-fd7178b214fb",
    "name": "KRAKEN"
  }
]
```

### Retrieve platform by platformId

`/api/v1/platforms/{platformId}`

```json
{
   "id": "db13cdbc-f33e-4ca3-acd0-7357bb99e0e2",
   "name": "BINANCE"
}
```

</details>