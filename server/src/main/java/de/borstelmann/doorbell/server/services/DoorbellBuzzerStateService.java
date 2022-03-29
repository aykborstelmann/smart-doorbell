package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.dto.OpenMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoorbellBuzzerStateService {
    private final DoorbellService doorbellService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void openDoor(Long doorbellId) {
        DoorbellDevice doorbell = doorbellService.getDoorbell(doorbellId);
        OpenMessage openMessage = new OpenMessage(5000);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(doorbell.getId()), "/commands/open", openMessage);
    }

    public void closeDoor(Long doorbellId) {
        DoorbellDevice doorbell = doorbellService.getDoorbell(doorbellId);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(doorbell.getId()), "/commands/close", "");
    }
}
