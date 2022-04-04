package de.borstelmann.doorbell.server.services.mapper;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.actions.api.smarthome.SyncResponse.Payload.Device;

public class GoogleHomeMapper {

    public static final String LOCK_KEY = "lock";
    private static final String LOCK_UNLOCK_TRAIT = "action.devices.traits.LockUnlock";
    private static final String LOCK_DEVICE_TYPE = "action.devices.types.LOCK";
    public static final String ONLINE_KEY = "online";
    public static final String STATUS_KEY = "status";

    public static final String PENDING = "PENDING";
    public static final String SUCCESS = "SUCCESS";
    public static final String OFFLINE = "OFFLINE";
    public static final String IS_LOCKED_KEY = "isLocked";
    public static final String IS_JAMMED_KEY = "isJammed";

    public static Device[] toGoogleHome(List<DoorbellDevice> doorbells) {
        return doorbells.stream()
                .map(GoogleHomeMapper::toGoogleHome)
                .toArray(Device[]::new);
    }

    public static Device toGoogleHome(DoorbellDevice doorbell) {
        return new Device.Builder()
                .setId(String.valueOf(doorbell.getId()))
                .setType(LOCK_DEVICE_TYPE)
                .addTrait(LOCK_UNLOCK_TRAIT)
                .setName(Collections.singletonList("Smart Doorbell-Buzzer"), doorbell.getName(), Collections.singletonList(doorbell.getName()))
                .setWillReportState(true)
                .build();
    }

    public static Map<String, Object> toGoogleHomeDeviceState(DoorbellDevice doorbell) {
        return Map.of(
                ONLINE_KEY, doorbell.getIsConnected(),
                STATUS_KEY, doorbell.getIsConnected() ? SUCCESS : OFFLINE,
                IS_LOCKED_KEY, !doorbell.getIsOpened(),
                IS_JAMMED_KEY, false
        );
    }

}
