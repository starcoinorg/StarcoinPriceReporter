package org.starcoin.stcpricereporter;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.starcoin.stcpricereporter.chainlink.PriceFeedRecord;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.starcoin.stcpricereporter.chainlink.utils.CsvUtils.readCsvPriceFeedRecords;

public class GenerateMoveTokenPairsAndConsoleDeploymentFile {


    public static void main(String[] args) {
        String csvFilePath = "src/main/resources/EthereumPriceFeeds-Mainnet.csv";
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

        Handlebars handlebars = new Handlebars();

        Template template;
        try {
            template = handlebars.compile("MoveTokenPairsTemplate");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Map<String, Object> templateContext = new HashMap<>();
        templateContext.put("feeds", priceFeedRecords);

        String moveCode;
        try {
            moveCode = template.apply(templateContext);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        String moveCodeFilePath = "src/test/move/modules/TokenPairs.move";
        writeTextFile(moveCodeFilePath, moveCode);

        try {
            template = handlebars.compile("ConsoleDeployPairRegisterOracle");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        String deployFileContent;
        try {
            deployFileContent = template.apply(templateContext);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        String deployFilePath = "src/test/resources/ConsoleDeployPairRegisterOracle.txt";
        writeTextFile(deployFilePath, deployFileContent);
    }

    private static void writeTextFile(String filePath, String content) {
        try (Writer fileWriter = new FileWriter(filePath)) {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
