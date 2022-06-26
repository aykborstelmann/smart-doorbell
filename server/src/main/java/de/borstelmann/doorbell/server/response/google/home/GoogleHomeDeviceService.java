package de.borstelmann.doorbell.server.response.google.home;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import de.borstelmann.doorbell.server.services.DoorbellService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

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

    public List<GoogleHomeDoorbellDevice> getAllDevicesForUser(User user) {
        return doorbellService.getAllDoorbells(user.getId())
                .stream()
                .map(GoogleHomeDoorbellDevice::fromDomainModelDevice)
                .toList();
    }

    public List<GoogleHomeDoorbellDevice> getDevicesForUser(User user, List<Long> devices) {
        List<DoorbellDevice> doorbells = doorbellDeviceRepository.findAllById(devices);
        validateUserPermissions(user, doorbells);

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

    public ReportStateAndNotificationRequest makeReportDeviceStateRequest(long deviceId) {
        var device = getDevice(deviceId);
        var doorbellEntity = doorbellService.getDoorbell(deviceId);

        var agentUserId = doorbellEntity
            .getUser()
            .getId()
            .toString();

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
            .setAgentUserId(agentUserId)
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
