package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest.Inputs.Payload.Commands.Execution;
import com.google.actions.api.smarthome.ExecuteResponse;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.services.DoorbellBuzzerStateService;
import de.borstelmann.doorbell.server.test.JUnit5ValidationFileAssertionsWithJsonFormatting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.mockito.Mockito.verify;

@SpringJUnitConfig
@ContextConfiguration(classes = GoogleHomeDoorbellExecutionHandler.class)
class GoogleHomeDoorbellExecutionHandlerTest implements JUnit5ValidationFileAssertionsWithJsonFormatting {

    @MockBean
    private DoorbellBuzzerStateService doorbellBuzzerStateService;

    @Autowired
    private GoogleHomeDoorbellExecutionHandler googleHomeDoorbellExecutionHandler;

    @Test
    void testExecute_lock() {
        DoorbellDevice doorbellDevice = DoorbellDevice.builder().id(0L).name("Name").isConnected(true).build();
        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        Execution execution = new Execution();
        execution.setCommand(GoogleHomeDoorbellDevice.LOCK_COMMAND);
        execution.setParams(Map.of(
                GoogleHomePayloadAttributes.LOCK, true
        ));

        ExecuteResponse.Payload.Commands execute = googleHomeDoorbellExecutionHandler.execute(googleHomeDoorbellDevice, execution);

        assertWithFormattedJsonFile(execute);
        verify(doorbellBuzzerStateService).closeDoor(doorbellDevice.getId());
    }

    @Test
    void testExecute_unlock() {
        DoorbellDevice doorbellDevice = DoorbellDevice.builder().id(0L).name("Name").isConnected(true).build();
        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        Execution execution = new Execution();
        execution.setCommand(GoogleHomeDoorbellDevice.LOCK_COMMAND);
        execution.setParams(Map.of(
                GoogleHomePayloadAttributes.LOCK, false
        ));

        ExecuteResponse.Payload.Commands execute = googleHomeDoorbellExecutionHandler.execute(googleHomeDoorbellDevice, execution);

        assertWithFormattedJsonFile(execute);
        verify(doorbellBuzzerStateService).openDoor(doorbellDevice.getId());
    }

    @Test
    void testExecute_missingParam() {
        DoorbellDevice doorbellDevice = DoorbellDevice.builder().id(0L).name("Name").isConnected(true).build();
        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        Execution execution = new Execution();
        execution.setCommand(GoogleHomeDoorbellDevice.LOCK_COMMAND);
        execution.setParams(Map.of());

        ExecuteResponse.Payload.Commands execute = googleHomeDoorbellExecutionHandler.execute(googleHomeDoorbellDevice, execution);

        assertWithFormattedJsonFile(execute);
    }


    @Test
    void testExecute_offline() {
        DoorbellDevice doorbellDevice = DoorbellDevice.builder().id(0L).name("Name").isConnected(false).build();
        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        Execution execution = new Execution();
        execution.setCommand(GoogleHomeDoorbellDevice.LOCK_COMMAND);
        execution.setParams(Map.of(
                GoogleHomePayloadAttributes.LOCK, false
        ));

        ExecuteResponse.Payload.Commands execute = googleHomeDoorbellExecutionHandler.execute(googleHomeDoorbellDevice, execution);

        assertWithFormattedJsonFile(execute);
    }


    @Test
    void testExecute_wrongCommand() {
        DoorbellDevice doorbellDevice = DoorbellDevice.builder().id(0L).name("Name").isConnected(false).build();
        GoogleHomeDoorbellDevice googleHomeDoorbellDevice = GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbellDevice);

        Execution execution = new Execution();
        execution.setCommand("action.devices.commands.WrongCommand");
        execution.setParams(Map.of(
                GoogleHomePayloadAttributes.LOCK, false
        ));

        ExecuteResponse.Payload.Commands execute = googleHomeDoorbellExecutionHandler.execute(googleHomeDoorbellDevice, execution);

        assertWithFormattedJsonFile(execute);
    }

}