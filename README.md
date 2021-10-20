# Starcoin Token Pair Price Reporter(Price Oracle Off-Chain Application)

本应用是 Starcoin 币对价格 Oracle 的链下服务组件。

应用可以提供 STC 兑美元及其他若干币对（Token Pair）的价格信息。

## 链下 RESTful API

本应用在链下通过 RESTful API 对外提供币对价格信息。

以下示例 URL 以 barnard 测试网络为例。需要请求主网接口时，请把 URL 路径中的 `barnard` 换为 `main`。

### 获取可提供价格的币对列表

获取当前可以提供价格的币对的列表，HTTP GET 请求的 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/pricePairs
```

### 获取币对价格 Feed

获取币对价格，HTTP GET 请求的 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/priceFeeds/{pairId}
```

路径参数（示例代码中以花括号包裹的是参数，按实际需要填写）：

* pairId：币对 Id。支持的的币对 Id 可见「获取可提供价格的币对列表」接口。

### 获取币对价格 Feed 列表

获取币对价格 feed，HTTP GET 请求 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/priceFeeds
```

### 其他接口

以下接口定义未稳定，仅供合作伙伴优先试用。

#### 获取最接近某个时间点的币对价格

请求 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/getProximatePriceRound?pairId=STCUSD&timestamp=1632729164168
```

查询参数：

* pairId：币对 Id。
* timestamp：时间点（Epoch 毫秒数）。

#### 获取币对 24 小时涨跌幅

请求 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/priceGrowths?p=STCUSD&p=ETH_USD&p=BTC_USD
```

查询参数：

* p：币对 Id。可查询多个币对涨跌信息。

#### 获取一个时间段内币对价格的均值信息

请求 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/getPriceAverages?p=BTC_USD&p=STCUSD&after=1632720000000&before=1632729164168
```

查询参数：

* p：币对 Id。可查询多个币对涨跌信息。
* after：开始时间（Epoch 毫秒数）。
* before：接受时间。

#### 获取 ETH 兑 STC 的汇率

请求 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/exchangeRates/ETH_STC
```

#### 获取（ETH）WEI 兑换 Nano STC 的汇率

请求 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/exchangeRates/WEI_NANOSTC
```

#### 获取某个 Token 兑美元的价格信息

请求 URL 示例：

```url
https://price-api.starcoin.org/barnard/v1/toUsdPriceFeeds?t=STC&t=BTC
```

查询参数：

* t：Token Id。

注意：本方法仅简单地通过拼接 Token Id 以及 `USD` 或 `_USD` 得到 Pair Id，然后以该 pairId 查询币对的价格信息。

## 链上接口（Move 合约）

本应用会将币对价格写入 Starcoin 链上合约。

Starcoin 链上存在用于读取币对（Token Pair）价格信息的 Price Oracle 模块。 可以使用此方法读取已经上链的币对价格：

```text
0x00000000000000000000000000000001::PriceOracle::read
```

Starcoin Java SDK 对该方法进行了封装。

读取价格信息需传入币对价格的 Oracle Type 作为 TypeArg 参数。

币对价格的 Oracle Type 信息（地址、模块、名称）可见「获取可提供价格的币对列表」接口。

### 当前 Barnard 网络的 Oarlce Types

STC / USD 币对的 Oarcle Type 为：

```
0x00000000000000000000000000000001::STCUSDOracle::STCUSD
```

其他 Token Pair 的 Oracle Type 统一格式为：

```
0x07fa08a855753f0ff7292fdcbe871216::{pairId}::{pairId}
```

上面的占位符 `{pairId}` 指的是可获取价格的币对 Id（即 `BTC_USD`、`ETH_USD` 等）。

### 当前主网上的 Oracle Types

STC / USD 币对的 Oarcle Type 为：

```
0x00000000000000000000000000000001::STCUSDOracle::STCUSD
```

其他 Token Pair 的 Oracle Type 统一格式为：

```
0x82e35b34096f32c42061717c06e44a59::{pairId}::{pairId}
```

## 代码说明

### 价格来源

本应用将三个交易所的 STC 价格（进行聚合后），以及以太坊链上（由 Chainlink 提供的）代币对价格上报到 Starcoin 网络上的 Oracle 合约。

Chainlink Price Feeds 合约地址见：https://docs.chain.link/docs/reference-contracts/

### 部署及运行应用

目前应用使用 CSV 资源文件保存以太坊上的 Chainlink Price Feeds 的信息（在 `src/main/resources/EthereumPriceFeeds-Mainnet.csv`）。

可以编辑 CSV 文件，然后从 CSV 文件生成 Starcoin 需要的代币对类型（Oracle Type）的 Move 代码，以及可以通过 Starcoin Console 部署 Move 代码、注册价格 Oracle 的脚本。

所生成的 Starcoin 链上的代币对类型的模块名以及结构名从 CSV 中的 Pair 列生成，生成逻辑在 `PriceFeedRecord.getMoveTokenPairName()`。

然后通过 Starcoin Console 执行脚本。

运行应用。

### 应用依赖的链上价格 Oracle 接口

应用依赖的链上的价格 Oracle 脚本接口的定义可以通过 JSON RPC 获取，示例：

```shell
curl --location --request POST 'https://barnard-seed.starcoin.org' \
--header 'Content-Type: application/json' \
--data-raw '{
 "id":101,
 "jsonrpc":"2.0",
 "method":"contract.resolve_module",
 "params":["0x1::PriceOracleScripts"]
}'
```

获取主网接口信息时请把上面的 `barnard` 换成 `main`。

