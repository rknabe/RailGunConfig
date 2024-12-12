package com.rkade;

import java.nio.ByteBuffer;

public final class GunDataReport extends DataReport {
    private final int ammoCount;
    private final boolean useAmmoCount;

    public GunDataReport(byte reportType, ByteBuffer buffer) {
        super(reportType);
        ammoCount = buffer.getShort();
        useAmmoCount = buffer.get() > 0;
    }

    public int getAmmoCount() {
        return ammoCount;
    }

    public boolean usingAmmoCount() {
        return useAmmoCount;
    }
}
