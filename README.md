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

- Add/Update/Delete:
  - Platforms
  - Goals
  - Price Targets
  - Cryptos
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
   "platformName": "BINGX",
   "balances": {
      "totalUSDBalance": "790.73",
      "totalEURBalance": "700.18",
      "totalBTCBalance": "0.00940208"
   },
   "cryptos": [
      {
         "id": "37919ee8-e3cb-42f7-ade2-0af669501a9c",
         "userCryptoInfo": {
            "cryptoInfo": {
               "cryptoName": "XRP",
               "cryptoId": "ripple",
               "symbol": "xrp",
               "image": "https://coin-images.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1696501442"
            },
            "quantity": "150",
            "percentage": 40.22,
            "balances": {
               "totalUSDBalance": "318.00",
               "totalEURBalance": "282.00",
               "totalBTCBalance": "0.003783"
            }
         }
      },
      {
         "id": "32c94337-0e16-4860-ad74-acce50ca1285",
         "userCryptoInfo": {
            "cryptoInfo": {
               "cryptoName": "Solana",
               "cryptoId": "solana",
               "symbol": "sol",
               "image": "https://coin-images.coingecko.com/coins/images/4128/large/solana.png?1718769756"
            },
            "quantity": "2",
            "percentage": 32.5,
            "balances": {
               "totalUSDBalance": "256.96",
               "totalEURBalance": "227.24",
               "totalBTCBalance": "0.00305408"
            }
         }
      },
      {
         "id": "b1b94355-1f09-4bbe-ba8e-f087c2465c2d",
         "userCryptoInfo": {
            "cryptoInfo": {
               "cryptoName": "Chainlink",
               "cryptoId": "chainlink",
               "symbol": "link",
               "image": "https://coin-images.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1696502009"
            },
            "quantity": "15",
            "percentage": 24.09,
            "balances": {
               "totalUSDBalance": "190.50",
               "totalEURBalance": "168.60",
               "totalBTCBalance": "0.002265"
            }
         }
      },
      {
         "id": "e9aeaa72-d9db-4a5d-aa66-e2c1540e9900",
         "userCryptoInfo": {
            "cryptoInfo": {
               "cryptoName": "TRON",
               "cryptoId": "tron",
               "symbol": "trx",
               "image": "https://coin-images.coingecko.com/coins/images/1094/large/tron-logo.png?1696502193"
            },
            "quantity": "100",
            "percentage": 3.2,
            "balances": {
               "totalUSDBalance": "25.27",
               "totalEURBalance": "22.34",
               "totalBTCBalance": "0.0003"
            }
         }
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
      "totalUSDBalance": "11318.38",
      "totalEURBalance": "10250.32",
      "totalBTCBalance": "0.1380989847"
   },
   "cryptos": [
      {
         "cryptoInfo": {
            "cryptoName": "Bitcoin",
            "cryptoId": "bitcoin",
            "symbol": "btc",
            "image": "https://coin-images.coingecko.com/coins/images/1/large/bitcoin.png?1696501400",
            "currentPrice": {
               "usd": "81935",
               "eur": "74199",
               "btc": "1.0"
            },
            "priceChange": {
               "changePercentageIn24h": -5.75,
               "changePercentageIn7d": -5.77,
               "changePercentageIn30d": -1.19
            }
         },
         "quantity": "0.1001",
         "percentage": 72.46,
         "balances": {
            "totalUSDBalance": "8201.69",
            "totalEURBalance": "7427.32",
            "totalBTCBalance": "0.1001"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "Ethereum",
            "cryptoId": "ethereum",
            "symbol": "eth",
            "image": "https://coin-images.coingecko.com/coins/images/279/large/ethereum.png?1696501628",
            "currentPrice": {
               "usd": "1780.46",
               "eur": "1612.37",
               "btc": "0.02172093"
            },
            "priceChange": {
               "changePercentageIn24h": -6.45,
               "changePercentageIn7d": -11.23,
               "changePercentageIn30d": -13.36
            }
         },
         "quantity": "1",
         "percentage": 15.73,
         "balances": {
            "totalUSDBalance": "1780.46",
            "totalEURBalance": "1612.37",
            "totalBTCBalance": "0.02172093"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "Solana",
            "cryptoId": "solana",
            "symbol": "sol",
            "image": "https://coin-images.coingecko.com/coins/images/4128/large/solana.png?1718769756",
            "currentPrice": {
               "usd": "114.24",
               "eur": "103.45",
               "btc": "0.00139363"
            },
            "priceChange": {
               "changePercentageIn24h": -12.66,
               "changePercentageIn7d": -17.22,
               "changePercentageIn30d": -17.32
            }
         },
         "quantity": "7",
         "percentage": 7.07,
         "balances": {
            "totalUSDBalance": "799.68",
            "totalEURBalance": "724.15",
            "totalBTCBalance": "0.00975541"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "XRP",
            "cryptoId": "ripple",
            "symbol": "xrp",
            "image": "https://coin-images.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1696501442",
            "currentPrice": {
               "usd": "2.01",
               "eur": "1.82",
               "btc": "0.00002447"
            },
            "priceChange": {
               "changePercentageIn24h": -7.55,
               "changePercentageIn7d": -14.45,
               "changePercentageIn30d": -16.33
            }
         },
         "quantity": "150",
         "percentage": 2.66,
         "balances": {
            "totalUSDBalance": "301.50",
            "totalEURBalance": "273.00",
            "totalBTCBalance": "0.0036705"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "Chainlink",
            "cryptoId": "chainlink",
            "symbol": "link",
            "image": "https://coin-images.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1696502009",
            "currentPrice": {
               "usd": "12.51",
               "eur": "11.33",
               "btc": "0.00015262"
            },
            "priceChange": {
               "changePercentageIn24h": -11.1,
               "changePercentageIn7d": -19.72,
               "changePercentageIn30d": -8.91
            }
         },
         "quantity": "15",
         "percentage": 1.66,
         "balances": {
            "totalUSDBalance": "187.65",
            "totalEURBalance": "169.95",
            "totalBTCBalance": "0.0022893"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "TRON",
            "cryptoId": "tron",
            "symbol": "trx",
            "image": "https://coin-images.coingecko.com/coins/images/1094/large/tron-logo.png?1696502193",
            "currentPrice": {
               "usd": "0.230106",
               "eur": "0.212638",
               "btc": "0.0000027"
            },
            "priceChange": {
               "changePercentageIn24h": -3.23,
               "changePercentageIn7d": 8.08,
               "changePercentageIn30d": -2.91
            }
         },
         "quantity": "100",
         "percentage": 0.2,
         "balances": {
            "totalUSDBalance": "23.01",
            "totalEURBalance": "21.26",
            "totalBTCBalance": "0.00027"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "USDC",
            "cryptoId": "usd-coin",
            "symbol": "usdc",
            "image": "https://coin-images.coingecko.com/coins/images/6319/large/usdc.png?1696506694",
            "currentPrice": {
               "usd": "0.999944",
               "eur": "0.905541",
               "btc": "0.0000122"
            },
            "priceChange": {
               "changePercentageIn24h": 0,
               "changePercentageIn7d": 0.01,
               "changePercentageIn30d": 0.01
            }
         },
         "quantity": "12",
         "percentage": 0.11,
         "balances": {
            "totalUSDBalance": "12.00",
            "totalEURBalance": "10.87",
            "totalBTCBalance": "0.0001464"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "Illuvium",
            "cryptoId": "illuvium",
            "symbol": "ilv",
            "image": "https://coin-images.coingecko.com/coins/images/14468/large/logo-200x200.png?1696514154",
            "currentPrice": {
               "usd": "14.78",
               "eur": "13.65",
               "btc": "0.00017312"
            },
            "priceChange": {
               "changePercentageIn24h": 0.48,
               "changePercentageIn7d": 7.55,
               "changePercentageIn30d": -18.26
            }
         },
         "quantity": "0.66",
         "percentage": 0.09,
         "balances": {
            "totalUSDBalance": "9.75",
            "totalEURBalance": "9.01",
            "totalBTCBalance": "0.0001142592"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "Polygon",
            "cryptoId": "matic-network",
            "symbol": "matic",
            "image": "https://coin-images.coingecko.com/coins/images/4713/large/polygon.png?1698233745",
            "currentPrice": {
               "usd": "0.184599",
               "eur": "0.167171",
               "btc": "0.00000225"
            },
            "priceChange": {
               "changePercentageIn24h": -7.12,
               "changePercentageIn7d": -21.35,
               "changePercentageIn30d": -23.7
            }
         },
         "quantity": "10",
         "percentage": 0.02,
         "balances": {
            "totalUSDBalance": "1.85",
            "totalEURBalance": "1.67",
            "totalBTCBalance": "0.0000225"
         }
      },
      {
         "cryptoInfo": {
            "cryptoName": "Dogecoin",
            "cryptoId": "dogecoin",
            "symbol": "doge",
            "image": "https://coin-images.coingecko.com/coins/images/5/large/dogecoin.png?1696501409",
            "currentPrice": {
               "usd": "0.158201",
               "eur": "0.143266",
               "btc": "0.00000193"
            },
            "priceChange": {
               "changePercentageIn24h": -9.58,
               "changePercentageIn7d": -17.7,
               "changePercentageIn30d": -17.7
            }
         },
         "quantity": "5",
         "percentage": 0.01,
         "balances": {
            "totalUSDBalance": "0.79",
            "totalEURBalance": "0.72",
            "totalBTCBalance": "0.00000965"
         }
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
         "id": "4eb38add-cb71-4bbc-8c0b-c8097d8ad55b",
         "cryptoInfo": {
            "cryptoName": "TRON",
            "cryptoId": "tron",
            "symbol": "trx",
            "image": "https://coin-images.coingecko.com/coins/images/1094/large/tron-logo.png?1696502193"
         },
         "actualQuantity": "100",
         "progress": 100,
         "remainingQuantity": "0",
         "goalQuantity": "1",
         "moneyNeeded": "0.00"
      },
      {
         "id": "8f290e74-4eee-49f5-b02a-b983a4328c3c",
         "cryptoInfo": {
            "cryptoName": "Bitcoin",
            "cryptoId": "bitcoin",
            "symbol": "btc",
            "image": "https://coin-images.coingecko.com/coins/images/1/large/bitcoin.png?1696501400"
         },
         "actualQuantity": "0.0751",
         "progress": 15.02,
         "remainingQuantity": "0.4249",
         "goalQuantity": "0.5",
         "moneyNeeded": "35749.81"
      }
   ]
}
```

### Retrieve goal by goalId

`/api/v1/goals/{goalId}`

```json
{
   "id": "4eb38add-cb71-4bbc-8c0b-c8097d8ad55b",
   "cryptoInfo": {
      "cryptoName": "TRON",
      "cryptoId": "tron",
      "symbol": "trx",
      "image": "https://coin-images.coingecko.com/coins/images/1094/large/tron-logo.png?1696502193"
   },
   "actualQuantity": "100",
   "progress": 100,
   "remainingQuantity": "0",
   "goalQuantity": "1",
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