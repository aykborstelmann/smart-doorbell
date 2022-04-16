package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest.Inputs.Payload.Commands;
import com.google.actions.api.smarthome.ExecuteRequest.Inputs.Payload.Commands.Devices;
import com.google.actions.api.smarthome.ExecuteRequest.Inputs.Payload.Commands.Execution;
import com.google.actions.api.smarthome.ExecuteResponse;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.error.BadRequestException;
import de.borstelmann.doorbell.server.test.authentication.WithMockOAuth2Scope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ContextConfiguration(classes = GoogleHomeExecutionService.class)
class GoogleHomeExecutionServiceTest {

    @MockBean
    private GoogleHomeDoorbellExecutionHandler googleHomeDoorbellExecutionHandler;

    @MockBean
    private GoogleHomeDeviceService googleHomeDeviceService;

    @Autowired
    private GoogleHomeExecutionService googleHomeExecutionService;

    @Test
    @WithMockOAuth2Scope
    void testExecute_empty() {
        Commands[] commands = new Commands[]{};

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeExecutionService.execute(commands);
        assertThat(executeResult).isEmpty();

        verify(googleHomeDoorbellExecutionHandler, never()).execute(any(), any());
    }

    @Test
    @WithMockOAuth2Scope
    void testExecute() {
        Devices device = new Devices();
        device.id = "0";
        Execution execution = new Execution();

        Commands command = new Commands();
        command.setDevices(new Devices[]{device});
        command.setExecution(new Execution[]{execution});

        Commands[] commands = new Commands[]{command};

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        doReturn(List.of(googleHomeDoorbellDevice)).when(googleHomeDeviceService).getDevicesForUser(userWithId(), eq(List.of(0L)));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeExecutionService.execute(commands);
        assertThat(executeResult)
                .hasSize(1);

        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, execution);
    }

    @Test
    @WithMockOAuth2Scope
    void testExecute_multipleDevices() {
        long firstDeviceId = 0L;
        long secondDeviceId = 1L;

        Devices firstDevice = new Devices();
        firstDevice.id = String.valueOf(firstDeviceId);
        Devices secondDevice = new Devices();
        secondDevice.id = String.valueOf(secondDeviceId);

        Execution execution = new Execution();

        Commands command = new Commands();
        command.setDevices(new Devices[]{firstDevice, secondDevice});
        command.setExecution(new Execution[]{execution});

        Commands[] commands = new Commands[]{command};

        GoogleHomeDoorbellDevice firstGoogleHomeDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        GoogleHomeDoorbellDevice secondGoogleHomeDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());


        doReturn(List.of(
                firstGoogleHomeDevice,
                secondGoogleHomeDevice
        )).when(googleHomeDeviceService).getDevicesForUser(userWithId(), eq(List.of(firstDeviceId, secondDeviceId)));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeExecutionService.execute(commands);
        assertThat(executeResult)
                .hasSize(2);

        verify(googleHomeDoorbellExecutionHandler).execute(firstGoogleHomeDevice, execution);
        verify(googleHomeDoorbellExecutionHandler).execute(secondGoogleHomeDevice, execution);
    }

    @Test
    @WithMockOAuth2Scope
    void testExecute_multipleCommands() {
        Devices device = new Devices();
        device.id = "0";
        Execution firstExecution = new Execution();
        Execution secondExecution = new Execution();

        Commands firstCommand = new Commands();
        firstCommand.setDevices(new Devices[]{device});
        firstCommand.setExecution(new Execution[]{firstExecution});

        Commands secondCommand = new Commands();
        secondCommand.setDevices(new Devices[]{device});
        secondCommand.setExecution(new Execution[]{secondExecution});

        Commands[] commands = new Commands[]{firstCommand, secondCommand};

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        doReturn(List.of(googleHomeDoorbellDevice)).when(googleHomeDeviceService).getDevicesForUser(userWithId(), eq(List.of(0L)));

        List<ExecuteResponse.Payload.Commands> executeResult = googleHomeExecutionService.execute(commands);
        assertThat(executeResult)
                .hasSize(2);

        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, firstExecution);
        verify(googleHomeDoorbellExecutionHandler).execute(googleHomeDoorbellDevice, secondExecution);
    }

    @Test
    void testExecute_noUser() {
        Devices device = new Devices();
        device.id = "0";
        Execution execution = new Execution();

        Commands command = new Commands();
        command.setDevices(new Devices[]{device});
        command.setExecution(new Execution[]{execution});

        Commands[] commands = new Commands[]{command};

        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(new DoorbellDevice());
        doReturn(List.of(googleHomeDoorbellDevice)).when(googleHomeDeviceService).getDevicesForUser(userWithId(), eq(List.of(0L)));

        assertThatThrownBy(() -> googleHomeExecutionService.execute(commands)).isInstanceOf(BadRequestException.class);

        verify(googleHomeDoorbellExecutionHandler, never()).execute(googleHomeDoorbellDevice, execution);
    }

    private User userWithId() {
        return argThat(user -> user.getId() == WithMockOAuth2Scope.DEFAULT_USER_ID);
    }
}