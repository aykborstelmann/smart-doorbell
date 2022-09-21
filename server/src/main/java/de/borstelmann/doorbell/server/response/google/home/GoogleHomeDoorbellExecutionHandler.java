package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest;
import com.google.actions.api.smarthome.ExecuteResponse;
import de.borstelmann.doorbell.server.services.DoorbellBuzzerStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoogleHomeDoorbellExecutionHandler {
    private final DoorbellBuzzerStateService doorbellBuzzerStateService;

    public ExecuteResponse.Payload.Commands execute(GoogleHomeDoorbellDevice device, ExecuteRequest.Inputs.Payload.Commands.Execution execution) {
        Map<String, Object> params = execution.getParams();

        if (!GoogleHomeDoorbellDevice.LOCK_COMMAND.equals(execution.getCommand())) {
            return GoogleHomeExecuteResponseUtil.makeExceptionResponse(device.getId());
        }
        if (!device.getIsOnline()) {
            return GoogleHomeExecuteResponseUtil.makeOfflineResponse(device.getId());
        }

        Optional<Boolean> lockParameter = getLockParameter(params);
        if (lockParameter.isEmpty()) {
            return GoogleHomeExecuteResponseUtil.makeExceptionResponse(device.getId());
        }

        boolean shouldLock = lockParameter.get();
        if (shouldLock) {
            doorbellBuzzerStateService.closeDoor(Long.valueOf(device.getId()));
        } else {
            doorbellBuzzerStateService.openDoor(Long.valueOf(device.getId()));
        }

        return GoogleHomeExecuteResponseUtil.makePendingResponse(device.getId());
    }


    private Optional<Boolean> getLockParameter(Map<String, Object> parameters) {
        return Optional.ofNullable(parameters)
                .map(param -> param.get(GoogleHomePayloadAttributes.LOCK))
                .map(Boolean.class::cast);
    }

}
