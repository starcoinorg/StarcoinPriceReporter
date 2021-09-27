package org.starcoin.stcpricereporter.vo;

import org.starcoin.stcpricereporter.data.model.MoveStructType;

public class PriceOracleType {
    private final String moduleAddress;
    private final String moduleName;
    private final String structName;

    public PriceOracleType(String moduleAddress, String moduleName, String structName) {
        this.moduleAddress = moduleAddress;
        this.moduleName = moduleName;
        this.structName = structName;
    }

    public String getModuleAddress() {
        return moduleAddress;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getStructName() {
        return structName;
    }

    @Override
    public String toString() {
        return "PriceOracleType{" +
                "moduleAddress='" + moduleAddress + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", structName='" + structName + '\'' +
                '}';
    }

    public MoveStructType toMoveStructType() {
        return new MoveStructType(
                this.getModuleAddress(), this.getModuleName(), this.getStructName()
        );
    }
}
