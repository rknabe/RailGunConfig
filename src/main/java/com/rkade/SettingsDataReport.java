package com.rkade;

import java.nio.ByteBuffer;

public final class SettingsDataReport extends DataReport {
    private final String id; //6 bytes
    private final String version; //12 bytes;
    private final short xAxisMinimum;
    private final short xAxisMaximum;
    private final short yAxisMinimum;
    private final short yAxisMaximum;

    public SettingsDataReport(byte reportType, ByteBuffer buffer) {
        super(reportType);
        byte cmd = buffer.get();
        short args = buffer.getShort();
        id = getString(buffer, 9);
        version = getString(buffer, 5);
        xAxisMinimum = buffer.getShort();
        xAxisMaximum = buffer.getShort();
        yAxisMinimum = buffer.getShort();
        yAxisMaximum = buffer.getShort();
    }

    public short getXAxisMinimum() {
        return xAxisMinimum;
    }

    public short getXAxisMaximum() {
        return xAxisMaximum;
    }

    public short getYAxisMinimum() {
        return yAxisMinimum;
    }

    public short getYAxisMaximum() {
        return yAxisMaximum;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }
}
