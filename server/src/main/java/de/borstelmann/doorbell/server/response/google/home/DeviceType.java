package de.borstelmann.doorbell.server.response.google.home;

public enum DeviceType {
    LOCK("action.devices.types.LOCK");

    private final String deviceTypeName;

    DeviceType(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    @Override
    public String toString() {
        return deviceTypeName;
    }
}
