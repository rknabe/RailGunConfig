package com.rkade;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public abstract class DataReportFactory {

    public static List<DataReport> create(byte reportType, byte[] data) {
        List<DataReport> reports = new ArrayList<>();
        if (reportType == Device.DATA_REPORT_ID || reportType == Device.CMD_GET_VER) {
            ByteBuffer buffer = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(data);
            buffer.rewind();
            //byte reportIndex = buffer.get();
            //short section = buffer.getShort();
            //short section = 0;

            if (reportType == Device.CMD_GET_VER) {
                reports.add(new SettingsDataReport(reportType, buffer));
            } else {
                reports.add(new ButtonsDataReport(reportType, buffer));
                reports.add(new AxisDataReport(reportType, buffer));
            }

            /*
            return switch (reportType) {
                case Device.CMD_GET_STEER -> new WheelDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_ANALOG -> new AxisDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_GAINS -> new GainsDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_MISC -> new MiscDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_BUTTONS -> new ButtonsDataReport(reportType, reportIndex, section, buffer);
                case Device.CMD_GET_VER -> new VersionDataReport(reportType, reportIndex, section, buffer);
                default -> null;
            };*/
        }
        return reports;
    }
}
