package org.starcoin.stcpricereporter.taskservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.starcoin.bean.TypeObj;
import org.starcoin.stcpricereporter.utils.OnChainTransactionUtils;
import org.starcoin.types.AccountAddress;
import org.starcoin.types.Ed25519PrivateKey;
import org.starcoin.types.TransactionPayload;
import org.starcoin.types.TypeTag;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.SignatureUtils;
import org.starcoin.utils.StarcoinClient;

import java.math.BigInteger;

@Component
public class OnChainManager {

    private final String senderAddressHex;// = "0x07fa08a855753f0ff7292fdcbe871216";

    private final String senderPrivateKeyHex;

    private final String starcoinFeedUrl;

    private final Integer starcoinChainId;

    private final String oracleScriptsAddressHex;// = "0x00000000000000000000000000000001";
    //"0x07fa08a855753f0ff7292fdcbe871216";

    private final StarcoinClient starcoinClient;

    public OnChainManager(@Value("${starcoin.stc-price-reporter.sender-address}") String senderAddressHex,
                          @Value("${starcoin.stc-price-reporter.sender-private-key}") String senderPrivateKeyHex,
                          @Value("${starcoin.feed-url}") String starcoinFeedUrl,
                          @Value("${starcoin.chain-id}") Integer starcoinChainId,
                          @Value("${starcoin.stc-price-reporter.oracle-scripts-address}") String oracleScriptsAddressHex) {
        this.senderAddressHex = senderAddressHex;
        this.senderPrivateKeyHex = senderPrivateKeyHex;
        this.starcoinFeedUrl = starcoinFeedUrl;
        this.starcoinChainId = starcoinChainId;
        this.oracleScriptsAddressHex = oracleScriptsAddressHex;
        this.starcoinClient = new StarcoinClient(this.starcoinFeedUrl, this.starcoinChainId);
    }

    public void initDataSourceOrUpdateOnChain(OffChainPriceCache<?> offChainPriceCache,
                                              PriceOracleType priceOracleType, BigInteger price) {
        if (offChainPriceCache.isFirstUpdate()) {
            if (!isDataSourceInitialize(priceOracleType)) {
                System.out.println("Init data-source first.");
                initDataSource(priceOracleType, price);
                updateOnChain(priceOracleType, price);//todo remove this!!!
            } else {
                updateOnChain(priceOracleType, price);
            }
        } else {
            updateOnChain(priceOracleType, price);
        }
    }

    public void updateOnChain(PriceOracleType priceOracleType, BigInteger price) {
        submitOracleTransaction(priceOracleType, oracleTypeTag ->
                OnChainTransactionUtils.encodePriceOracleUpdateScriptFunction(oracleTypeTag,
                        price, oracleScriptsAddressHex));
    }

    public boolean isDataSourceInitialize(PriceOracleType priceOracleType) {
        //todo check on chain!!!
        return false;
    }

    public void initDataSource(PriceOracleType priceOracleType, BigInteger price) {
        submitOracleTransaction(priceOracleType, oracleTypeTag ->
                OnChainTransactionUtils.encodePriceOracleInitDataSourceScriptFunction(oracleTypeTag,
                        price, oracleScriptsAddressHex));
    }

    private void submitOracleTransaction(PriceOracleType priceOracleType,
                                         java.util.function.Function<TypeTag, TransactionPayload> transactionPayloadProvider) {
        TypeObj oracleTypeObject = TypeObj.builder()
                .moduleName(priceOracleType.getModuleName())
                .moduleAddress(priceOracleType.getModuleAddress())
                .name(priceOracleType.getStructName()).build();
        TypeTag oracleTypeTag = oracleTypeObject.toTypeTag();

        final Ed25519PrivateKey senderPrivateKey = SignatureUtils.strToPrivateKey(senderPrivateKeyHex);
        final AccountAddress senderAddress = AccountAddressUtils.create(senderAddressHex);
        TransactionPayload transactionPayload = transactionPayloadProvider.apply(oracleTypeTag);
        this.starcoinClient.submitTransaction(senderAddress, senderPrivateKey, transactionPayload);
    }

}
