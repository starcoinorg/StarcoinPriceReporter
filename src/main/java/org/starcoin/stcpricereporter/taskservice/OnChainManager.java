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

import java.math.BigDecimal;

@Component
public class OnChainManager {

    public static final int PRICE_PRECISION = 8;

    public static final String STC_USDT_ORALCE_TYPE_MODULE_ADDRESS = "0x07fa08a855753f0ff7292fdcbe871216";

    private String senderAddressHex = "0x07fa08a855753f0ff7292fdcbe871216";

    @Value("${starcoin.stc-price-reporter.sender-private-key}")
    private String senderPrivateKeyHex;

    @Value("${starcoin.feed-url}")
    private String starcoinFeedUrl;

    @Value("${starcoin.chain-id}")
    private Integer starcoinChainId;

    public void reportOnChain(BigDecimal decimalPrice) {
        TypeObj oracleTypeObject = TypeObj.builder()
                .moduleName("STCUSDT")
                .moduleAddress(STC_USDT_ORALCE_TYPE_MODULE_ADDRESS)
                .name("STCUSDT").build();
        TypeTag oracleTypeTag = oracleTypeObject.toTypeTag();

        final Ed25519PrivateKey senderPrivateKey = SignatureUtils.strToPrivateKey(senderPrivateKeyHex);
        final AccountAddress senderAddress = AccountAddressUtils.create(senderAddressHex);
        final StarcoinClient starcoinClient = new StarcoinClient(starcoinFeedUrl, starcoinChainId);

        TransactionPayload transactionPayload = OnChainTransactionUtils.encodePriceOracleUpdateScriptFunction(oracleTypeTag,
                decimalPrice.multiply(BigDecimal.TEN.pow(PRICE_PRECISION)).toBigInteger());
        starcoinClient.submitTransaction(senderAddress, senderPrivateKey, transactionPayload);
    }

    public String getSenderAddressHex() {
        return senderAddressHex;
    }

    public void setSenderAddressHex(String senderAddressHex) {
        this.senderAddressHex = senderAddressHex;
    }

    public String getSenderPrivateKeyHex() {
        return senderPrivateKeyHex;
    }

    public void setSenderPrivateKeyHex(String senderPrivateKeyHex) {
        this.senderPrivateKeyHex = senderPrivateKeyHex;
    }

    public String getStarcoinFeedUrl() {
        return starcoinFeedUrl;
    }

    public void setStarcoinFeedUrl(String starcoinFeedUrl) {
        this.starcoinFeedUrl = starcoinFeedUrl;
    }

    public Integer getStarcoinChainId() {
        return starcoinChainId;
    }

    public void setStarcoinChainId(Integer starcoinChainId) {
        this.starcoinChainId = starcoinChainId;
    }
}
