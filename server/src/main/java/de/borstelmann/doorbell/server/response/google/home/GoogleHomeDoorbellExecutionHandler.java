package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest;
import com.google.actions.api.smarthome.ExecuteResponse;
import de.borstelmann.doorbell.server.error.OfflineException;
import de.borstelmann.doorbell.server.error.ParameterNotAvailableException;
import de.borstelmann.doorbell.server.services.DoorbellBuzzerStateService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static de.borstelmann.doorbell.server.response.google.home.DeviceCommand.LockCommandParamsKeys.LOCK;

@Component
@RequiredArgsConstructor
public class GoogleHomeDoorbellExecutionHandler {

    private final DoorbellBuzzerStateService doorbellBuzzerStateService;

    public ExecuteResponse.Payload.Commands execute(GoogleHomeDoorbellDevice device, ExecuteRequest.Inputs.Payload.Commands.Execution execution) {
        DeviceCommand deviceCommand = DeviceCommand.fromCommandName(execution.getCommand());
        Map<String, Object> params = execution.getParams();

        try {
            return executeCommand(device, deviceCommand, params);
        } catch (OfflineException e) {
            return makeOfflineResponse(device.getId());
        } catch (UnsupportedOperationException | ParameterNotAvailableException e) {
            return makeExceptionResponse(device.getId());
        }
    }

    private ExecuteResponse.Payload.Commands executeCommand(GoogleHomeDoorbellDevice device, DeviceCommand command, @Nullable Map<String, Object> parameters) {
        if (command != DeviceCommand.LOCK_COMMAND) {
            throw new UnsupportedOperationException();
        }
        if (!device.getIsOnline()) {
            throw new OfflineException();
        }

        boolean shouldLock = getParameter(parameters, Boolean.class, LOCK);

        if (shouldLock) {
            doorbellBuzzerStateService.closeDoor(Long.valueOf(device.getId()));
        } else {
            doorbellBuzzerStateService.openDoor(Long.valueOf(device.getId()));
        }

        return GoogleHomeDoorbellExecutionHandler.makePendingResponse(device.getId());
    }


    private <T> T getParameter(Map<String, Object> parameters, Class<T> type, String key) {
        return Optional.ofNullable(parameters)
                .map(param -> param.get(key))
                .map(type::cast)
                .orElseThrow(ParameterNotAvailableException::new);
    }

    public static ExecuteResponse.Payload.Commands makePendingResponse(String id) {
        return makeResponse(DeviceStatus.PENDING, id);
    }

    public static ExecuteResponse.Payload.Commands makeOfflineResponse(String id) {
        ExecuteResponse.Payload.Commands commands = makeResponse(DeviceStatus.OFFLINE, id);
        commands.setStates(Map.of(GoogleHomeDoorbellDevice.ONLINE, false));
        return commands;
    }

    public static ExecuteResponse.Payload.Commands makeExceptionResponse(String id) {
        return makeResponse(DeviceStatus.EXCEPTIONS, id);
    }

    public static ExecuteResponse.Payload.Commands makeResponse(DeviceStatus status, String id) {
        ExecuteResponse.Payload.Commands commands = new ExecuteResponse.Payload.Commands();
        commands.status = status.toString();
        commands.ids = new String[]{id};
        return commands;
    }
}
