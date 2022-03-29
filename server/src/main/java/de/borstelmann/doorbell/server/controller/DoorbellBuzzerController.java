package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.openapi.api.DoorbellBuzzerApi;
import de.borstelmann.doorbell.server.services.DoorbellBuzzerStateService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class DoorbellBuzzerController implements DoorbellBuzzerApi {

    private final DoorbellBuzzerStateService doorbellBuzzerStateService;
    private final ModelMapper modelMapper;

    @Override
    public ResponseEntity<Void> openDoor(Long doorbellId) {
        doorbellBuzzerStateService.openDoor(doorbellId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> closeDoor(Long doorbellId) {
        doorbellBuzzerStateService.closeDoor(doorbellId);
        return ResponseEntity.noContent().build();
    }

}
