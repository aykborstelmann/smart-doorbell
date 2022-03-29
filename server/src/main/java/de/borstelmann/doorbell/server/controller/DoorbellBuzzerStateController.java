package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.dto.StateMessage;
import de.borstelmann.doorbell.server.services.DoorbellStateChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class DoorbellBuzzerStateController {
    private final DoorbellStateChangeService doorbellStateChangeService;

    @MessageMapping("/state")
    public void setState(@Payload StateMessage stateMessage, Principal principal) {
        Long deviceId = Long.valueOf(principal.getName());
        doorbellStateChangeService.setIsOpened(deviceId, stateMessage.getIsOpened());
    }
}
