package com.rkade;

import java.nio.ByteBuffer;

public final class WheelDataReport extends DataReport {
    private final int rawValue;
    private final short value;
    private final short range;
    private final short velocity;
    private final short acceleration;
    private final double angle;
    private final short min;
    private final short max;
    private final short center;
    private final short deadZone;
    private final boolean autoLimit;
    private final byte trim;
    private final boolean invertRotation;

    public WheelDataReport(byte reportType, byte reportIndex, short section, ByteBuffer buffer) {
        super(reportType, reportIndex, section);

        rawValue = buffer.getInt();
        value = buffer.getShort();
        range = buffer.getShort();
        velocity = buffer.getShort();
        acceleration = buffer.getShort();
        min = buffer.getShort();
        max = buffer.getShort();
        center = buffer.getShort();
        deadZone = buffer.getShort();
        autoLimit = (buffer.get() == 1);
        trim = buffer.get();
        invertRotation = (buffer.get() == 1);
        if (range == 0) {
            angle = 0;
        } else {
            angle = (double) value / ((double) Short.MAX_VALUE / ((double) range / 2.0));
        }
    }

    public int getRawValue() {
        return rawValue;
    }

    public short getValue() {
        return value;
    }

    public short getRange() {
        return range;
    }

    public short getVelocity() {
        return velocity;
    }

    public short getAcceleration() {
        return acceleration;
    }

    public double getAngle() {
        return angle;
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

    public byte getTrim() {
        return trim;
    }

    public boolean isInvertRotation() {
        return invertRotation;
    }
}
