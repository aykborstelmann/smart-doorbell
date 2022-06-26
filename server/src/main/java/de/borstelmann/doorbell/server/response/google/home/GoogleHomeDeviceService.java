package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest;
import com.google.actions.api.smarthome.ExecuteResponse;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import de.borstelmann.doorbell.server.services.DoorbellService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.home.graph.v1.HomeGraphApiServiceProto.ReportStateAndNotificationDevice;
import com.google.home.graph.v1.HomeGraphApiServiceProto.ReportStateAndNotificationRequest;
import com.google.home.graph.v1.HomeGraphApiServiceProto.StateAndNotificationPayload;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

@Component
@RequiredArgsConstructor
public class GoogleHomeDeviceService {

    private final DoorbellService doorbellService;
    private final DoorbellDeviceRepository doorbellDeviceRepository;
    private final GoogleHomeDoorbellExecutionHandler executionHandler;

    public List<GoogleHomeDoorbellDevice> getAllDevicesForUser(User user) {
        List<DoorbellDevice> doorbells = doorbellService.getAllDoorbells(user.getId());
        return mapToGoogleHomeDoorbellDevice(doorbells);
    }

    public List<GoogleHomeDoorbellDevice> getDevicesForUser(User user, List<Long> devices) {
        List<DoorbellDevice> doorbells = doorbellDeviceRepository.findAllById(devices);
        validateUserPermissions(user, doorbells);
        return mapToGoogleHomeDoorbellDevice(doorbells);
    }

    private List<GoogleHomeDoorbellDevice> mapToGoogleHomeDoorbellDevice(List<DoorbellDevice> doorbells) {
        return doorbells
                .stream()
                .map(GoogleHomeDoorbellDevice::fromDomainModelDevice)
                .toList();
    }

    private void validateUserPermissions(User user, List<DoorbellDevice> doorbells) {
        doorbells.forEach(doorbellDevice -> throwIfDoorbellDoesNotMatchUser(user, doorbellDevice));
    }

    private void throwIfDoorbellDoesNotMatchUser(User user, DoorbellDevice doorbellDevice) {
        if (!doorbellDevice.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(doorbellDevice.getId());
        }
    }

    public GoogleHomeDoorbellDevice getDevice(Long id) {
        DoorbellDevice doorbell = doorbellService.getDoorbell(id);
        return GoogleHomeDoorbellDevice.fromDomainModelDevice(doorbell);
    }

    public List<ExecuteResponse.Payload.Commands> execute(ExecuteRequest.Inputs.Payload.Commands[] commands) {
        return Arrays.stream(commands)
                .map(this::executeCommand)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<ExecuteResponse.Payload.Commands> executeCommand(ExecuteRequest.Inputs.Payload.Commands command) {
        List<Long> deviceIds = getDeviceIds(command);
        ExecuteRequest.Inputs.Payload.Commands.Execution[] executions = command.getExecution();
        User user = CustomUserSession.getCurrentUserOrThrow();

        try {
            return getDevicesForUser(user, deviceIds)
                    .stream()
                    .flatMap(device ->
                            Arrays.stream(executions)
                                    .map(execution -> executionHandler.execute(device, execution)))
                    .toList();
        } catch (ForbiddenException e) {
            return List.of(GoogleHomeExecuteResponseUtil.makeExceptionResponse(String.valueOf(e.getId())));
        }
    }

    private List<Long> getDeviceIds(ExecuteRequest.Inputs.Payload.Commands command) {
        return Arrays.stream(command.getDevices())
                .map(ExecuteRequest.Inputs.Payload.Commands.Devices::getId)
                .map(Long::parseLong)
                .toList();
    }

    public ReportStateAndNotificationRequest makeReportDeviceStateRequest(long deviceId) {
        var device = getDevice(deviceId);

        var states = Struct.newBuilder();

        device
            .getState()
            .forEach((key, value) -> states.putFields(key, makeValue(value)));

        var deviceBuilder = ReportStateAndNotificationDevice
            .newBuilder()
            .setStates(Struct
                .newBuilder()
                .putFields(String.valueOf(deviceId), Value
                    .newBuilder()
                    .setStructValue(states)
                    .build())
            );

        return ReportStateAndNotificationRequest
            .newBuilder()
            .setRequestId(UUID
                .randomUUID()
                .toString())
            .setAgentUserId(device.getAgentUserId())
            .setPayload(StateAndNotificationPayload
                .newBuilder()
                .setDevices(deviceBuilder))
            .build();
    }

    private Value makeValue(Object value) {
        if (value instanceof Boolean bool) {
            return Value
                .newBuilder()
                .setBoolValue(bool)
                .build();
        }
        throw new IllegalArgumentException("Unknown value type %s".formatted(value.getClass()));
    }

}
