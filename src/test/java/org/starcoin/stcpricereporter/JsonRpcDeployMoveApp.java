package org.starcoin.stcpricereporter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.starcoin.stcpricereporter.chainlink.PriceFeedRecord;
import org.starcoin.stcpricereporter.service.OnChainManager;
import org.starcoin.stcpricereporter.utils.JsonRpcUtils;
import org.starcoin.stcpricereporter.utils.OnChainTransactionUtils;
import org.starcoin.stcpricereporter.vo.PriceOracleType;
import org.starcoin.types.AccountAddress;
import org.starcoin.types.Ed25519PrivateKey;
import org.starcoin.types.TransactionPayload;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.ChainInfo;
import org.starcoin.utils.SignatureUtils;
import org.starcoin.utils.StarcoinClient;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.starcoin.stcpricereporter.chainlink.utils.CsvUtils.readCsvPriceFeedRecords;

@SpringBootTest
public class JsonRpcDeployMoveApp {

    // 目前只是用来读取链上的状态
    @Autowired
    private OnChainManager onChainManager;

    @Test
    public void testDeploy() {
        String csvFilePath = "src/main/resources/EthereumPriceFeeds-Mainnet.csv";
        String mvFileBasePath = "/Users/yangjiefeng/Documents/wubuku/StcPriceReporter/src/test/move/storage/0x82e35b34096f32c42061717c06e44a59/modules";

        Reader in;
        try {
            in = new FileReader(csvFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        List<PriceFeedRecord> priceFeedRecords = readCsvPriceFeedRecords(in)
                .stream().filter(f -> f.getEnabled() != null && f.getEnabled()).collect(Collectors.toList());
        System.out.println(priceFeedRecords);

        //ChainInfo chainInfo = ChainInfo.DEFAULT_BARNARD;//使用 barnard 网络！
        ChainInfo chainInfo = new ChainInfo(
                System.getenv("STARCOIN_NETWORK"),
                System.getenv("STARCOIN_RPC_URL"),
                Integer.parseInt(System.getenv("STARCOIN_CHAIN_ID"))
        );
        StarcoinClient starcoinClient = new StarcoinClient(chainInfo);
//        if (args.length < 1) {
//            throw new IllegalArgumentException("Please enter account private key");
//        }
        String firstPrivateKeyArg = System.getenv("STARCOIN_SENDER_PRIVATE_KEY");//args[0];
        if (firstPrivateKeyArg == null || firstPrivateKeyArg.isEmpty()) {
            throw new RuntimeException("Private key is null!");
        }

        int i = 0;
        for (PriceFeedRecord p : priceFeedRecords) {
//            if ("BTC_USD".equalsIgnoreCase(p.getMoveTokenPairName())) {
//                continue; // ignore!!!
//            }
            Path mvFilePath = Paths.get(mvFileBasePath, p.getMoveTokenPairName() + ".mv");
            if (!Files.exists(mvFilePath)) {
                throw new RuntimeException("File not exists: " + mvFilePath);
            }
            AccountAddress sender = AccountAddressUtils.create("0x82e35b34096f32c42061717c06e44a59");
            Ed25519PrivateKey privateKey = SignatureUtils.strToPrivateKey(firstPrivateKeyArg);
            boolean isModuleResolved;
            try {
                Object resolvedModuleInfo = onChainManager.resolveModule("0x82e35b34096f32c42061717c06e44a59" + "::" + p.getMoveTokenPairName());
                isModuleResolved = resolvedModuleInfo != null;
                System.out.println("Module is resolved: " + p.getMoveTokenPairName());
            } catch (RuntimeException e) {
                isModuleResolved = false;
            }
            if (!isModuleResolved) {
                System.out.println("Deploying mv: " + mvFilePath);
                //ScriptFunctionObj initSF = ScriptFunctionObj.builder().moduleAddress("").moduleName("")...
                String rspBody = starcoinClient.deployContractPackage(sender,
                        privateKey,
                        mvFilePath.toString(), null);
                System.out.println(rspBody);
                String transactionHash = JsonRpcUtils.getStringResultFromResponseBody(rspBody);
                Map<String, Object> transactionInfo = waitTransactionInfo(p, transactionHash);
                if (transactionInfo == null) {
                    System.out.println("deployContractPackage NOT return transactionInfo: " + p.getMoveTokenPairName());
                    continue;
                } else {
                    isModuleResolved = true;
                }
            }

            if (isModuleResolved) {
                System.out.println("Register oracle for: " + p.getMoveTokenPairName());
                PriceOracleType priceOracleType = new PriceOracleType("0x82e35b34096f32c42061717c06e44a59", p.getMoveTokenPairName(), p.getMoveTokenPairName());
                TransactionPayload transactionPayload = OnChainTransactionUtils.buildPriceOracleRegisterTransaction(
                        OnChainTransactionUtils.toTypeTag(priceOracleType),
                        p.getDecimals().byteValue(),
                        "0x00000000000000000000000000000001");
                String regRspBody = starcoinClient.submitTransaction(sender, privateKey, transactionPayload);
                System.out.println(regRspBody);
                try {
                    String transactionHash = JsonRpcUtils.getStringResultFromResponseBody(regRspBody);
                    Map<String, Object> transactionInfo = waitTransactionInfo(p, transactionHash);
                    if (transactionInfo == null) {
                        System.out.println("Register oracle NOT return transactionInfo: " + p.getMoveTokenPairName());
                        continue;
                    } else {
                        //ok
                        continue;
                    }
                } catch (RuntimeException runtimeException) {
                    System.out.println("Register " + p.getMoveTokenPairName() + " oracle error." + runtimeException.getMessage());
                    continue;
                }
            }
            // ------------------------------------
            i++;
        }

    }

    private Map<String, Object> waitTransactionInfo(PriceFeedRecord p, String transactionHash) {
        int sleepCount = 0;
        while (true) {
            Map<String, Object> transactionInfo = onChainManager.getTransactionInfo(transactionHash);
            if (transactionInfo == null) {
                if (sleepCount >= 4) {
                    System.out.println("SleepCount >= 4, transaction maybe failed: " + p.getMoveTokenPairName());
                    break;
                }
                try {
                    Thread.sleep(30000);
                    sleepCount++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Transaction status: " + transactionInfo.get("status"));
                return transactionInfo;//break;
            }
        }
        return null;
    }


}
