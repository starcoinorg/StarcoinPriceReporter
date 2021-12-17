/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.starcoin.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.novi.serde.Bytes;
import lombok.SneakyThrows;
import okhttp3.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.starcoin.bean.ResourceObj;
import org.starcoin.bean.ScriptFunctionObj;
import org.starcoin.bean.TypeObj;
import org.starcoin.stdlib.Helpers;
import org.starcoin.types.*;
import org.starcoin.types.TransactionPayload.ScriptFunction;
import org.starcoin.types.Module;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

// todo remove this file if maven central repository updated...
public class StarcoinClient {
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse(
            "application/json; charset=utf-8");
    private static final long DEFAULT_MAX_GAS_AMOUNT = 10000000L;
    private static final long DEFAULT_TRANSACTION_EXPIRATION_SECONDS = 2 * 60 * 60;
    private static final String GAS_TOKEN_CODE = "0x1::STC::STC";

    private static final String FUNCTION_ID_PRICE_ORACLE_READ = "0x00000000000000000000000000000001::PriceOracle::read";
    private final String baseUrl;
    private final int chaindId;
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    public StarcoinClient(String url, int chainId) {
        this.baseUrl = url;
        this.chaindId = chainId;
    }

    public StarcoinClient(ChainInfo chainInfo) {

        this.baseUrl = chainInfo.getUrl();
        this.chaindId = chainInfo.getChainId();
    }

    private static boolean indicatesSuccess(Map<String, Object> responseMap) {
        return !responseMap.containsKey("error");//error == null;
    }

