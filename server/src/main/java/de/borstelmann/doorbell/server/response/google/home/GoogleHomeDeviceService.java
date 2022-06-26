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
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import de.borstelmann.doorbell.server.services.DoorbellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class GoogleHomeDeviceService {

    private final DoorbellService doorbellService;
    private final DoorbellDeviceRepository doorbellDeviceRepository;
    private final GoogleHomeDoorbellExecutionHandler executionHandler;
    private HomeGraphService homeGraphService;

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

    public void reportDeviceState(long deviceId) {
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

    @Autowired(required = false)
    public void setHomeGraphService(HomeGraphService homeGraphService) {
        this.homeGraphService = homeGraphService;
    }
}
