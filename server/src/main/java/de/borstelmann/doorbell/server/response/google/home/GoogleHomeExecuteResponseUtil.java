package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteResponse;

import java.util.Map;

public class GoogleHomeExecuteResponseUtil {
    private GoogleHomeExecuteResponseUtil() {
    }

    public static ExecuteResponse.Payload.Commands makePendingResponse(String id) {
        return makeResponse(DeviceStatus.PENDING, id);
    }

    public static ExecuteResponse.Payload.Commands makeOfflineResponse(String id) {
        ExecuteResponse.Payload.Commands commands = makeResponse(DeviceStatus.OFFLINE, id);
        commands.setStates(Map.of(GoogleHomePayloadAttributes.ONLINE, false));
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
