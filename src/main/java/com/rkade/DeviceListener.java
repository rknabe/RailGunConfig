package com.rkade;

public interface DeviceListener {
    void deviceAttached(Device device);

    void deviceDetached(Device device);

    void deviceUpdated(Device device, String status, DataReport report);
}
