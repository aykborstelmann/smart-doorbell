package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.SyncResponse;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;

import java.util.*;

import static de.borstelmann.doorbell.server.response.google.home.DeviceStatus.OFFLINE;
import static de.borstelmann.doorbell.server.response.google.home.DeviceStatus.SUCCESS;

public class GoogleHomeDoorbellDevice {

    public static final String TYPE = "action.devices.types.LOCK";
    public static final String LOCK_COMMAND = "action.devices.commands.LockUnlock";
    public static final String LOCK_UNLOCK_TRAIT = "action.devices.traits.LockUnlock";

    private static final List<String> DEFAULT_NAMES = Collections.singletonList("Smart Doorbell-Buzzer");
    private static final boolean WILL_REPORT_STATE = true;

    private String id;
    private String name;

    private String agentUserId;

    private DoorbellDevice doorbellDevice;

    private GoogleHomeDoorbellDevice() {
    }

    public static GoogleHomeDoorbellDevice fromDomainModelDevice(DoorbellDevice doorbellDevice) {
        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = new GoogleHomeDoorbellDevice();

        googleHomeDoorbellDevice.doorbellDevice = doorbellDevice;
        googleHomeDoorbellDevice.id = String.valueOf(doorbellDevice.getId());
        googleHomeDoorbellDevice.name = doorbellDevice.getName();
        googleHomeDoorbellDevice.agentUserId = retrieveAgentUserId(doorbellDevice);

        return googleHomeDoorbellDevice;
    }

    private static String retrieveAgentUserId(DoorbellDevice doorbellDevice) {
        return Optional
                .ofNullable(doorbellDevice.getUser())
                .map(User::getId)
                .map(String::valueOf)
                .orElse(null);
    }

    public SyncResponse.Payload.Device getSync() {
        return new SyncResponse.Payload.Device.Builder()
                .setType(TYPE)
                .setId(id)
                .setName(DEFAULT_NAMES, name, Collections.singletonList(name))
                .addTrait(LOCK_UNLOCK_TRAIT)
                .setWillReportState(WILL_REPORT_STATE).build();
    }

    public Map<String, Object> getQueryState() {
        Map<String, Object> state = new HashMap<>(getState());
        state.put(GoogleHomePayloadAttributes.STATUS, getIsOnline() ? SUCCESS : OFFLINE);
        return state;
    }

    public Map<String, Object> getState() {
        return Map.of(
                GoogleHomePayloadAttributes.IS_LOCKED, !doorbellDevice.getIsOpened(),
                GoogleHomePayloadAttributes.IS_JAMMED, false,
                GoogleHomePayloadAttributes.ONLINE, getIsOnline()
        );
    }

    public boolean getIsOnline() {
        return doorbellDevice.getIsConnected();
    }

    public String getId() {
        return this.id;
    }

    public String getAgentUserId() {
        return agentUserId;
    }

}
