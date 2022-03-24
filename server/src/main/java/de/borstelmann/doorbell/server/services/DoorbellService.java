package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.error.BadRequestException;
import de.borstelmann.doorbell.server.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoorbellService {

    private final DoorbellDeviceRepository doorbellDeviceRepository;
    private final UserRepository userRepository;

    public DoorbellDevice createDoorbell(long userId, DoorbellDevice doorbellDevice) {
        return userRepository.findById(userId)
                .map(user -> setUserAndReturn(doorbellDevice, user))
                .map(doorbellDeviceRepository::save)
                .orElseThrow(() -> NotFoundException.createUserNotFoundException(userId));
    }

    public List<DoorbellDevice> getAllDoorbells(long userId) {
        return userRepository.findById(userId)
                .map(User::getDoorbellDevices)
                .orElseThrow(() -> NotFoundException.createUserNotFoundException(userId));
    }

    public DoorbellDevice getDoorbell(long doorbellId) {
        return doorbellDeviceRepository.findById(doorbellId)
                .orElseThrow(() -> NotFoundException.createDoorbellNotFoundException(doorbellId));
    }

    public void deleteDoorbell(long doorbellId) {
        DoorbellDevice doorbell = getDoorbell(doorbellId);
        doorbellDeviceRepository.delete(doorbell);
    }

    @NotNull
    private DoorbellDevice setUserAndReturn(DoorbellDevice doorbellDevice, User user) {
        doorbellDevice.setUser(user);
        return doorbellDevice;
    }
}
