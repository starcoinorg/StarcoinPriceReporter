package org.starcoin.stcpricereporter;

import org.starcoin.stcpricereporter.chainlink.PriceFeedRecord;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.ChainInfo;
import org.starcoin.utils.SignatureUtils;
import org.starcoin.utils.StarcoinClient;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.starcoin.stcpricereporter.chainlink.utils.CsvUtils.readCsvPriceFeedRecords;

public class JsonRpcDeployMoveApp {

    public static void main(String[] args) {
        String csvFilePath = "src/main/resources/EthereumPriceFeeds-Mainnet.csv";
        String mvFileBasePath = "/Users/yangjiefeng/Documents/wubuku/StcPriceReporter/src/test/move/storage/0x07fa08a855753f0ff7292fdcbe871216/modules";

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

        StarcoinClient starcoinClient = new StarcoinClient(ChainInfo.DEFAULT_BARNARD);
        if (args.length < 1) {
            throw new IllegalArgumentException("Please enter account private key");
        }
        String firstPrivateKey = args[0];

        for (PriceFeedRecord p : priceFeedRecords) {
            Path mvFilePath = Paths.get(mvFileBasePath, p.getMoveTokenPairName() + ".mv");
            String rspBody = starcoinClient.deployContractPackage(AccountAddressUtils.create("0x07fa08a855753f0ff7292fdcbe871216"),
                    SignatureUtils.strToPrivateKey(firstPrivateKey),
                    mvFilePath.toString(), null);
            System.out.println(rspBody);
        }

    }
}
