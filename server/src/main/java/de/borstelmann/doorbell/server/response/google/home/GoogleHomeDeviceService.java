package de.borstelmann.doorbell.server.response.google.home;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.error.ForbiddenException;
import de.borstelmann.doorbell.server.services.DoorbellService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
        return doorbellDeviceRepository.findAllById(devices)
                .stream()
                .peek(doorbellDevice -> throwIfDoorbellDoesNotMatchUser(user, doorbellDevice))
                .map(GoogleHomeDoorbellDevice::fromDomainModelDevice)
                .toList();
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
}
