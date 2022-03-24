package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.error.BadRequestException;
import de.borstelmann.doorbell.server.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings
class DoorbellServiceTest {

    private static final long USER_ID = 0L;
    private static final long DOORBELL_ID = 1L;
    private static final DoorbellDevice SAMPLE_DOORBELL = DoorbellDevice.builder()
            .id(DOORBELL_ID)
            .name("Doorbell")
            .build();
    private static final User SAMPLE_USER = User.builder()
            .id(USER_ID)
            .doorbellDevices(List.of(SAMPLE_DOORBELL))
            .name("User")
            .build();

    @Mock
    private DoorbellDeviceRepository doorbellDeviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DoorbellService doorbellService;


    @Test
    void testCreateDoorbell() {
        DoorbellDevice doorbellToSave = DoorbellDevice.builder()
                .name("Doorbell")
                .build();

        makeUserExist();

        doReturn(SAMPLE_DOORBELL).when(doorbellDeviceRepository).save(any());

        DoorbellDevice doorbell = doorbellService.createDoorbell(USER_ID, doorbellToSave);

        assertThat(doorbell).isEqualTo(SAMPLE_DOORBELL);
    }

    @Test
    void testCreateDoorbell_userNotFound() {
        DoorbellDevice doorbellToSave = DoorbellDevice.builder()
                .name("Doorbell")
                .build();

        makeUserNotExist();

        assertThatThrownBy(() -> doorbellService.createDoorbell(USER_ID, doorbellToSave))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with ID 0 not found");
    }

    @Test
    void testGetAllDoorbells() {
        makeUserExist();

        List<DoorbellDevice> doorbells = doorbellService.getAllDoorbells(USER_ID);

        assertThat(doorbells).isEqualTo(SAMPLE_USER.getDoorbellDevices());
    }

    @Test
    void testGetAllDoorbells_userNotFound() {
        makeUserNotExist();

        assertThatThrownBy(() -> doorbellService.getAllDoorbells(USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with ID 0 not found");

    }

    @Test
    void testGetDoorbell() {
        DoorbellDevice expectedDoorbell = DoorbellDevice.builder()
                .id(DOORBELL_ID)
                .build();
        makeDoorbellExist(expectedDoorbell);

        DoorbellDevice doorbell = doorbellService.getDoorbell(DOORBELL_ID);
        assertThat(doorbell).isEqualTo(expectedDoorbell);
    }


    @Test
    void testGetDoorbell_doorbellNotFound() {
        makeDoorbellNotExist();

        assertThatThrownBy(() -> doorbellService.getDoorbell(DOORBELL_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doorbell with ID 1 not found");
    }

    @Test
    void testDeleteDoorbell() {
        DoorbellDevice expectedDoorbell = DoorbellDevice.builder()
                .id(DOORBELL_ID)
                .build();
        makeDoorbellExist(expectedDoorbell);

        doorbellService.deleteDoorbell(DOORBELL_ID);

        verify(doorbellDeviceRepository).delete(expectedDoorbell);
    }

    @Test
    void testDeleteDoorbell_doorbellNotFound() {
        makeDoorbellNotExist();

        assertThatThrownBy(() -> doorbellService.deleteDoorbell(DOORBELL_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Doorbell with ID 1 not found");

        verify(doorbellDeviceRepository, times(0)).deleteById(DOORBELL_ID);
    }

    private void makeUserExist() {
        lenient().doReturn(Optional.of(SAMPLE_USER)).when(userRepository).findById(USER_ID);
        lenient().doReturn(true).when(userRepository).existsById(USER_ID);
    }

    private void makeUserNotExist() {
        lenient().doReturn(Optional.empty()).when(userRepository).findById(USER_ID);
        lenient().doReturn(false).when(userRepository).existsById(USER_ID);
    }

    private void makeDoorbellExist(DoorbellDevice sampleDoorbell) {
        doReturn(Optional.of(sampleDoorbell)).when(doorbellDeviceRepository).findById(DOORBELL_ID);
    }

    private void makeDoorbellNotExist() {
        doReturn(Optional.empty()).when(doorbellDeviceRepository).findById(DOORBELL_ID);
    }
}