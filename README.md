# Starcoin Price Reporter

将三个交易所的 STC 价格以及以太坊上（Chainlink 提供的）代币对价格上报到 Starcoin 网络上的 Oracle 合约。

Chainlink Price Feeds 合约地址见：https://docs.chain.link/docs/reference-contracts/

## Starcoin 链上的 Oracle 模块 

链上 Token Pair 价格可以使用此方法读取：

```
0x00000000000000000000000000000001::PriceOracle::read
```

读取时候需要传入 Oracle Type 作为 TypeArg 参数。

### Barnard 网络的 Oarlce Type

STC / USD 的 Oarcle Type 为：

```
0x00000000000000000000000000000001::STCUSDOracle::STCUSD
```

其他 Token Pair 的 Oracle Type 为：

```
0x07fa08a855753f0ff7292fdcbe871216::{pairId}::{pairId}
```

`{pairId}` 为 BTC_USD、ETH_USD 等。

## 运行应用

目前应用使用 CSV 文件保存以太坊上的 Chainlink Price Feeds 的信息（在 `src/main/resources/EthereumPriceFeeds-Mainnet.csv`）。

可以编辑 CSV 文件，然后从 CSV 文件生成 Starcoin 需要的代币对类型（Oracle Type）的 Move 代码以及通过 Starcoin Console 部署 Move 代码、注册价格 Oracle 的脚本。

生成的 Starcoin 链上的代币对类型的模块名以及结构名从 CSV 中的 Pair 列生成，生成逻辑在 `PriceFeedRecord.getMoveTokenPairName()`。

然后通过 Starcoin Console 执行脚本。运行应用。

应用调用链上的接口信息可以通过 JSON RPC 获取：

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
