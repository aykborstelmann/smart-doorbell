package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest;
import com.google.actions.api.smarthome.ExecuteResponse;
import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationRequest;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.error.BadRequestException;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import de.borstelmann.doorbell.server.services.AuthenticationService;
import de.borstelmann.doorbell.server.services.DoorbellService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ContextConfiguration(classes = {GoogleHomeDeviceService.class, DoorbellService.class})
class GoogleHomeDeviceServiceTest {

    private static final long USER_ID = 0L;
    private static final long DOORBELL_ID = 1L;
    private DoorbellDevice sampleDoorbell;
    private User sampleUser;

    @MockBean
    private DoorbellDeviceRepository doorbellDeviceRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private HomeGraphService homeGraphService;

    @MockBean
    private GoogleHomeDoorbellExecutionHandler googleHomeDoorbellExecutionHandler;

    @MockBean
    private AuthenticationService authenticationService;

    @SpyBean
    private GoogleHomeDeviceService googleHomeDeviceService;

    @BeforeEach
    void setUp() {
        sampleDoorbell = DoorbellDevice.builder()
                .id(DOORBELL_ID)
                .name("Test")
                .build();

        sampleUser = User.builder()
                .id(USER_ID)
                .doorbellDevices(List.of(sampleDoorbell))
                .googleHomeConnected(true)
                .build();

        sampleDoorbell.setUser(sampleUser);
    }

    @Test
    void testGetAllDevicesForUser() {
        withSampleUserLoggedIn();

        doReturn(Optional.of(sampleUser)).when(userRepository).findById(USER_ID);

        List<? extends GoogleHomeDoorbellDevice> allDevicesForUser = googleHomeDeviceService.getAllDevicesForUser();
        assertThat(allDevicesForUser)
                .hasSize(1)
                .hasOnlyElementsOfType(GoogleHomeDoorbellDevice.class)
                .extracting(GoogleHomeDoorbellDevice::getId)
                .containsExactly(String.valueOf(sampleDoorbell.getId()));
        }


    @Test
    void testGetDevicesForUser() {
        withSampleUserLoggedIn();

        doReturn(List.of(sampleDoorbell)).when(doorbellDeviceRepository).findAllById(List.of(sampleDoorbell.getId()));

        List<? extends GoogleHomeDoorbellDevice> devicesForUser = googleHomeDeviceService.getDevicesForUser(List.of(DOORBELL_ID));
        assertThat(devicesForUser)
                .hasSize(1)
                .hasOnlyElementsOfType(GoogleHomeDoorbellDevice.class)
                .extracting(GoogleHomeDoorbellDevice::getId)
                .containsExactly(String.valueOf(sampleDoorbell.getId()));
    }

    @Test
    void testGetDevicesForUser_notBelongsToUser() {
        withSampleUserLoggedIn();

        User differentUser = User.builder().id(1L).build();
        sampleDoorbell.setUser(differentUser);
        doReturn(List.of(sampleDoorbell)).when(doorbellDeviceRepository).findAllById(List.of(sampleDoorbell.getId()));

        List<Long> doorbellIds = List.of(DOORBELL_ID);
        ForbiddenException forbiddenException = catchThrowableOfType(
                () -> googleHomeDeviceService.getDevicesForUser(doorbellIds),
                ForbiddenException.class
        );
        assertThat(forbiddenException.getId()).isEqualTo(DOORBELL_ID);
    }

    @Test
    void testGetDevice() {
        withSampleUserLoggedIn();

        doReturn(Optional.of(sampleDoorbell)).when(doorbellDeviceRepository).findById(DOORBELL_ID);

        GoogleHomeDoorbellDevice device = googleHomeDeviceService.getDevice(DOORBELL_ID);
        assertThat(device).isInstanceOf(GoogleHomeDoorbellDevice.class);
        assertThat(device.getId()).isEqualTo(String.valueOf(sampleDoorbell.getId()));
    }

    @Test
    void testExecute_empty() {
        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{};

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeDeviceService.execute(commands);
        assertThat(executeResult).isEmpty();

        verify(googleHomeDoorbellExecutionHandler, never()).execute(any(), any());
    }

