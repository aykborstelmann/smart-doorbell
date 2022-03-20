package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.openapi.api.DoorbellApi;
import de.borstelmann.doorbell.server.openapi.model.DoorbellRequest;
import de.borstelmann.doorbell.server.openapi.model.DoorbellResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1")
@RestController
public class DoorbellController implements DoorbellApi {

    @Override
    public ResponseEntity<DoorbellResponse> createDoorbell(Long userId, DoorbellRequest doorbellRequest) {
        return null;
    }

    @Override
    public ResponseEntity<List<DoorbellResponse>> getAllDoorbells(Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<DoorbellResponse> getDoorbell(Long doorbellId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteDoorbell(Long doorbellId) {
        return null;
    }

}
