package com.rkade;

import java.nio.ByteBuffer;

public final class AxisDataReport extends DataReport {
    private final short x;
    private final short y;
    private final int axis;

    public AxisDataReport(byte reportType, ByteBuffer buffer) {
        super(reportType);
        axis = 0;
        x = buffer.getShort();
        y = buffer.getShort();
    }

    public int getAxis() {
        return axis;
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }
}