    @Test
    void testExecute() {
        withSampleUserLoggedIn();

        ExecuteRequest.Inputs.Payload.Commands.Devices device = new ExecuteRequest.Inputs.Payload.Commands.Devices();
        device.id = "0";
        ExecuteRequest.Inputs.Payload.Commands.Execution execution = new ExecuteRequest.Inputs.Payload.Commands.Execution();

        ExecuteRequest.Inputs.Payload.Commands command = new ExecuteRequest.Inputs.Payload.Commands();
        command.setDevices(new ExecuteRequest.Inputs.Payload.Commands.Devices[]{device});
        command.setExecution(new ExecuteRequest.Inputs.Payload.Commands.Execution[]{execution});

        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{command};

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        doReturn(List.of(googleHomeDoorbellDevice)).when(googleHomeDeviceService).getDevicesForUser(List.of(0L));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeDeviceService.execute(commands);
        assertThat(executeResult)
                .hasSize(1);

        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, execution);
    }

    @Test
    void testExecute_multipleDevices() {
        withSampleUserLoggedIn();

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
        )).when(googleHomeDeviceService).getDevicesForUser(List.of(firstDeviceId, secondDeviceId));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeDeviceService.execute(commands);
        assertThat(executeResult)
                .hasSize(2);

        verify(googleHomeDoorbellExecutionHandler).execute(firstGoogleHomeDevice, execution);
        verify(googleHomeDoorbellExecutionHandler).execute(secondGoogleHomeDevice, execution);
    }

    @Test
    void testExecute_multipleCommands() {
        withSampleUserLoggedIn();

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
        doReturn(List.of(googleHomeDoorbellDevice)).when(googleHomeDeviceService).getDevicesForUser(List.of(0L));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeDeviceService.execute(commands);
        assertThat(executeResult)
                .hasSize(2);

        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, firstExecution);
        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, secondExecution);
    }

    @Test
    void testExecute_wrongUser() {
        withSampleUserLoggedIn();

        ExecuteRequest.Inputs.Payload.Commands.Devices device = new ExecuteRequest.Inputs.Payload.Commands.Devices();
        device.id = "0";
        ExecuteRequest.Inputs.Payload.Commands.Execution execution = new ExecuteRequest.Inputs.Payload.Commands.Execution();

        ExecuteRequest.Inputs.Payload.Commands command = new ExecuteRequest.Inputs.Payload.Commands();
        command.setDevices(new ExecuteRequest.Inputs.Payload.Commands.Devices[]{device});
        command.setExecution(new ExecuteRequest.Inputs.Payload.Commands.Execution[]{execution});

        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{command};
        doReturn(List.of(sampleDoorbell)).when(doorbellDeviceRepository).findAllById(List.of(0L));
        sampleDoorbell.setUser(User.builder().id(2L).build());

        List<ExecuteResponse.Payload.Commands> response = googleHomeDeviceService.execute(commands);
        assertThat(response)
                .hasSize(1)
                .first()
                .extracting(ExecuteResponse.Payload.Commands::getStatus)
                .isEqualTo("EXCEPTIONS");
    }

    @Test
    void testExecute_noUser() {
        withNoUserLoggedIn();

        ExecuteRequest.Inputs.Payload.Commands.Devices device = new ExecuteRequest.Inputs.Payload.Commands.Devices();
        device.id = "0";
        ExecuteRequest.Inputs.Payload.Commands.Execution execution = new ExecuteRequest.Inputs.Payload.Commands.Execution();

        ExecuteRequest.Inputs.Payload.Commands command = new ExecuteRequest.Inputs.Payload.Commands();
        command.setDevices(new ExecuteRequest.Inputs.Payload.Commands.Devices[]{device});
        command.setExecution(new ExecuteRequest.Inputs.Payload.Commands.Execution[]{execution});

        ExecuteRequest.Inputs.Payload.Commands[] commands = new ExecuteRequest.Inputs.Payload.Commands[]{command};
        doReturn(List.of(sampleDoorbell)).when(doorbellDeviceRepository).findAllById(List.of(sampleDoorbell.getId()));

        assertThatThrownBy(() -> googleHomeDeviceService.execute(commands)).isInstanceOf(BadRequestException.class);
        verify(googleHomeDoorbellExecutionHandler, never()).execute(any(), any());
    }

    @Test
    void testReportDeviceStateIfNessary_googleHomeEnabled() throws IOException {
        HomeGraphService.Devices devices = mock(HomeGraphService.Devices.class);
        HomeGraphService.Devices.ReportStateAndNotification reportStateAndNotification = mock(HomeGraphService.Devices.ReportStateAndNotification.class);

        doReturn(devices).when(homeGraphService).devices();
        doReturn(reportStateAndNotification).when(devices).reportStateAndNotification(any());
        ArgumentCaptor<ReportStateAndNotificationRequest> captor = ArgumentCaptor.forClass(ReportStateAndNotificationRequest.class);

        doReturn(Optional.of(sampleDoorbell)).when(doorbellDeviceRepository).findById(sampleDoorbell.getId());

        googleHomeDeviceService.reportDeviceStateIfNecessary(sampleDoorbell.getId());
        verify(devices).reportStateAndNotification(captor.capture());

        ReportStateAndNotificationRequest request = captor.getValue();
        assertThat(request.getRequestId()).isNotBlank();
        assertThat(request.getAgentUserId()).isEqualTo(sampleUser.getId().toString());

        assertThat(request.getPayload().getDevices().getStates())
                .extractingByKey(sampleDoorbell.getId().toString())
                .asInstanceOf(MAP)
                .contains(
                        entry(GoogleHomePayloadAttributes.IS_JAMMED, false),
                        entry(GoogleHomePayloadAttributes.IS_LOCKED, true),
                        entry(GoogleHomePayloadAttributes.ONLINE, false)
                );

    }

    @Test
    void testReportDeviceStateIfNessary_googleHomeDisabled() throws IOException {
        HomeGraphService.Devices devices = mock(HomeGraphService.Devices.class);
        HomeGraphService.Devices.ReportStateAndNotification reportStateAndNotification = mock(HomeGraphService.Devices.ReportStateAndNotification.class);

        doReturn(devices).when(homeGraphService).devices();
        doReturn(reportStateAndNotification).when(devices).reportStateAndNotification(any());

        sampleUser.setGoogleHomeConnected(false);
        doReturn(Optional.of(sampleDoorbell)).when(doorbellDeviceRepository).findById(sampleDoorbell.getId());

        googleHomeDeviceService.reportDeviceStateIfNecessary(sampleDoorbell.getId());
        verify(devices, never()).reportStateAndNotification(any());
    }

    private void withNoUserLoggedIn() {
        doThrow(new BadRequestException("Current session does not have a user")).when(authenticationService).getCurrentUserOrThrow();
    }


    private void withSampleUserLoggedIn() {
        doReturn(sampleUser).when(authenticationService).getCurrentUserOrThrow();
    }
}