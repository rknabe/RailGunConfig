package com.rkade;

import java.nio.ByteBuffer;

public final class SettingsDataReport extends DataReport {
    private final String deviceType;
    private final String deviceVersion;
    private final short xAxisMinimum;
    private final short xAxisMaximum;
    private final short yAxisMinimum;
    private final short yAxisMaximum;
    private final boolean autoRecoil;
    private final short triggerRepeatDelay;
    private final short triggerHoldTime;

    public SettingsDataReport(byte reportType, ByteBuffer buffer) {
        super(reportType);
        byte cmd = buffer.get();
        short args = buffer.getShort();
        deviceType = getString(buffer, 10).trim();
        deviceVersion = getString(buffer, 6).trim();
        xAxisMinimum = buffer.getShort();
        xAxisMaximum = buffer.getShort();
        yAxisMinimum = buffer.getShort();
        yAxisMaximum = buffer.getShort();
        autoRecoil = buffer.get() > 0;
        triggerRepeatDelay = buffer.getShort();
        triggerHoldTime = buffer.getShort();
    }

    public short getTriggerHoldTime() {
        return triggerHoldTime;
    }

    public short getTriggerRepeatDelay() {
        return triggerRepeatDelay;
    }

    public boolean isAutoRecoil() {
        return autoRecoil;
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

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }
}
