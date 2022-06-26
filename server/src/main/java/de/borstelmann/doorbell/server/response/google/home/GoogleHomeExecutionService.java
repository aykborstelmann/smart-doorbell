package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest.Inputs.Payload.Commands;
import com.google.actions.api.smarthome.ExecuteRequest.Inputs.Payload.Commands.Execution;
import com.google.actions.api.smarthome.ExecuteResponse;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class GoogleHomeExecutionService {

    private final GoogleHomeDeviceService googleHomeDeviceService;
    private final GoogleHomeDoorbellExecutionHandler executionHandler;

    public List<ExecuteResponse.Payload.Commands> execute(Commands[] commands) {
        return Arrays.stream(commands)
                .map(this::executeCommand)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<ExecuteResponse.Payload.Commands> executeCommand(Commands command) {
        List<Long> deviceIds = getDeviceIds(command);
        Execution[] executions = command.getExecution();
        User user = CustomUserSession.getCurrentUserOrThrow();

        try {
            return googleHomeDeviceService.getDevicesForUser(user, deviceIds)
                    .stream()
                    .flatMap(device -> mapToExecutedCommands(executions, device))
                    .toList();
        } catch (ForbiddenException e) {
            return List.of(
                    GoogleHomeDoorbellExecutionHandler.makeExceptionResponse(String.valueOf(e.getId())));
        }
    }

    private Stream<ExecuteResponse.Payload.Commands> mapToExecutedCommands(Execution[] executions, GoogleHomeDoorbellDevice device) {
        return Arrays.stream(executions)
                .map(execution -> executionHandler.execute(device, execution));
    }

    private List<Long> getDeviceIds(Commands command) {
        return Arrays.stream(command.getDevices())
                .map(Commands.Devices::getId)
                .map(Long::parseLong)
                .toList();
    }

}
