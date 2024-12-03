package com.rkade;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class DataReportFactory {

    public static DataReport create(byte reportType, byte[] data) {
        if (reportType == Device.DATA_REPORT_ID) {
            ByteBuffer buffer = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(data);
            buffer.rewind();
            byte reportIndex = buffer.get();
            short section = buffer.getShort();

            switch (reportIndex) {
                case Device.CMD_GET_STEER:
                    return new WheelDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_ANALOG:
                    return new AxisDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_GAINS:
                    return new GainsDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_MISC:
                    return new MiscDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_BUTTONS:
                    return new ButtonsDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_VER:
                    return new VersionDataReport(reportType, reportIndex, section, buffer);
            }
        }

        return null;
    }
}
