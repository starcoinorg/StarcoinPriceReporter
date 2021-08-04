# StcPriceReporter

将三个交易所的 STC 价格以及以太坊上（Chainlink 提供的）代币对价格上报到 Starcoin 网络上的 Oracle 合约。

## 使用说明

目前使用 CSV 文件保存以太坊上的 Chainlink Price Feeds 的信息（如 `src/main/resources/EthereumPriceFeeds-Mainnet.csv`）。

可以编辑 CSV 文件，然后从 CSV 文件生成 Starcoin 需要的代币对类型的 Move 代码以及通过 Starcoin Console 部署 Move 代码、注册价格 Oracle 的脚本。

在 Starcoin 上的代币对类型的模块名以及结构名从 CSV 中的 Pair 列生成，生成逻辑在 `PriceFeedRecord.getMoveTokenPairName()`。

然后通过 Starcoin Console 执行脚本，然后运行应用。
