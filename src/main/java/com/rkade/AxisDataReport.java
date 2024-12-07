package com.rkade;

import java.nio.ByteBuffer;

public final class AxisDataReport extends DataReport {
    private final int x;
    private final int y;
    //private final int xRaw;
    //private final int yMax;
    private final int axis;

    public AxisDataReport(byte reportType, ByteBuffer buffer) {
        super(reportType);
        axis = 0;
        x = buffer.getShort();
        y = buffer.getShort();
        while (buffer.hasRemaining()) {
            System.out.println(buffer.getShort());
        }
        //xRaw = buffer.getShort();
        System.out.println(x + ":" + y);
        //System.out.println(xRaw);
    }

    public int getAxis() {
        return axis;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
