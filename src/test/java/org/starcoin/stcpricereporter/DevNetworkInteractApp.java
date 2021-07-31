package org.starcoin.stcpricereporter;

import org.starcoin.stcpricereporter.taskservice.OnChainManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DevNetworkInteractApp {

    public static void main(String[] args) {
        // ------------------------------------
        // 先编译 move 代码：
        // move clean && move publish
        // ------------------------------------
        String shellPath = "/bin/sh";
        String starcoinCmd = "/Users/yangjiefeng/Documents/starcoinorg/starcoin/target/debug/starcoin -n dev -d alice console";
        String moveProjectDir = "/Users/yangjiefeng/Documents/wubuku/StcPriceReporter/src/test/move";
        if (args.length < 2) {
            throw new IllegalArgumentException("Please enter two account private keys");
        }
        String firstPrivateKey = args[0];
        String secondPrivateKey = args[1];
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    new String[]{shellPath, "-c", starcoinCmd}
                    //new String[] {starcoinCmd, "-n", "dev", "console"}
            );
            processBuilder.directory(new File(moveProjectDir));
            //processBuilder.inheritIO();
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (process == null) {
            throw new NullPointerException();
        }


        CommandLineInteractor commandLineInteractor = new CommandLineInteractor(process);
        commandLineInteractor.expect("Start console,", 10)
                 //导入账户，部署合约
//                .sendLine("account import -i " + firstPrivateKey)
//                .expect("\"ok\":", 10)
//                .sendLine("account import -i " + secondPrivateKey)
//                .expect("\"ok\":", 10)
//                .sendLine("account default 0x07fa08a855753f0ff7292fdcbe871216")
//                .expect("\"ok\":", 10)
                .sendLine("account unlock 0x07fa08a855753f0ff7292fdcbe871216")
                .expect("\"ok\":", 10)
                .sendLine("account unlock 0xff2794187d72cc3a9240198ca98ac7b6")
                .expect("\"ok\":", 10)
                .sendLine("dev get-coin 0x07fa08a855753f0ff7292fdcbe871216")
                .expect("\"ok\":", 10)
                .sendLine("dev get-coin 0xff2794187d72cc3a9240198ca98ac7b6")
                .expect("\"ok\":", 10)
                .sendLine("dev deploy storage/0x07fa08a855753f0ff7292fdcbe871216/modules/Oracle.mv -b")
                .expect("\"ok\":", 10)
                .sendLine("dev deploy storage/0x07fa08a855753f0ff7292fdcbe871216/modules/PriceOracle.mv -b")
                .expect("\"ok\":", 10)
                .sendLine("dev deploy storage/0x07fa08a855753f0ff7292fdcbe871216/modules/PriceOracleAggregator.mv -b")
                .expect("\"ok\":", 10)
                .sendLine("dev deploy storage/0x07fa08a855753f0ff7292fdcbe871216/modules/PriceOracleScripts.mv -b")
                .expect("\"ok\":", 10)

                // /////////////////////////////////////////////
                .sendLine("dev deploy storage/0x07fa08a855753f0ff7292fdcbe871216/modules/STCUSDT.mv -b")
                .expect("\"ok\":", 10)

                .sendLine("account execute-function -s 0x07fa08a855753f0ff7292fdcbe871216 " +
                        "--function 0x07fa08a855753f0ff7292fdcbe871216::PriceOracleScripts::register_oracle " +
                        "-t 0x07fa08a855753f0ff7292fdcbe871216::STCUSDT::STCUSDT " +
                        String.format("--arg %1$su8 ", OnChainManager.PRICE_PRECISION) +
                        "-b")
                .expect("\"ok\":", 10)

                .sendLine("account execute-function -s 0x07fa08a855753f0ff7292fdcbe871216 " +
                        "--function 0x07fa08a855753f0ff7292fdcbe871216::PriceOracleScripts::init_data_source " +
                        "-t 0x07fa08a855753f0ff7292fdcbe871216::STCUSDT::STCUSDT " +
                        "--arg 10000000u128 " +
                        "-b")
                .expect("\"ok\":", 10)

                .sendLine("account execute-function -s 0x07fa08a855753f0ff7292fdcbe871216 " +
                        "--function 0x07fa08a855753f0ff7292fdcbe871216::PriceOracleScripts::update " +
                        "-t 0x07fa08a855753f0ff7292fdcbe871216::STCUSDT::STCUSDT " +
                        "--arg 10000000u128 " +
                        "-b")
                .expect("\"ok\":", 10)
                // /////////////////////////////////////////////
        ;


        // /////////////////////////////////////////////
        String deployFilePath = "src/test/resources/ConsoleDeployPairRegisterOracleInitDataSource.txt";
        List<String> lines = readAllLines(deployFilePath);
        lines.stream().filter(c -> !c.isEmpty()).forEach( line -> {
            //System.out.println(line);
            commandLineInteractor.sendLine(line)
                    .expect("\"ok\":", 10)
            ;
        });
        // if (true) return;
        // /////////////////////////////////////////////
    }

    private static List<String> readAllLines(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
