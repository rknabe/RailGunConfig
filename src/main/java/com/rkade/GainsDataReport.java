package com.rkade;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class GainsDataReport extends DataReport {
    private final List<Short> gainValues = new ArrayList<>(Device.DATA_REPORT_VALUE_COUNT);

    public GainsDataReport(byte reportType, byte reportIndex, short section, ByteBuffer buffer) {
        super(reportType, reportIndex, section);
        while (buffer.hasRemaining()) {
            gainValues.add(buffer.getShort());
        }
    }

    public short getGainValue(short index) {
        return gainValues.get(index);
    }
}
