package org.starcoin.stcpricereporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.starcoin.stcpricereporter.taskservice.PriceOracleType;
import org.starcoin.stcpricereporter.utils.JsonRpcUtils;
import org.starcoin.stcpricereporter.utils.OnChainTransactionUtils;
import org.starcoin.types.AccountAddress;
import org.starcoin.types.Ed25519PrivateKey;
import org.starcoin.types.TransactionPayload;
import org.starcoin.types.TypeTag;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.SignatureUtils;
import org.starcoin.utils.StarcoinClient;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.starcoin.stcpricereporter.utils.OnChainTransactionUtils.toTypeTag;


@Component
public class OnChainManager {
    private static final String FUNCTION_ID_IS_DATA_SOURCE_INITIALIZED = "0x00000000000000000000000000000001::PriceOracle::is_data_source_initialized";
    private static final String FUNCTION_ID_PRICE_ORACLE_READ = "0x00000000000000000000000000000001::PriceOracle::read";
    private final Logger LOG = LoggerFactory.getLogger(OnChainManager.class);
    private final String senderAddressHex;// = "0x07fa08a855753f0ff7292fdcbe871216";
    private final String senderPrivateKeyHex;
    private final String starcoinRpcUrl;
    private final Integer starcoinChainId;
    private final String oracleScriptsAddressHex;// = "0x00000000000000000000000000000001";
    private final StarcoinClient starcoinClient;
    //"0x07fa08a855753f0ff7292fdcbe871216";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PriceFeedService priceFeedService;

    @Autowired
    private StarcoinAccountService starcoinAccountService;

    public OnChainManager(@Value("${starcoin.sender-address}") String senderAddressHex,
                          @Value("${starcoin.sender-private-key}") String senderPrivateKeyHex,
                          @Value("${starcoin.rpc-url}") String starcoinRpcUrl,
                          @Value("${starcoin.chain-id}") Integer starcoinChainId,
                          @Value("${starcoin.oracle-scripts-address}") String oracleScriptsAddressHex) {
        this.senderAddressHex = senderAddressHex;
        this.senderPrivateKeyHex = senderPrivateKeyHex;
        this.starcoinRpcUrl = starcoinRpcUrl;
        this.starcoinChainId = starcoinChainId;
        this.oracleScriptsAddressHex = oracleScriptsAddressHex;
        this.starcoinClient = new StarcoinClient(this.starcoinRpcUrl, this.starcoinChainId);
    }

    private static boolean indicatesSuccess(Map<String, Object> responseObj) {
        return !responseObj.containsKey("error");// && (responseObj.containsKey("result") && null != responseObj.getJSONObject("result"));
    }

    public void initDataSourceOrUpdateOnChain(PriceOracleType priceOracleType, BigInteger price) {
//        try {
//            Thread.sleep(Long.MAX_VALUE);//sleep forever now...
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String pairId = priceOracleType.getStructName(); // Pair Id. in database!
        // /////////////////////////////////////////////
        // try update in database
        if (!tryUpdatePriceInDatabase(pairId, price)) return;
        // ////////////////////////////////////////////
        String transactionHash;
        //if (offChainPriceCache.isFirstUpdate()) {
        if (!isDataSourceInitialized(priceOracleType)) {
            LOG.debug("Datasource is NOT initialized. Init data-source first.");
            transactionHash = doInitDataSource(priceOracleType, price); //updateOnChain(priceOracleType, price);
        } else {
            transactionHash = doUpdateOnChain(priceOracleType, price);
        }
        //} else {
        //    updateOnChain(priceOracleType, price);
        //}
        try {
            priceFeedService.setOnChainStatusSubmitted(pairId, transactionHash);
        } catch (RuntimeException runtimeException) {
            LOG.info("Update on-chain status in database caught runtime error. PairId: " + pairId, runtimeException);
        }
    }

