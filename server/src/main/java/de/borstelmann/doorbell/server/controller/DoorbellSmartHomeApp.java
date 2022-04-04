package de.borstelmann.doorbell.server.controller;

import com.google.actions.api.smarthome.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.home.graph.v1.HomeGraphApiServiceProto;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.error.BadRequestException;
import de.borstelmann.doorbell.server.services.DoorbellBuzzerStateService;
import de.borstelmann.doorbell.server.services.DoorbellService;
import de.borstelmann.doorbell.server.services.mapper.GoogleHomeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DoorbellSmartHomeApp extends SmartHomeApp {

    public static final String LOCK_COMMAND = "action.devices.commands.LockUnlock";

    private final DoorbellService doorbellService;
    private final DoorbellBuzzerStateService doorbellBuzzerStateService;


    @Autowired(required = false)
    public void setGoogleCredentials(GoogleCredentials googleCredentials) {
        setCredentials(googleCredentials);
    }

    @Override
    public void onDisconnect(@NotNull DisconnectRequest disconnectRequest, @Nullable Map<?, ?> map) {

    }

    @NotNull
    @Override
    public ExecuteResponse onExecute(@NotNull ExecuteRequest executeRequest, @Nullable Map<?, ?> map) {
        ExecuteResponse executeResponse = new ExecuteResponse();
        executeResponse.requestId = executeRequest.requestId;
        executeResponse.payload = new ExecuteResponse.Payload();


        List<ExecuteResponse.Payload.Commands> commandsResponse = new ArrayList<>();
        for (ExecuteRequest.Inputs.Payload.Commands command : getInputs(executeRequest).payload.commands) {
            for (ExecuteRequest.Inputs.Payload.Commands.Devices device : command.getDevices()) {
                for (ExecuteRequest.Inputs.Payload.Commands.Execution execution : command.getExecution()) {
                    Map<String, Object> states = executeCommand(device, execution);

                    ExecuteResponse.Payload.Commands commandResponse = new ExecuteResponse.Payload.Commands();
                    commandResponse.ids = new String[]{device.getId()};
                    commandResponse.status = GoogleHomeMapper.PENDING;

                    commandsResponse.add(commandResponse);
                }
            }
        }

        executeResponse.payload.setCommands(commandsResponse.toArray(ExecuteResponse.Payload.Commands[]::new));
        return executeResponse;
    }

    private Map<String, Object> executeCommand(ExecuteRequest.Inputs.Payload.Commands.Devices device, ExecuteRequest.Inputs.Payload.Commands.Execution execution) {
        if (LOCK_COMMAND.equals(execution.getCommand())) {
            Map<String, Object> params = execution.getParams();
            if (params == null) {
                return Map.of();
            }

            Optional.ofNullable(params.get(GoogleHomeMapper.LOCK_KEY))
                    .map(lock -> (Boolean) lock)
                    .ifPresent(lock -> executeOpenClose(device, lock));
        }

        return null;
    }

    private void executeOpenClose(ExecuteRequest.Inputs.Payload.Commands.Devices device, Boolean shouldLock) {
        if (shouldLock) {
            doorbellBuzzerStateService.closeDoor(Long.valueOf(device.getId()));
        } else {
            doorbellBuzzerStateService.openDoor(Long.valueOf(device.getId()));
        }
    }

    @NotNull
    @Override
    public QueryResponse onQuery(@NotNull QueryRequest queryRequest, @Nullable Map<?, ?> map) {
        QueryResponse queryResponse = new QueryResponse();
        queryResponse.requestId = queryRequest.requestId;

        QueryRequest.Inputs.Payload.Device[] devices = getInputs(queryRequest).payload.devices;
        queryResponse.payload = new QueryResponse.Payload();
        var deviceMap = Arrays.stream(devices)
                .map(device -> Long.parseLong(device.getId()))
                .distinct()
                .map(doorbellService::getDoorbell)
                .collect(Collectors.toMap(doorbellDevice -> String.valueOf(doorbellDevice.getId()), GoogleHomeMapper::toGoogleHomeDeviceState));
        queryResponse.payload.setDevices(deviceMap);
        return queryResponse;
    }

    @NotNull
    @Override
    public SyncResponse onSync(@NotNull SyncRequest syncRequest, @Nullable Map<?, ?> headers) {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.requestId = syncRequest.requestId;
        syncResponse.payload = new SyncResponse.Payload();

        User user = Optional.ofNullable(CustomUserSession.getCurrentUser())
                .orElseThrow(() -> new BadRequestException("Current session does not have a user"));

        syncResponse.payload.agentUserId = String.valueOf(user.getId());
        syncResponse.payload.devices = GoogleHomeMapper.toGoogleHome(doorbellService.getAllDoorbells(user.getId()));
        return syncResponse;
    }

    public void reportStateDoorbellState(DoorbellDevice doorbell) {
        DoorbellDevice doorbellEntity = doorbellService.getDoorbell(doorbell.getId());
        String agentUserId = String.valueOf(doorbellEntity.getUser().getId());

        Struct.Builder states = Struct.newBuilder()
                .putFields(GoogleHomeMapper.IS_LOCKED_KEY, Value.newBuilder().setBoolValue(!doorbellEntity.getIsOpened()).build())
                .putFields(GoogleHomeMapper.IS_JAMMED_KEY, Value.newBuilder().setBoolValue(false).build())
                .putFields(GoogleHomeMapper.ONLINE_KEY, Value.newBuilder().setBoolValue(doorbellEntity.getIsConnected()).build());

        HomeGraphApiServiceProto.ReportStateAndNotificationDevice.Builder deviceBuilder = HomeGraphApiServiceProto.ReportStateAndNotificationDevice.newBuilder()
                .setStates(Struct.newBuilder()
                        .putFields(String.valueOf(doorbell.getId()), Value.newBuilder()
                                .setStructValue(states)
                                .build())
                );

        HomeGraphApiServiceProto.ReportStateAndNotificationRequest reportStateRequest = HomeGraphApiServiceProto.ReportStateAndNotificationRequest.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setAgentUserId(agentUserId)
                .setPayload(HomeGraphApiServiceProto.StateAndNotificationPayload.newBuilder()
                        .setDevices(deviceBuilder))
                .build();

        HomeGraphApiServiceProto.ReportStateAndNotificationResponse reportStateAndNotificationResponse = reportState(reportStateRequest);
        log.info("Got response {}", reportStateAndNotificationResponse);
    }

    private QueryRequest.Inputs getInputs(@NotNull QueryRequest queryRequest) {
        SmartHomeRequest.RequestInputs firstInput = queryRequest.getInputs()[0];
        return (QueryRequest.Inputs) firstInput;
    }

    private ExecuteRequest.Inputs getInputs(@NotNull ExecuteRequest executeRequest) {
        SmartHomeRequest.RequestInputs input = executeRequest.getInputs()[0];
        return (ExecuteRequest.Inputs) input;
    }
}
