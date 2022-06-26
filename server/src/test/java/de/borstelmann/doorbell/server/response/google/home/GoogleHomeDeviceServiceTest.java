package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest;
import com.google.actions.api.smarthome.ExecuteResponse;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.error.BadRequestException;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import de.borstelmann.doorbell.server.services.DoorbellService;
import de.borstelmann.doorbell.server.test.authentication.WithMockOAuth2Scope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @MockBean
    private GoogleHomeDoorbellExecutionHandler googleHomeDoorbellExecutionHandler;

    @SpyBean
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

    @Test
    @WithMockOAuth2Scope
    void testExecute_empty() {
        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{};

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeDeviceService.execute(commands);
        assertThat(executeResult).isEmpty();

        verify(googleHomeDoorbellExecutionHandler, never()).execute(any(), any());
    }

    @Test
    @WithMockOAuth2Scope
    void testExecute() {
        ExecuteRequest.Inputs.Payload.Commands.Devices device = new ExecuteRequest.Inputs.Payload.Commands.Devices();
        device.id = "0";
        ExecuteRequest.Inputs.Payload.Commands.Execution execution = new ExecuteRequest.Inputs.Payload.Commands.Execution();

        ExecuteRequest.Inputs.Payload.Commands command = new ExecuteRequest.Inputs.Payload.Commands();
        command.setDevices(new ExecuteRequest.Inputs.Payload.Commands.Devices[]{device});
        command.setExecution(new ExecuteRequest.Inputs.Payload.Commands.Execution[]{execution});

        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{command};

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        doReturn(List.of(googleHomeDoorbellDevice)).when(googleHomeDeviceService).getDevicesForUser(userWithId(), eq(List.of(0L)));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeDeviceService.execute(commands);
        assertThat(executeResult)
                .hasSize(1);

        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, execution);
    }

    @Test
    @WithMockOAuth2Scope
    void testExecute_multipleDevices() {
        long firstDeviceId = 0L;
        long secondDeviceId = 1L;

        ExecuteRequest.Inputs.Payload.Commands.Devices firstDevice = new ExecuteRequest.Inputs.Payload.Commands.Devices();
        firstDevice.id = String.valueOf(firstDeviceId);
        ExecuteRequest.Inputs.Payload.Commands.Devices secondDevice = new ExecuteRequest.Inputs.Payload.Commands.Devices();
        secondDevice.id = String.valueOf(secondDeviceId);

        ExecuteRequest.Inputs.Payload.Commands.Execution execution = new ExecuteRequest.Inputs.Payload.Commands.Execution();

        ExecuteRequest.Inputs.Payload.Commands command = new ExecuteRequest.Inputs.Payload.Commands();
        command.setDevices(new ExecuteRequest.Inputs.Payload.Commands.Devices[]{firstDevice, secondDevice});
        command.setExecution(new ExecuteRequest.Inputs.Payload.Commands.Execution[]{execution});

        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{command};

        GoogleHomeDoorbellDevice firstGoogleHomeDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        GoogleHomeDoorbellDevice secondGoogleHomeDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());


        doReturn(List.of(
                firstGoogleHomeDevice,
                secondGoogleHomeDevice
        )).when(googleHomeDeviceService).getDevicesForUser(userWithId(), eq(List.of(firstDeviceId, secondDeviceId)));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeDeviceService.execute(commands);
        assertThat(executeResult)
                .hasSize(2);

        verify(googleHomeDoorbellExecutionHandler).execute(firstGoogleHomeDevice, execution);
        verify(googleHomeDoorbellExecutionHandler).execute(secondGoogleHomeDevice, execution);
    }

    @Test
    @WithMockOAuth2Scope
    void testExecute_multipleCommands() {
        ExecuteRequest.Inputs.Payload.Commands.Devices device = new ExecuteRequest.Inputs.Payload.Commands.Devices();
        device.id = "0";
        ExecuteRequest.Inputs.Payload.Commands.Execution firstExecution = new ExecuteRequest.Inputs.Payload.Commands.Execution();
        ExecuteRequest.Inputs.Payload.Commands.Execution secondExecution = new ExecuteRequest.Inputs.Payload.Commands.Execution();

        ExecuteRequest.Inputs.Payload.Commands firstCommand = new ExecuteRequest.Inputs.Payload.Commands();
        firstCommand.setDevices(new ExecuteRequest.Inputs.Payload.Commands.Devices[]{device});
        firstCommand.setExecution(new ExecuteRequest.Inputs.Payload.Commands.Execution[]{firstExecution});

        ExecuteRequest.Inputs.Payload.Commands secondCommand = new ExecuteRequest.Inputs.Payload.Commands();
        secondCommand.setDevices(new ExecuteRequest.Inputs.Payload.Commands.Devices[]{device});
        secondCommand.setExecution(new ExecuteRequest.Inputs.Payload.Commands.Execution[]{secondExecution});

        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{firstCommand, secondCommand};

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        doReturn(List.of(googleHomeDoorbellDevice)).when(googleHomeDeviceService).getDevicesForUser(userWithId(), eq(List.of(0L)));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeDeviceService.execute(commands);
        assertThat(executeResult)
                .hasSize(2);

        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, firstExecution);
        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, secondExecution);
    }

    @Test
    void testExecute_noUser() {
        ExecuteRequest.Inputs.Payload.Commands.Devices device = new ExecuteRequest.Inputs.Payload.Commands.Devices();
        device.id = "0";
        ExecuteRequest.Inputs.Payload.Commands.Execution execution = new ExecuteRequest.Inputs.Payload.Commands.Execution();

        ExecuteRequest.Inputs.Payload.Commands command = new ExecuteRequest.Inputs.Payload.Commands();
        command.setDevices(new ExecuteRequest.Inputs.Payload.Commands.Devices[]{device});
        command.setExecution(new ExecuteRequest.Inputs.Payload.Commands.Execution[]{execution});

        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{command};

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        doReturn(List.of(googleHomeDoorbellDevice)).when(googleHomeDeviceService).getDevicesForUser(userWithId(), eq(List.of(0L)));

        assertThatThrownBy(() -> googleHomeDeviceService.execute(commands)).isInstanceOf(BadRequestException.class);

        verify(googleHomeDoorbellExecutionHandler, never()).execute(googleHomeDoorbellDevice, execution);
    }

    private User userWithId() {
        return argThat(user -> user.getId() == WithMockOAuth2Scope.DEFAULT_USER_ID);
    }
}