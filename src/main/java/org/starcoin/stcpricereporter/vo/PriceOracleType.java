package org.starcoin.stcpricereporter.vo;

public class PriceOracleType {
    private String moduleAddress;
    private String moduleName;
    private String structName;

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
}
