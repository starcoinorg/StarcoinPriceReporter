package org.starcoin.stcpricereporter.taskservice;


public interface OffChainPriceCache<PT> {
    boolean tryUpdate(PT price, Long timestamp);

    boolean isDirty();

    void setDirty(boolean dirty);

    boolean isFirstUpdate();
}
