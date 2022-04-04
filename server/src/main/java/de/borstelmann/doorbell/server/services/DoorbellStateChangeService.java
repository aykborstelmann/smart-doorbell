package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.controller.DoorbellSmartHomeApp;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoorbellStateChangeService {
    private final DoorbellSmartHomeApp doorbellSmartHomeApp;
    private final DoorbellDeviceRepository doorbellDeviceRepository;

    public void setIsOpened(Long deviceId, boolean isOpened) {
        doorbellDeviceRepository.findById(deviceId)
                .ifPresent(doorbell -> {
                    doorbell.setIsOpened(isOpened);
                    doorbellDeviceRepository.save(doorbell);
                    doorbellSmartHomeApp.reportStateDoorbellState(doorbell);
                });
    }

    public void setIsConnected(Long deviceId, boolean isConnected) {
        doorbellDeviceRepository.findById(deviceId)
                .ifPresent(doorbell -> {
                    doorbell.setIsConnected(isConnected);
                    doorbellDeviceRepository.save(doorbell);
                });
    }
}
