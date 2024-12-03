package com.rkade;

import java.nio.ByteBuffer;

public final class AxisDataReport extends DataReport {
    private final short rawValue;
    private final short value;
    private final short min;
    private final short max;
    private final short center;
    private final short deadZone;
    private final boolean autoLimit;
    private final boolean hasCenter;
    private final boolean enabled;
    private final byte trim;
    private final int axis;

    public AxisDataReport(byte reportType, byte reportIndex, short section, ByteBuffer buffer) {
        super(reportType, reportIndex, section);
        axis = section + 1;
        rawValue = buffer.getShort();
        value = buffer.getShort();
        min = buffer.getShort();
        max = buffer.getShort();

        center = buffer.getShort();
        deadZone = buffer.getShort();

        autoLimit = (buffer.get() == 1);
        hasCenter = (buffer.get() == 1);
        enabled = !(buffer.get() == 1);
        trim = buffer.get();
    }

    public int getAxis() {
        return axis;
    }

    public short getRawValue() {
        return rawValue;
    }

    public short getValue() {
        return value;
    }

    public short getMin() {
        return min;
    }

    public short getMax() {
        return max;
    }

    public short getCenter() {
        return center;
    }

    public short getDeadZone() {
        return deadZone;
    }

    public boolean isAutoLimit() {
        return autoLimit;
    }

    public boolean isHasCenter() {
        return hasCenter;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public byte getTrim() {
        return trim;
    }
}
