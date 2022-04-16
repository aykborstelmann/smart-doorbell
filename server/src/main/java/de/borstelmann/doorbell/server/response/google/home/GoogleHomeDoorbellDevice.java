package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.SyncResponse;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;

import java.util.*;

import static de.borstelmann.doorbell.server.response.google.home.DeviceStatus.OFFLINE;
import static de.borstelmann.doorbell.server.response.google.home.DeviceStatus.SUCCESS;
import static de.borstelmann.doorbell.server.response.google.home.DeviceTrait.LockUnlockStateKeys.IS_JAMMED;
import static de.borstelmann.doorbell.server.response.google.home.DeviceTrait.LockUnlockStateKeys.IS_LOCKED;

public class GoogleHomeDoorbellDevice {

    public static final String ONLINE = "online";
    public static final String STATUS = "status";

    private static final Set<DeviceTrait> TRAITS = Set.of(DeviceTrait.LOCK_UNLOCK);
    private static final DeviceType TYPE = DeviceType.LOCK;
    private static final List<String> DEFAULT_NAMES = Collections.singletonList("Smart Doorbell-Buzzer");
    private static final boolean WILL_REPORT_STATE = true;

    private String id;
    private String name;

    private DoorbellDevice doorbellDevice;

    private GoogleHomeDoorbellDevice() {
    }

    public static GoogleHomeDoorbellDevice fromDomainModelDevice(DoorbellDevice doorbellDevice) {
        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = new GoogleHomeDoorbellDevice();

        googleHomeDoorbellDevice.doorbellDevice = doorbellDevice;
        googleHomeDoorbellDevice.id = String.valueOf(doorbellDevice.getId());
        googleHomeDoorbellDevice.name = doorbellDevice.getName();

        return googleHomeDoorbellDevice;
    }

    public SyncResponse.Payload.Device getSync() {
        SyncResponse.Payload.Device.Builder builder = new SyncResponse.Payload.Device.Builder()
                .setType(TYPE.toString())
                .setId(id)
                .setName(DEFAULT_NAMES, name, Collections.singletonList(name))
                .setWillReportState(WILL_REPORT_STATE);

        TRAITS.stream()
                .map(DeviceTrait::toString)
                .forEach(builder::addTrait);

        return builder.build();
    }

    public Map<String, Object> getQueryState() {
        Map<String, Object> state = new HashMap<>(getState());
        state.put(STATUS, getIsOnline() ? SUCCESS : OFFLINE);
        return state;
    }

    public Map<String, Object> getState() {
        return Map.of(
                IS_LOCKED, !doorbellDevice.getIsOpened(),
                IS_JAMMED, false,
                ONLINE, getIsOnline()
        );
    }

    public boolean getIsOnline() {
        return doorbellDevice.getIsConnected();
    }

    public String getId() {
        return this.id;
    }
}
