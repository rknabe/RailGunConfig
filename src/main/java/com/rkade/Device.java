package com.rkade;

import com.fazecast.jSerialComm.SerialPort;
import purejavahidapi.HidDevice;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class Device {
    public static final byte CMD_REPORT_ID = 15;
    public static final byte DATA_REPORT_ID = 1;
    public static final byte DATA_REPORT_VALUE_COUNT = 31;
    public static final byte CMD_GET_VER = 16;
    public static final byte CMD_GET_SETTINGS = 1;
    public static final byte CMD_HEARTBEAT = 0;
    public static final byte CMD_SET_AXIS_LIMITS = 2;
    public static final byte CMD_SET_AUTO_RECOIL = 3;
    public static final byte CMD_SET_TRIGGER_RATE = 4;
    public static final byte CMD_SET_TRIGGER_HOLD = 5;
    public static final byte CMD_EESAVE = 16;
    public static final byte CMD_EELOAD = 17;
    public static final byte CMD_DEFAULT = 18;
    public static final byte CMD_RECOIL = 19;
    public static final byte CMD_GET_MISC = 6;
    public static final byte CMD_SET_RANGE = 10;
    public static final byte CMD_SET_AALIMITS = 11;
    public static final byte CMD_SET_AACENTER = 12;
    public static final byte CMD_SET_AADZ = 13;
    public static final byte CMD_SET_AAAUTOLIM = 14;
    public static final byte CMD_CENTER = 23;
    public static final byte CMD_WHEEL_LIMITS = 24;
    public static final byte CMD_WHEEL_CENTER = 25;
    public static final byte CMD_WHEEL_DZ = 26;
    public static final byte CMD_WHEEL_AUTO_LIMIT = 27;
    public static final byte CMD_WHEEL_TRIM = 28;
    public static final byte CMD_WHEEL_INVERT = 29;
    public static final String CMD_AUTOCENTER_TEXT = "autocenter ";
    public static final String CMD_CENTER_TEXT = "center ";
    public static final String CMD_SPRING_ON_TEXT = "spring 1 ";
    public static final String CMD_SPRING_OFF_TEXT = "spring 0 ";
    public static final String CMD_VERSION = "version ";
    public static final String FIRMWARE_TYPE = "RKADE-GUN";
    private static final Logger logger = Logger.getLogger(Device.class.getName());
    private static final int WAIT_AFTER_EFFECT_UPDATE = 5;
    private final String hidPath;
    private final HidDevice hidDevice;
    private String name;
    private SerialPort port;
    private String firmwareType;
    private String firmwareVersion;

    public Device(HidDevice hidDevice, String path) {
        this.hidDevice = hidDevice;
        this.name = hidDevice.getHidDeviceInfo().getProductString();
        this.hidPath = path;
    }

    @Override
    public String toString() {
        return name;
    }

    public synchronized boolean saveSettings() {
        return sendCommand(CMD_EESAVE);
    }

    public synchronized boolean loadDefaults() {
        return sendCommand(CMD_DEFAULT);
    }

    public synchronized boolean loadFromEeprom() {
        return sendCommand(CMD_EELOAD);
    }

    public synchronized boolean setAutoRecoil(boolean state) {
        return sendCommand(CMD_SET_AUTO_RECOIL, state);
    }

    public synchronized boolean doRecoil(boolean state) {
        return sendCommand(CMD_RECOIL, state);
    }

    public synchronized boolean setWheelCenter() {
        return sendCommand(CMD_CENTER);
    }

    public synchronized boolean setWheelCenterCli() {
        return writeTextToPort(CMD_CENTER_TEXT);
    }

    public synchronized boolean setWheelRange(Short range) {
        return sendCommand(CMD_SET_RANGE, range);
    }

    public synchronized boolean setWheelLimits(Short minValue, short maxValue) {
        return sendCommand(CMD_WHEEL_LIMITS, minValue, maxValue);
    }

    public synchronized boolean setWheelCenter(short center) {
        return sendCommand(CMD_WHEEL_CENTER, center);
    }

    public synchronized boolean setWheelDeadZone(short deadZone) {
        return sendCommand(CMD_WHEEL_DZ, deadZone);
    }

    public synchronized boolean setWheelAutoLimit(short flag) {
        return sendCommand(CMD_WHEEL_AUTO_LIMIT, flag);
    }

    public synchronized boolean setWheelTrim(short trimIndex) {
        return sendCommand(CMD_WHEEL_TRIM, trimIndex);
    }

    public synchronized boolean setWheelInvert(short state) {
        return sendCommand(CMD_WHEEL_INVERT, state);
    }

    public synchronized boolean setTriggerRepeatRate(short rate) {
        return sendCommand(CMD_SET_TRIGGER_RATE, rate);
    }

    public synchronized boolean setTriggerHoldTime(short time) {
        System.out.println("sending trigger hold:" + time);
        return sendCommand(CMD_SET_TRIGGER_HOLD, time);
    }

    public synchronized boolean setAxisLimits(short xMinValue, short xMaxValue, short yMinValue, short yMaxValue) {
        return sendCommand(CMD_SET_AXIS_LIMITS, xMinValue, xMaxValue, yMinValue, yMaxValue);
    }

    public synchronized boolean setConstantSpring(boolean state) {
        if (state) {
            return writeTextToPort(CMD_SPRING_ON_TEXT);
        }
        return writeTextToPort(CMD_SPRING_OFF_TEXT);
    }

    public synchronized boolean writeTextToPort(String text) {
        boolean isOpen = port.isOpen();
        if (!isOpen) {
            port.setBaudRate(9600);
            port.setParity(0);
            port.setNumStopBits(1);
            port.setNumDataBits(8);
            isOpen = port.openPort(500);
        }
        if (isOpen) {
            byte[] value = text.getBytes(StandardCharsets.US_ASCII);
            int ret = port.writeBytes(value, value.length);
            port.closePort();
            return (ret > 0);
        }
        return false;
    }

    public synchronized String readVersion() {
        boolean isOpen = port.isOpen();
        if (!isOpen) {
            port.setBaudRate(9600);
            port.setParity(0);
            port.setNumStopBits(1);
            port.setNumDataBits(8);
            isOpen = port.openPort(500);
        }
        if (isOpen) {
            byte[] value = CMD_VERSION.getBytes(StandardCharsets.US_ASCII);
            int ret = port.writeBytes(value, value.length);
            if (ret > 0) {
                try {
                    InputStream is = port.getInputStream();
                    port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                    InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    return bufferedReader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    port.closePort();
                }
            }
            return null;
        }
        return null;
    }

    public synchronized boolean runAutoCenter() {
        return writeTextToPort(CMD_AUTOCENTER_TEXT);
    }

    public void setPort(SerialPort port) {
        this.port = port;
    }

    private boolean sendCommand(byte command) {
        return sendCommand(command, (short) 0, (short) 0, (short) 0);
    }

    private boolean sendCommand(byte command, boolean state) {
        return sendCommand(command, (short) (state ? 1 : 0), (short) 0);
    }

    private boolean sendCommand(byte command, short arg1) {
        return sendCommand(command, arg1, (short) 0, (short) 0);
    }

    private boolean sendCommand(byte command, short arg1, short arg2) {
        return sendCommand(command, arg1, arg2, (short) 0);
    }

    private boolean sendCommand(byte command, short arg1, short arg2, short arg3) {
        return sendCommand(command, arg1, arg2, arg3, (short) 0);
    }

    private boolean sendCommand(byte command, short arg1, short arg2, short arg3, short arg4) {
        final boolean[] status = {true};
        //TODO: this will not work with CLI only invocation
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            public Void doInBackground() {
                byte[] data = new byte[9];
                data[0] = command;
                data[1] = getFirstByte(arg1);
                data[2] = getSecondByte(arg1);

                data[3] = getFirstByte(arg2);
                data[4] = getSecondByte(arg2);

                data[5] = getFirstByte(arg3);
                data[6] = getSecondByte(arg3);

                data[7] = getFirstByte(arg4);
                data[8] = getSecondByte(arg4);

                int ret = hidDevice.setOutputReport(CMD_REPORT_ID, data, 9);
                if (ret <= 0) {
                    logger.severe("Device returned error on Save:" + ret);
                    status[0] = false;
                }
                return null;
            }
        };
        worker.execute();
        return status[0];
    }

    private byte getFirstByte(short value) {
        return (byte) (value & 0xff);
    }

    private byte getSecondByte(short value) {
        return (byte) ((value >> 8) & 0xff);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirmwareType() {
        return firmwareType;
    }

    public void setFirmwareType(String firmwareType) {
        this.firmwareType = firmwareType;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
    }
}