    /**
     * Try update price in database.
     *
     * @param pairId
     * @param price
     * @return If price updated, return ture, else return false. If unexpected runtime error caught, return TRUE!
     */
    private boolean tryUpdatePriceInDatabase(String pairId, BigInteger price) {
        try {
            if (!priceFeedService.tryUpdatePrice(pairId, price)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Try update database failed. Maybe another process have updated it." + pairId + ": " + price);
                }
                return false;
            } else {
                return true;
            }
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException optimisticLockingFailureException) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Try update database failed, cause of ObjectOptimisticLockingFailureException." + pairId + ": " + price);
            }
            return false;
        } catch (RuntimeException exception) {
            LOG.info("Update price in database caught runtime error. " + pairId + ": " + price, exception);
            return true;// continue update on-chain.
        }
    }

    /**
     * @throws RuntimeException if submit transaction error, throw runtime exception.
     */
    protected String doUpdateOnChain(PriceOracleType priceOracleType, BigInteger price) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Update on-chain price of oracle: " + priceOracleType.getStructName() + ", price: " + price);
        }
        return submitOracleTransaction(priceOracleType, oracleTypeTag ->
                OnChainTransactionUtils.buildPriceOracleUpdateTransaction(oracleTypeTag,
                        price, oracleScriptsAddressHex));
    }

    public boolean isDataSourceInitialized(PriceOracleType priceOracleType) {
        Object resultObj = contractCallV2(
                FUNCTION_ID_IS_DATA_SOURCE_INITIALIZED,
                Collections.singletonList(getTypeArgString(priceOracleType)),
                Collections.singletonList(senderAddressHex));
        return Boolean.parseBoolean(((List<Object>) resultObj).get(0).toString());
    }

    public BigInteger priceOracleRead(PriceOracleType priceOracleType) {
        Object resultObj = contractCallV2(
                FUNCTION_ID_PRICE_ORACLE_READ,
                Collections.singletonList(getTypeArgString(priceOracleType)),
                Collections.singletonList(senderAddressHex));
        return new BigInteger(((List<Object>) resultObj).get(0).toString());
    }

    private String getTypeArgString(PriceOracleType priceOracleType) {
        return priceOracleType.getModuleAddress()
                + "::" + priceOracleType.getModuleName()
                + "::" + priceOracleType.getStructName();
    }

    /**
     * curl --location --request POST 'http://localhost:9850' \
     * --header 'Content-Type: application/json' \
     * --data-raw '{
     * "id":101,
     * "jsonrpc":"2.0",
     * "method":"contract.call_v2",
     * "params":[ {"function_id":"0x1::PriceOracle::is_data_source_initialized","type_args":["0x1::STCUSDOracle::STCUSD"],"args":["0x7beb045f2dea2f7fe50ede88c3e19a72"]}]
     * }'
     */
    private Object contractCallV2(String functionId, List<String> typeArgs, List<Object> args) {
        String method = "contract.call_v2";
        Map<String, Object> singleParamMap = new HashMap<>();
        singleParamMap.put("function_id", functionId);
        singleParamMap.put("type_args", typeArgs);
        singleParamMap.put("args", args);
        List<Object> params = Collections.singletonList(singleParamMap);
        Object resultObj = JsonRpcUtils.invoke(restTemplate, starcoinRpcUrl, method, params);
        if (LOG.isDebugEnabled()) {
            LOG.debug("contract.call_v2 {}, typeArgs: {}, args: {}, return result: {}", functionId, typeArgs, args, resultObj);
        }
        return resultObj;
    }

    /**
     * curl --location --request POST 'https://barnard-seed.starcoin.org' \
     * --header 'Content-Type: application/json' \
     * --data-raw '{
     * "id":101,
     * "jsonrpc":"2.0",
     * "method":"contract.resolve_module",
     * "params":["0x07fa08a855753f0ff7292fdcbe871216::BTC_USD"]
     * }'
     */
    public Object resolveModule(String moduleTag) {
        String method = "contract.resolve_module";
        List<Object> params = Collections.singletonList(moduleTag);
        Object resultObj = JsonRpcUtils.invoke(restTemplate, starcoinRpcUrl, method, params);
        if (LOG.isDebugEnabled()) {
            LOG.debug("contract.resolve_module {}, return result: {}", moduleTag, resultObj);
        }
        return resultObj;
    }

    public Map<String, Object> getTransactionInfo(String transactionHash) {
        String method = "chain.get_transaction_info";
        List<Object> params = Collections.singletonList(transactionHash);
        Object resultObj = JsonRpcUtils.invoke(restTemplate, starcoinRpcUrl, method, params);
        if (LOG.isDebugEnabled()) {
            LOG.debug("chain.get_transaction_info {}, return result: {}", transactionHash, resultObj);
        }
        return (Map<String, Object>) resultObj;
    }


    public Map<String, Object> getTransaction(String transactionHash) {
        String method = "chain.get_transaction";
        List<Object> params = Collections.singletonList(transactionHash);
        Object resultObj = JsonRpcUtils.invoke(restTemplate, starcoinRpcUrl, method, params);
        if (LOG.isDebugEnabled()) {
            LOG.debug("chain.get_transaction {}, return result: {}", transactionHash, resultObj);
        }
        return (Map<String, Object>) resultObj;
    }

    public void registerOracle(PriceOracleType priceOracleType, Byte precision) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Register price oracle: " + priceOracleType.getStructName() + ", precision: " + precision);
        }
        submitOracleTransaction(priceOracleType, oracleTypeTag ->
                OnChainTransactionUtils.buildPriceOracleRegisterTransaction(oracleTypeTag, precision, oracleScriptsAddressHex));
    }

    /**
     * Init oracle datasource on-chain.
     *
     * @param priceOracleType
     * @param price
     * @throws RuntimeException if submit transaction error, throw runtime exception.
     */
    protected String doInitDataSource(PriceOracleType priceOracleType, BigInteger price) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Init datasource of price oracle: " + priceOracleType.getStructName() + ", price: " + price);
        }
        return submitOracleTransaction(priceOracleType, oracleTypeTag ->
                OnChainTransactionUtils.buildPriceOracleInitDataSourceTransaction(oracleTypeTag,
                        price, oracleScriptsAddressHex));
    }

    private String submitOracleTransaction(PriceOracleType priceOracleType,
                                           java.util.function.Function<TypeTag, TransactionPayload> transactionPayloadProvider) {
        TypeTag oracleTypeTag = toTypeTag(priceOracleType);
        BigInteger seqNumber = getSenderSequenceNumberFromDatabase();
        final Ed25519PrivateKey senderPrivateKey = SignatureUtils.strToPrivateKey(senderPrivateKeyHex);
        final AccountAddress senderAddress = AccountAddressUtils.create(senderAddressHex);
        TransactionPayload transactionPayload = transactionPayloadProvider.apply(oracleTypeTag);
        String respBody;
        if (seqNumber != null) {
            respBody = this.starcoinClient.submitTransaction(senderAddress, seqNumber.longValue(), senderPrivateKey, transactionPayload);
        } else {
            respBody = this.starcoinClient.submitTransaction(senderAddress, senderPrivateKey, transactionPayload);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Submit oracle transaction about: " + priceOracleType.getStructName()
                    + ", account sequence number: " + (seqNumber == null ? "<NULL>" : seqNumber)
                    + ", response: " + respBody);
        }
        try {
            Map<String, Object> responseMap = new ObjectMapper().readValue(respBody, new TypeReference<Map<String, Object>>() {
            });
            if (!indicatesSuccess(responseMap)) {
                LOG.error("Submit oracle transaction about {} caught error. {}", priceOracleType.getStructName(), respBody);
                throw new RuntimeException("Submit transaction error. " + respBody);
            }
            return (String) responseMap.get("result");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get Sender Sequence Number From Database.
     *
     * @return Sender Sequence Number. If unexpected runtime error caught, return null.
     * @throws org.springframework.orm.ObjectOptimisticLockingFailureException Throw if database update conflicted.
     */
    private BigInteger getSenderSequenceNumberFromDatabase() {
        BigInteger seqNumber;
        try {
            seqNumber = starcoinAccountService.getSequenceNumberAndIncrease(this.getSenderAddress());
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException optimisticLockingFailureException) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get account sequence number from database error, cause of ObjectOptimisticLockingFailureException.");
            }
            throw optimisticLockingFailureException;
        } catch (RuntimeException e) {
            seqNumber = null;
            LOG.error("Get account sequence number from database error.", e);
        }
        return seqNumber;
    }

    public BigInteger getSenderSequenceNumber(String address) {
        long seqNumber = this.starcoinClient.getAccountSequenceNumber(AccountAddressUtils.create(address));
        return BigInteger.valueOf(seqNumber);
    }

    public String getSenderAddress() {
        return this.senderAddressHex;
    }

    public void resetByOnChainSequenceNumber(String address) {
        BigInteger seqNumber = getSenderSequenceNumber(address);
        starcoinAccountService.resetSequenceNumber(address, seqNumber);
    }

    public void createSenderAccountIfNoExists() {
        String address = getSenderAddress();
        if (starcoinAccountService.getStarcoinAccountOrElseNull(address) == null) {
            this.resetByOnChainSequenceNumber(address);
        }
    }

}
