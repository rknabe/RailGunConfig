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
    private final static int OUTPUT_REPORT_DATA_LENGTH = 9;
    private final static List<DeviceListener> deviceListeners = Collections.synchronizedList(new ArrayList<>());
    private final static Map<String, Device> deviceMap = Collections.synchronizedMap(new HashMap<>());
    private final static Map<Device, SettingsDataReport> deviceSettings = Collections.synchronizedMap(new HashMap<>());
    private final static List<HidDeviceInfo> devList = Collections.synchronizedList(new ArrayList<>());
    private static final Random random = new Random();
    private static HidDevice openedDevice = null;

    public DeviceManager(DeviceListener listener) {
        addDeviceListener(listener);
        SerialPort.autoCleanupAtShutdown();
        new Thread(new ConnectionChecker()).start();
        scanDevices();
    }

    private void notifyListenersDeviceFound(Device device) {
        for (DeviceListener deviceListener : deviceListeners) {
            deviceListener.deviceFound(device);
        }
    }

    private void notifyListenersDeviceAttached(Device device) {
        for (DeviceListener deviceListener : deviceListeners) {
            deviceListener.deviceAttached(device);
        }
    }

    private void notifyListenersDeviceDetached(Device device) {
        for (DeviceListener deviceListener : deviceListeners) {
            deviceListener.deviceDetached(device);
        }
    }

    private void notifyListenersDeviceUpdated(Device device, String status, DataReport report) {
        if (report != null && openedDevice != null && device.getHidDevice() == openedDevice) {
            for (DeviceListener deviceListener : deviceListeners) {
                deviceListener.deviceUpdated(device, status, report);
            }
        }
    }

    private Device getDevice(HidDevice hidDevice) {
        return deviceMap.computeIfAbsent(getHidPath(hidDevice), k -> new Device(hidDevice));
    }

    private String getHidPath(HidDevice device) {
        if (device != null) {
            //hidPath is not null terminated, force to it null-term and uppercase to match SDL case
            return device.getHidDeviceInfo().getPath().trim().toUpperCase();
        }
        return null;
    }

    private void sleep(int millis) {
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
        //deviceAttached = false;
        //versionReported = false;
        //deviceInfo = null;
        Device device = getDevice(hidDevice);
        deviceMap.remove(getHidPath(hidDevice));
        deviceSettings.remove(device);
        notifyListenersDeviceDetached(device);
        openedDevice = null;
    }

    @Override
    public void onInputReport(HidDevice hidDevice, byte id, byte[] data, int len) {
        if (id == Device.DATA_REPORT_ID || id == Device.CMD_GET_VER) {
            List<DataReport> reports = DataReportFactory.create(id, data);
            Device device = getDevice(hidDevice);
            for (DataReport report : reports) {
                if (report instanceof SettingsDataReport settings) {
                    SettingsDataReport prevSettings = deviceSettings.get(device);
                    if (prevSettings != null) {//not first pass
                        continue;
                    }
                    deviceSettings.put(device, settings);
                    if (Device.FIRMWARE_TYPE.equalsIgnoreCase(settings.getDeviceType())) {
                        hidDevice.setInputReportListener(null);  //stop listening until connected
                        //Device device = getDevice(hidDevice);
                        device.setName(settings.getDeviceType());
                        short uniqueId = (short) random.nextInt(Short.MAX_VALUE + 1);
                        boolean ret = device.setUniqueId(uniqueId);
                        sleep(20);
                        if (ret) {
                            SerialPort port = findMatchingCommPort(uniqueId);
                            if (port != null) {
                                device.setName(settings.getDeviceType() + " (" + port.getSystemPortName() + ")");
                            }
                        }
                        device.setFirmwareType(settings.getDeviceType());
                        device.setFirmwareVersion(settings.getDeviceVersion());
                        notifyListenersDeviceFound(getDevice(hidDevice));
                    }
                } else {
                    notifyListenersDeviceUpdated(getDevice(hidDevice), null, report);
                }
            }
        }
    }

    private void closeDevice(HidDevice hidDevice) {
        try {
            hidDevice.close();
        } catch (Exception _) {
        }
    }

    private SerialPort findMatchingCommPort(short uniqueId) {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            if (port.getVendorID() == LEONARDO_VENDOR_ID && port.getProductID() == LEONARDO_PRODUCT_ID) {
                String uniqueIdStr = Device.readUniqueId(port);
                if (uniqueIdStr != null) try {
                    short id = Short.parseShort(uniqueIdStr);
                    if (id == uniqueId) {
                        return port;
                    }
                } catch (Exception ex) {
                    logger.warning(ex.getMessage());
                }
            }
        }
        return null;
    }

    public void getOutputReport(HidDevice hidDevice, byte dataType, byte dataIndex, byte[] data) throws IOException {
        data[0] = dataType;
        data[1] = dataIndex;
        int ret = hidDevice.setOutputReport(Device.CMD_REPORT_ID, data, OUTPUT_REPORT_DATA_LENGTH);
        if (ret <= 0) {
            throw new IOException("Device returned error for dataType:" + dataType + " dataIndex:" + dataIndex);
        }
    }

    public void getOutputReport(byte dataType, byte dataIndex, byte[] data) throws IOException {
        getOutputReport(openedDevice, dataType, dataIndex, data);
    }

    public boolean connectDevice(Device device) {
        if (device != null) {
            if (openedDevice != null) {
                openedDevice.setDeviceRemovalListener(null);
                openedDevice.setInputReportListener(null);
                closeDevice(openedDevice);
            }
            HidDevice hidDevice = device.getHidDevice();
            openedDevice = hidDevice;
            hidDevice.setDeviceRemovalListener(DeviceManager.this);
            hidDevice.setInputReportListener(DeviceManager.this);
            notifyListenersDeviceUpdated(device, "Attached", deviceSettings.get(device));
            notifyListenersDeviceAttached(device);
            return true;
        }
        return false;
    }

    public synchronized void scanDevices() {
        scanDevices(filterToArduinos(PureJavaHidApi.enumerateDevices()));
    }

    public synchronized void scanDevices(List<HidDeviceInfo> devices) {
        byte[] reportData = new byte[OUTPUT_REPORT_DATA_LENGTH];
        devList.clear();
        devList.addAll(devices);
        for (HidDeviceInfo info : devList) {
            try {
                HidDevice openedDevice = PureJavaHidApi.openDevice(info);
                if (openedDevice != null) {
                    openedDevice.open();
                    openedDevice.setInputReportListener(DeviceManager.this);
                    getOutputReport(openedDevice, Device.CMD_GET_SETTINGS, (byte) 0, reportData);
                }
            } catch (Exception ex) {
                logger.warning(ex.getMessage());
            }
        }
    }

    private List<HidDeviceInfo> filterToArduinos(List<HidDeviceInfo> allDevices) {
        List<HidDeviceInfo> arduinos = new ArrayList<>();
        for (HidDeviceInfo info : allDevices) {
            if (info.getVendorId() == LEONARDO_VENDOR_ID && info.getProductId() == LEONARDO_PRODUCT_ID) {
                arduinos.add(info);
            }
        }
        return arduinos;
    }

    private boolean devicesChanged(List<HidDeviceInfo> currentDevList) {
        HashSet<DeviceInfo> currentDevices = new HashSet<>();
        HashSet<DeviceInfo> previousDevices = new HashSet<>();
        for (HidDeviceInfo hidDeviceInfo : currentDevList) {
            currentDevices.add(new DeviceInfo(hidDeviceInfo));
        }
        for (HidDeviceInfo hidDeviceInfo : devList) {
            previousDevices.add(new DeviceInfo(hidDeviceInfo));
        }

        HashSet<DeviceInfo> removedDevices = new HashSet<>(previousDevices);
        HashSet<DeviceInfo> addedDevices = new HashSet<>(currentDevices);
        removedDevices.removeAll(currentDevices);
        addedDevices.removeAll(previousDevices);  //this is now added devices

        for (DeviceInfo info : removedDevices) {
            Device device = deviceMap.get(info.getPath());
            if (device != null) {
                onDeviceRemoval(device.getHidDevice());
            }
        }

        if (!removedDevices.isEmpty()) {
            return true;
        }
        if (!addedDevices.isEmpty()) {
            return true;
        }

        return false;
    }

    private final class ConnectionChecker implements Runnable {
        @Override
        public void run() {
            while (true) {
                List<HidDeviceInfo> devices = filterToArduinos(PureJavaHidApi.enumerateDevices());
                if (devicesChanged(devices)) {
                    scanDevices(devices);
                }
                sleep(2000);
            }
        }
    }
}