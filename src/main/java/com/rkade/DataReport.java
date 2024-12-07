package com.rkade;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataReport {
    protected final byte reportType;
    protected final short reportIndex;
    protected final short section;

    public DataReport(byte reportType) {
        this.reportType = reportType;
        this.reportIndex = 0;
        this.section = 0;
    }

    public DataReport(byte reportType, byte reportIndex, short section) {
        this.reportType = reportType;
        this.reportIndex = reportIndex;
        this.section = section;
    }

    protected String getString(ByteBuffer buffer, int bytes) {
        byte[] newArray = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            newArray[i] = buffer.get();
        }
        return new String(newArray, StandardCharsets.ISO_8859_1);
    }

    public byte getReportType() {
        return reportType;
    }

    @Override
    public String toString() {
        return "DataReport{" +
                "reportType=" + reportType +
                ", reportIndex=" + reportIndex +
                ", section=" + section +
                '}';
    }
}
