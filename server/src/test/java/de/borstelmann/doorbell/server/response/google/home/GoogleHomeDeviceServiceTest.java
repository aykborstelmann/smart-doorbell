package de.borstelmann.doorbell.server.response.google.home;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import de.borstelmann.doorbell.server.services.DoorbellService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@SpringJUnitConfig
@ContextConfiguration(classes = {GoogleHomeDeviceService.class, DoorbellService.class})
class GoogleHomeDeviceServiceTest {

    private static final long USER_ID = 0L;
    private static final long DOORBELL_ID = 1L;
    private static final DoorbellDevice SAMPLE_DOORBELL = DoorbellDevice.builder()
            .id(DOORBELL_ID)
            .name("Test")
            .build();
    private static final User SAMPLE_USER = User.builder()
            .id(USER_ID)
            .doorbellDevices(List.of(SAMPLE_DOORBELL))
            .build();

    @MockBean
    private DoorbellDeviceRepository doorbellDeviceRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private GoogleHomeDeviceService googleHomeDeviceService;

    @Test
    void testGetAllByUser() {
        doReturn(Optional.of(SAMPLE_USER)).when(userRepository).findById(USER_ID);

        List<? extends GoogleHomeDoorbellDevice> allDevicesForUser = googleHomeDeviceService.getAllDevicesForUser(SAMPLE_USER);
        assertThat(allDevicesForUser)
                .satisfiesExactly((device) -> {
                    assertThat(device)
                            .isInstanceOf(GoogleHomeDoorbellDevice.class);

                    var googleHomeDoorbellDevice = (GoogleHomeDoorbellDevice) device;
                    assertThat(googleHomeDoorbellDevice.getId()).isEqualTo(String.valueOf(SAMPLE_DOORBELL.getId()));
                });
    }

    @Test
    void testGetDevicesForUser() {
        SAMPLE_DOORBELL.setUser(SAMPLE_USER);
        doReturn(List.of(SAMPLE_DOORBELL)).when(doorbellDeviceRepository).findAllById(List.of(SAMPLE_DOORBELL.getId()));

        List<? extends GoogleHomeDoorbellDevice> devicesForUser = googleHomeDeviceService.getDevicesForUser(SAMPLE_USER, List.of(DOORBELL_ID));
        assertThat(devicesForUser)
                .satisfiesExactly((device) -> {
                    assertThat(device)
                            .isInstanceOf(GoogleHomeDoorbellDevice.class);

                    var googleHomeDoorbellDevice = (GoogleHomeDoorbellDevice) device;
                    assertThat(googleHomeDoorbellDevice.getId()).isEqualTo(String.valueOf(SAMPLE_DOORBELL.getId()));
                });
    }

    @Test
    void testGetDevicesForUser_notBelongsToUser() {
        SAMPLE_DOORBELL.setUser(User.builder().id(1L).build());
        doReturn(List.of(SAMPLE_DOORBELL)).when(doorbellDeviceRepository).findAllById(List.of(SAMPLE_DOORBELL.getId()));

        ForbiddenException forbiddenException = catchThrowableOfType(() -> googleHomeDeviceService.getDevicesForUser(SAMPLE_USER, List.of(DOORBELL_ID)), ForbiddenException.class);
        assertThat(forbiddenException.getId()).isEqualTo(DOORBELL_ID);
    }

    @Test
    void testGetDevice() {
        doReturn(Optional.of(SAMPLE_DOORBELL)).when(doorbellDeviceRepository).findById(DOORBELL_ID);

        GoogleHomeDoorbellDevice device = googleHomeDeviceService.getDevice(DOORBELL_ID);
        assertThat(device).isInstanceOf(GoogleHomeDoorbellDevice.class);
        assertThat(device.getId()).isEqualTo(String.valueOf(SAMPLE_DOORBELL.getId()));
    }
}