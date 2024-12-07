package com.rkade;

import com.fazecast.jSerialComm.SerialPort;
import purejavahidapi.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class DeviceManager implements InputReportListener, DeviceRemovalListener {
    private final static Logger logger = Logger.getLogger(DeviceManager.class.getName());
    private final static int LEONARDO_VENDOR_ID = 0x2341;
    private final static int LEONARDO_PRODUCT_ID = 0x8036;
    private final static int OUTPUT_REPORT_DATA_LENGTH = 7;
    private final static int SLEEP_BETWEEN_OUTPUT_REPORT = 10;
    private final static byte AXIS_COUNT = 7;
    private final static List<DeviceListener> deviceListeners = Collections.synchronizedList(new ArrayList<>());
    private final static Map<String, Device> deviceMap = Collections.synchronizedMap(new HashMap<>());
    private static volatile boolean deviceAttached = false;
    private static volatile boolean versionReported = false;
    private static volatile HidDeviceInfo deviceInfo = null;
    private static volatile HidDevice openedDevice = null;

    public DeviceManager(DeviceListener listener) {
        addDeviceListener(listener);
        new Thread(new ConnectionRunner()).start();
        new Thread(new OutputReportRunner()).start();
    }

    private static void notifyListenersDeviceAttached(Device device) {
        for (DeviceListener deviceListener : deviceListeners) {
            deviceListener.deviceAttached(device);
        }
    }

    private static void notifyListenersDeviceDetached(Device device) {
        for (DeviceListener deviceListener : deviceListeners) {
            deviceListener.deviceDetached(device);
        }
    }

    private static void notifyListenersDeviceUpdated(Device device, String status, DataReport report) {
        for (DeviceListener deviceListener : deviceListeners) {
            deviceListener.deviceUpdated(device, status, report);
        }
    }

    public static Device openDevice() {
        List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
        HidDeviceInfo myHidInfo = null;
        for (HidDeviceInfo info : devList) {
            if (info.getVendorId() == LEONARDO_VENDOR_ID && info.getProductId() == LEONARDO_PRODUCT_ID) {
                deviceInfo = info;
                myHidInfo = info;
                break;
            }
        }
        if (myHidInfo == null) {
            logger.info("device not found");
            notifyListenersDeviceUpdated(null, "Device Not Found...", null);
            sleep(1000);
        } else {
            logger.info("device found");
            notifyListenersDeviceUpdated(null, "Device Found...", null);
            deviceAttached = true;
            try {
                logger.info("opening device...");
                openedDevice = PureJavaHidApi.openDevice(myHidInfo);
                if (openedDevice != null) {
                    openedDevice.open();
                    Device device = getDevice(openedDevice);
                    if (device != null) {
                        SerialPort[] ports = SerialPort.getCommPorts();
                        for (SerialPort port : ports) {
                            if (port.getVendorID() == LEONARDO_VENDOR_ID && port.getProductID() == LEONARDO_PRODUCT_ID) {
                                device.setName(port.getDescriptivePortName());
                                device.setPort(port);
                                return device;
                                /*
                                String version = device.readVersion();
                                if (version != null && version.contains(":")) {
                                    String[] parts = version.split(":");
                                    if (parts.length == 2) {
                                        String firmwareType = parts[0];
                                        if (Device.SUPPORTED_FIRMWARE_TYPE.equalsIgnoreCase(firmwareType)) {
                                            logger.info("Device Attached");
                                            device.setFirmwareType(firmwareType);
                                            device.setFirmwareVersion(parts[1]);
                                            return device;
                                        } else {
                                            notifyListenersDeviceUpdated(null, "Unsupported Firmware", null);
                                        }
                                    }
                                }*/
                            }
                        }
                    } else {
                        logger.severe("Device could not be obtained");
                    }
                    return null;
                } else {
                    logger.severe("Device could not be opened");
                }
            } catch (IOException ex) {
                logger.warning(ex.getMessage());
            }
        }
        return null;
    }

    private static Device getDevice(HidDevice hidDevice) {
        String path = getHidPath(hidDevice);
        return deviceMap.computeIfAbsent(path, k -> new Device(hidDevice, path));
    }

    private static String getHidPath(HidDevice device) {
        if (device != null) {
            //hidPath is not null terminated, force to it null-term and uppercase to match SDL case
            return device.getHidDeviceInfo().getPath().trim().toUpperCase();
        }
        return null;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
    }

    public void addDeviceListener(DeviceListener deviceListener) {
        deviceListeners.add(deviceListener);
    }

    @Override
    public void onDeviceRemoval(HidDevice hidDevice) {
        logger.info("device removed");
        deviceAttached = false;
        versionReported = false;
        deviceInfo = null;
        openedDevice = null;
        Device device = getDevice(hidDevice);
        deviceMap.remove(getHidPath(hidDevice));
        notifyListenersDeviceDetached(device);
    }

    @Override
    public void onInputReport(HidDevice hidDevice, byte id, byte[] data, int len) {
        if (id == Device.DATA_REPORT_ID || id == Device.CMD_GET_VER) {
            List<DataReport> reports = DataReportFactory.create(id, data);
            for (DataReport report : reports) {
                if (report instanceof SettingsDataReport) {
                    if (versionReported) {
                        continue;
                    }
                    versionReported = true;
                }
                notifyListenersDeviceUpdated(getDevice(hidDevice), null, report);
            }
        }
    }

    private void getOutputReport(byte dataType, byte dataIndex, byte[] data) throws IOException {
        data[0] = dataType;
        data[1] = dataIndex;
        int ret = openedDevice.setOutputReport(Device.CMD_REPORT_ID, data, OUTPUT_REPORT_DATA_LENGTH);
        if (ret <= 0) {
            throw new IOException("Device returned error for dataType:" + dataType + " dataIndex:" + dataIndex);
        }
        sleep(SLEEP_BETWEEN_OUTPUT_REPORT);
    }

    private final class ConnectionRunner implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (!deviceAttached) {
                    deviceInfo = null;
                    logger.info("scanning");
                    notifyListenersDeviceUpdated(null, "Scanning...", null);
                    Device device = openDevice();
                    if (device != null) {
                        notifyListenersDeviceUpdated(device, "Opened", null);
                        openedDevice.setDeviceRemovalListener(DeviceManager.this);
                        openedDevice.setInputReportListener(DeviceManager.this);
                        notifyListenersDeviceAttached(device);
                    }
                }
                sleep(2000);
            }
        }
    }

    private final class OutputReportRunner implements Runnable {
        @Override
        public void run() {
            int failCount = 0;
            byte[] data = new byte[OUTPUT_REPORT_DATA_LENGTH];
            while (true) {
                if (openedDevice != null) {
                    try {
                        if (!versionReported) {
                            //only need to do this once
                            getOutputReport((byte)1, (byte) 0, data);
                        }
                        else {
                            //use this as heartbeat to check usb connections
                            getOutputReport((byte)0, (byte) 0, data);
                        }

                        //getOutputReport(Device.CMD_GET_STEER, (byte) 0, data);
                        //for (byte i = 0; i < AXIS_COUNT; i++) {
                        //    getOutputReport(Device.CMD_GET_ANALOG, i, data);
                        //}
                        //getOutputReport(Device.CMD_GET_BUTTONS, (byte) 0, data);
                        //getOutputReport(Device.CMD_GET_GAINS, (byte) 0, data);
                        //getOutputReport(Device.CMD_GET_MISC, (byte) 0, data);
                        failCount = 0;
                        sleep(2000);
                    } catch (IOException ex) {
                        ++failCount;
                        if (failCount > 3) {
                            onDeviceRemoval(openedDevice);
                        }
                        sleep(250);
                        logger.warning(ex.getMessage());
                    }
                } else {
                    sleep(500);
                }
            }
        }
    }
}