package org.starcoin.stcpricereporter.data.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class MoveStructType {

    @Column(length = 34, nullable = false)
    private String address;

    @Column(nullable = false)
    private String module;

    @Column(nullable = false)
    private String name;

    // private java.util.List<TypeTag> type_params;

    public MoveStructType() {
    }

    public MoveStructType(String address, String module, String name) {
        this.address = address;
        this.module = module;
        this.name = name;
    }

    public static MoveStructType parse(String s) {
        String[] fs = s.split("::");
        if (fs.length != 3) {
            throw new RuntimeException("Illegal string format.");
        }
        return new MoveStructType(fs[0], fs[1], fs[2]);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "StructType{" +
                "address='" + address + '\'' +
                ", module='" + module + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String toTypeTagString() {
        return address + "::" + module + "::" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveStructType that = (MoveStructType) o;
        return Objects.equals(address, that.address) && Objects.equals(module, that.module) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, module, name);
    }
}
