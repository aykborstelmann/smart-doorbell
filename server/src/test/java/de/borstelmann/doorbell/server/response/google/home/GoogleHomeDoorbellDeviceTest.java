package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.SyncResponse;
import com.google.home.graph.v1.DeviceProto;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static de.borstelmann.doorbell.server.response.google.home.GoogleHomePayloadAttributes.IS_JAMMED;
import static de.borstelmann.doorbell.server.response.google.home.GoogleHomePayloadAttributes.IS_LOCKED;
import static de.borstelmann.doorbell.server.response.google.home.GoogleHomeDoorbellDevice.*;
import static org.assertj.core.api.Assertions.assertThat;

class GoogleHomeDoorbellDeviceTest {

    @Test
    void testFromDomainModelDevice() {
        long id = 0L;
        long userId = 1L;
        String name = "Name";

        DoorbellDevice doorbellDevice = DoorbellDevice.builder()
                .name(name)
                .id(id)
                .user(User.builder().id(userId).build())
                .build();

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        assertThat(googleHomeDoorbellDevice.getId()).isEqualTo("0");
        assertThat(googleHomeDoorbellDevice.getAgentUserId()).isEqualTo("1");
    }

    @Test
    void testIsOnline() {
        boolean isConnected = true;

        DoorbellDevice doorbellDevice = DoorbellDevice.builder()
                .isConnected(isConnected)
                .build();

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        assertThat(googleHomeDoorbellDevice.getIsOnline()).isEqualTo(isConnected);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void testGetState(boolean isConnected, boolean isOpened) {
        DoorbellDevice doorbellDevice = DoorbellDevice.builder()
                .isConnected(isConnected)
                .isOpened(isOpened)
                .build();

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        assertThat(googleHomeDoorbellDevice.getState())
                .containsEntry(IS_LOCKED, !isOpened)
                .containsEntry(IS_JAMMED, false)
                .containsEntry(GoogleHomePayloadAttributes.ONLINE, isConnected);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void testGetQueryState(boolean isConnected, boolean isOpened) {
        DoorbellDevice doorbellDevice = DoorbellDevice.builder()
                .isConnected(isConnected)
                .isOpened(isOpened)
                .build();

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        assertThat(googleHomeDoorbellDevice.getQueryState())
                .containsEntry(IS_LOCKED, !isOpened)
                .containsEntry(IS_JAMMED, false)
                .containsEntry(GoogleHomePayloadAttributes.ONLINE, isConnected)
                .containsEntry(GoogleHomePayloadAttributes.STATUS, isConnected ? DeviceStatus.SUCCESS : DeviceStatus.OFFLINE);
    }

    @Test
    void testSync() {
        long id = 0L;
        String name = "Name";

        DoorbellDevice doorbellDevice = DoorbellDevice.builder()
                .id(id)
                .name(name)
                .build();

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        SyncResponse.Payload.Device sync = googleHomeDoorbellDevice.getSync();
        DeviceProto.Device device = sync.getDevice();
        assertThat(device.getId()).isEqualTo(String.valueOf(id));
        assertThat(device.getTraitsList()).containsExactly(LOCK_UNLOCK_TRAIT);
        assertThat(device.getType()).isEqualTo(GoogleHomeDoorbellDevice.TYPE);

        assertThat(device.getName().getName()).isEqualTo(name);
        assertThat(device.getName().getDefaultNamesList()).containsExactly("Smart Doorbell-Buzzer");
        assertThat(device.getName().getNicknamesList()).containsExactly(name);

        assertThat(device.getWillReportState()).isTrue();
    }
}