    @SneakyThrows
    public String call(String method, List<Object> params) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("jsonrpc", "2.0");
        jsonBody.put("method", method);
        jsonBody.put("id", UUID.randomUUID().toString());
        jsonBody.put("params", params);
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON_MEDIA_TYPE);
        Request request = new Request.Builder().post(body).url(this.baseUrl).build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body().string();
    }

    public String transfer(AccountAddress sender, Ed25519PrivateKey privateKey, AccountAddress to,
                           TypeObj typeObj, BigInteger amount) {
        TransactionPayload payload = buildTransferPayload(to, typeObj, amount);
        return submitTransaction(sender, privateKey, payload);
    }

    private TransactionPayload buildTransferPayload(AccountAddress toAddress, TypeObj typeObj,
                                                    BigInteger amount) {
        return Helpers.encode_peer_to_peer_v2_script_function(typeObj.toTypeTag(),
                toAddress, amount);
    }

    @SneakyThrows
    public String submitHexTransaction(Ed25519PrivateKey privateKey,
                                       RawUserTransaction rawUserTransaction) {
        SignedUserTransaction signedUserTransaction = SignatureUtils.signTxn(privateKey,
                rawUserTransaction);
        List<Object> params = Lists.newArrayList(Hex.encode(signedUserTransaction.bcsSerialize()));
        return call("txpool.submit_hex_transaction", params);
    }

    public String callScriptFunction(AccountAddress sender, Ed25519PrivateKey privateKey,
                                     ScriptFunctionObj scriptFunctionObj) {
        ScriptFunction scriptFunction = new ScriptFunction(scriptFunctionObj.toScriptFunction());
        RawUserTransaction rawUserTransaction = buildRawUserTransaction(sender, scriptFunction);
        return submitHexTransaction(privateKey, rawUserTransaction);
    }

    @SneakyThrows
    //  @TODO 链上改了返回结构以后要修改，命名好像改为 getAccountResource 更好一些？
    public AccountResource getAccountSequence(AccountAddress sender) {
        String path = AccountAddressUtils.hex(
                sender) + "/1/0x00000000000000000000000000000001::Account::Account";
        String rst = call("state.get", Lists.newArrayList(path));
        JSONObject jsonObject = JSON.parseObject(rst);
        List<Byte> result = jsonObject
                .getJSONArray("result")
                .toJavaList(Byte.class);
        Byte[] bytes = result.toArray(new Byte[0]);
        return AccountResource.bcsDeserialize(ArrayUtils.toPrimitive(bytes));
    }

    public long getAccountSequenceNumber(AccountAddress sender) {
        AccountResource accountResource = getAccountSequence(sender);
        return accountResource.sequence_number;
    }

    @SneakyThrows
    private RawUserTransaction buildRawUserTransaction(AccountAddress sender,
                                                       TransactionPayload payload) {
        long seqNumber = getAccountSequenceNumber(sender);
        return buildRawUserTransaction(sender, seqNumber, payload);
    }

    private RawUserTransaction buildRawUserTransaction(AccountAddress sender, long seqNumber,
                                                       TransactionPayload payload) {
        ChainId chainId = new ChainId((byte) chaindId);
        return new RawUserTransaction(sender, seqNumber, payload, DEFAULT_MAX_GAS_AMOUNT, getGasUnitPrice(),
                GAS_TOKEN_CODE, getExpirationTimestampSecs(), chainId);
    }

    private long getExpirationTimestampSecs() {
        //return System.currentTimeMillis() / 1000 + TimeUnit.HOURS.toSeconds(1);
        String resultStr = call("node.info", Collections.emptyList());
        JSONObject jsonObject = JSON.parseObject(resultStr);
        return jsonObject.getJSONObject("result").getLong("now_seconds") + DEFAULT_TRANSACTION_EXPIRATION_SECONDS;
    }

    private long getGasUnitPrice() {
        //return 1L;
        String resultStr = call("txpool.gas_price", Collections.emptyList());
        JSONObject jsonObject = JSON.parseObject(resultStr);
        return jsonObject.getLong("result");
    }

    //  @TODO
    public String dryRunTransaction(AccountAddress sender, Ed25519PrivateKey privateKey,
                                    TransactionPayload payload) {
        throw new NotImplementedException("");
        //    RawUserTransaction rawUserTransaction = buildRawUserTransaction(sender, payload);
        //    return dryRunHexTransaction(privateKey, rawUserTransaction);
    }

    @SneakyThrows
    private String dryRunHexTransaction(Ed25519PrivateKey privateKey,
                                        RawUserTransaction rawUserTransaction) {
        SignedUserTransaction signedUserTransaction = SignatureUtils.signTxn(privateKey,
                rawUserTransaction);
        List<Object> params = Lists.newArrayList(Hex.encode(signedUserTransaction.bcsSerialize()));
        return call("contract.dry_run_raw", params);
    }

    public String submitTransaction(AccountAddress sender, Ed25519PrivateKey privateKey,
                                    TransactionPayload payload) {
        RawUserTransaction rawUserTransaction = buildRawUserTransaction(sender, payload);
        return submitHexTransaction(privateKey, rawUserTransaction);
    }

    public String submitTransaction(AccountAddress sender, long seqNumber,  Ed25519PrivateKey privateKey,
                                    TransactionPayload payload) {
        RawUserTransaction rawUserTransaction = buildRawUserTransaction(sender, seqNumber, payload);
        return submitHexTransaction(privateKey, rawUserTransaction);
    }

    @SneakyThrows
    public String deployContractPackage(AccountAddress sender, Ed25519PrivateKey privateKey,
                                        String filePath, ScriptFunctionObj initScriptObj) {

        org.starcoin.types.ScriptFunction sf =
                Objects.isNull(initScriptObj) ? null : initScriptObj.toScriptFunction();
        byte[] contractBytes = Files.toByteArray(new File(filePath));
        Module module = new Module(new Bytes(contractBytes));
        org.starcoin.types.Package contractPackage = new org.starcoin.types.Package(sender,
                Lists.newArrayList(
                        module),
                Optional.ofNullable(sf));
        TransactionPayload.Package.Builder builder = new TransactionPayload.Package.Builder();
        builder.value = contractPackage;
        TransactionPayload payload = builder.build();
        return submitTransaction(sender, privateKey, payload);
    }

    public String getTransactionInfo(String txn) {
        return call("chain.get_transaction_info", Lists.newArrayList(txn));
    }

    public String getResource(AccountAddress sender, ResourceObj resourceObj) {
        return call("contract.get_resource",
                Lists.newArrayList(AccountAddressUtils.hex(sender), resourceObj.toRPCString()));
    }

    /**
     * Read price from oracle.
     *
     * @param priceOracleType Oracle type.
     * @param address         Oracle address.
     * @return price.
     */
    public BigInteger priceOracleRead(String priceOracleType, String address) {
        String rspBody = contractCallV2(
                FUNCTION_ID_PRICE_ORACLE_READ,
                Collections.singletonList(priceOracleType),
                Collections.singletonList(address));
        JSONObject jsonObject = JSON.parseObject(rspBody);
        if (!indicatesSuccess(jsonObject)) {
            throw new RuntimeException("JSON RPC error: " + jsonObject.get("error"));
        }
        return new BigInteger(jsonObject.getJSONArray("result").get(0).toString());
    }

    /**
     * JSON RPC call method 'contract.call_v2'.
     *
     * @param functionId function Id.
     * @param typeArgs   type arguments.
     * @param args       arguments.
     * @return JSON RPC response body.
     */
    public String contractCallV2(String functionId, List<String> typeArgs, List<Object> args) {
        String method = "contract.call_v2";
        Map<String, Object> singleParamMap = new HashMap<>();
        singleParamMap.put("function_id", functionId);
        singleParamMap.put("type_args", typeArgs);
        singleParamMap.put("args", args);
        List<Object> params = Collections.singletonList(singleParamMap);
        return call(method, params);
    }
}
