package com.rkade;

import purejavahidapi.HidDeviceInfo;

import java.util.Objects;

public final class DeviceInfo {
    private final HidDeviceInfo hidDeviceInfo;
    private final String path;

    public DeviceInfo(HidDeviceInfo hidDeviceInfo) {
        this.hidDeviceInfo = hidDeviceInfo;
        this.path = hidDeviceInfo.getPath().trim().toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return Objects.equals(path, that.path);
    }

    public HidDeviceInfo getHidDeviceInfo() {
        return hidDeviceInfo;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
