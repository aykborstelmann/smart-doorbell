package de.borstelmann.doorbell.server.response.google.home;

import com.google.actions.api.smarthome.ExecuteRequest;
import com.google.actions.api.smarthome.ExecuteResponse;
import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationDevice;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationRequest;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationResponse;
import com.google.api.services.homegraph.v1.model.StateAndNotificationPayload;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import de.borstelmann.doorbell.server.services.AuthenticationService;
import de.borstelmann.doorbell.server.services.DoorbellService;
import de.borstelmann.doorbell.server.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class GoogleHomeDeviceService {

    private final DoorbellService doorbellService;
    private final DoorbellDeviceRepository doorbellDeviceRepository;
    private final GoogleHomeDoorbellExecutionHandler executionHandler;
    private final AuthenticationService authenticationService;
    private final HomeGraphService homeGraphService;

    public List<GoogleHomeDoorbellDevice> getAllDevicesForUser() {
        User user = authenticationService.getCurrentUserOrThrow();
        List<DoorbellDevice> doorbells = doorbellService.getAllDoorbells(user.getId());
        return mapToGoogleHomeDoorbellDevice(doorbells);
    }

    public List<GoogleHomeDoorbellDevice> getDevicesForUser(List<Long> devices) {
        User user = authenticationService.getCurrentUserOrThrow();
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

        try {
            return getDevicesForUser(deviceIds)
                    .stream()
                    .flatMap(device -> executeExecutionsForDevice(executions, device))
                    .toList();
        } catch (ForbiddenException e) {
            return List.of(GoogleHomeExecuteResponseUtil.makeExceptionResponse(String.valueOf(e.getId())));
        }
    }

    private Stream<ExecuteResponse.Payload.Commands> executeExecutionsForDevice(ExecuteRequest.Inputs.Payload.Commands.Execution[] executions, GoogleHomeDoorbellDevice device) {
        return Arrays.stream(executions)
                .map(execution -> executionHandler.execute(device, execution));
    }

    private List<Long> getDeviceIds(ExecuteRequest.Inputs.Payload.Commands command) {
        return Arrays.stream(command.getDevices())
                .map(ExecuteRequest.Inputs.Payload.Commands.Devices::getId)
                .map(Long::parseLong)
                .toList();
    }

    public void reportDeviceStateIfNecessary(long deviceId) {
        if (!isGoogleHomeConnected(deviceId)) return;
        var device = getDevice(deviceId);

        var devicePayload = new ReportStateAndNotificationDevice()
                .setStates(Map.of(
                        String.valueOf(deviceId), device.getState()
                ));

        ReportStateAndNotificationRequest payload = new ReportStateAndNotificationRequest()
                .setRequestId(UUID
                        .randomUUID()
                        .toString())
                .setAgentUserId(device.getAgentUserId())
                .setPayload(new StateAndNotificationPayload().setDevices(devicePayload));

        try {
            HomeGraphService.Devices.ReportStateAndNotification request = homeGraphService.devices().reportStateAndNotification(payload);
            ReportStateAndNotificationResponse response = request.execute();
            log.debug("Got home graph api response {}", response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isGoogleHomeConnected(long deviceId) {
        DoorbellDevice doorbell = doorbellService.getDoorbell(deviceId);
        return doorbell.getUser().isGoogleHomeConnected();
    }

}
