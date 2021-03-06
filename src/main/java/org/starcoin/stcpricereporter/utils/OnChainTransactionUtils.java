package org.starcoin.stcpricereporter.utils;

import com.novi.bcs.BcsSerializer;
import com.novi.serde.Bytes;
import com.novi.serde.Int128;
import com.novi.serde.SerializationError;
import com.novi.serde.Unsigned;
import org.starcoin.bean.TypeObj;
import org.starcoin.stcpricereporter.vo.PriceOracleType;
import org.starcoin.types.*;

import java.math.BigInteger;
import java.util.Arrays;

public class OnChainTransactionUtils {

    private static final String PRICE_ORACLE_SCRIPTS_MODULE_NAME = "PriceOracleScripts";


    public static TypeTag toTypeTag(PriceOracleType priceOracleType) {
        TypeObj oracleTypeObject = TypeObj.builder()
                .moduleName(priceOracleType.getModuleName())
                .moduleAddress(priceOracleType.getModuleAddress())
                .name(priceOracleType.getStructName()).build();
        TypeTag oracleTypeTag = oracleTypeObject.toTypeTag();
        return oracleTypeTag;
    }

    /**
     * public(script) fun update<OracleT: copy+store+drop>(signer: signer, value: u128) {
     * PriceOracle::update<OracleT>(&signer, value);
     * }
     */
    public static TransactionPayload buildPriceOracleUpdateTransaction(TypeTag oracleType,
                                                                       @Unsigned @Int128 BigInteger price,
                                                                       String oracleScriptsAddress) {
        ScriptFunction.Builder script_function_builder = new ScriptFunction.Builder();
        script_function_builder.ty_args = Arrays.asList(oracleType);
        script_function_builder.args = Arrays.asList(encode_u128_argument(price));
        script_function_builder.function = new Identifier("update");
        script_function_builder.module = new ModuleId(AccountAddress.valueOf(
                CommonUtils.hexToByteArray(oracleScriptsAddress)),
                new Identifier(PRICE_ORACLE_SCRIPTS_MODULE_NAME));

        TransactionPayload.ScriptFunction.Builder builder = new TransactionPayload.ScriptFunction.Builder();
        builder.value = script_function_builder.build();
        return builder.build();
    }

    /**
     * public(script) fun init_data_source<OracleT: copy + store + drop>(signer: signer, init_value: u128) {
     */
    public static TransactionPayload buildPriceOracleInitDataSourceTransaction(TypeTag oracleType,
                                                                               @Unsigned @Int128 BigInteger price,
                                                                               String oracleScriptsAddress) {
        ScriptFunction.Builder script_function_builder = new ScriptFunction.Builder();
        script_function_builder.ty_args = Arrays.asList(oracleType);
        script_function_builder.args = Arrays.asList(encode_u128_argument(price));
        script_function_builder.function = new Identifier("init_data_source");
        script_function_builder.module = new ModuleId(AccountAddress.valueOf(
                CommonUtils.hexToByteArray(oracleScriptsAddress)),
                new Identifier(PRICE_ORACLE_SCRIPTS_MODULE_NAME));

        TransactionPayload.ScriptFunction.Builder builder = new TransactionPayload.ScriptFunction.Builder();
        builder.value = script_function_builder.build();
        return builder.build();
    }

    public static TransactionPayload buildPriceOracleRegisterTransaction(TypeTag oracleType,
                                                                         Byte precision,
                                                                         String oracleScriptsAddress) {
        ScriptFunction.Builder script_function_builder = new ScriptFunction.Builder();
        script_function_builder.ty_args = Arrays.asList(oracleType);
        script_function_builder.args = Arrays.asList(encode_u8_argument(precision));
        script_function_builder.function = new Identifier("register_oracle");
        script_function_builder.module = new ModuleId(AccountAddress.valueOf(
                CommonUtils.hexToByteArray(oracleScriptsAddress)),
                new Identifier(PRICE_ORACLE_SCRIPTS_MODULE_NAME));

        TransactionPayload.ScriptFunction.Builder builder = new TransactionPayload.ScriptFunction.Builder();
        builder.value = script_function_builder.build();
        return builder.build();
    }

    private static Bytes encode_u128_argument(@Unsigned @Int128 BigInteger arg) {
        try {
            BcsSerializer s = new BcsSerializer();
            s.serialize_u128(arg);
            return Bytes.valueOf(s.get_bytes());
        } catch (SerializationError var2) {
            throw new IllegalArgumentException("Unable to serialize argument of type u128");
        }
    }

    private static Bytes encode_u8_argument(@Unsigned Byte arg) {
        try {
            BcsSerializer s = new BcsSerializer();
            s.serialize_u8(arg);
            return Bytes.valueOf(s.get_bytes());
        } catch (SerializationError e) {
            throw new IllegalArgumentException("Unable to serialize argument of type u8");
        }
    }
}
