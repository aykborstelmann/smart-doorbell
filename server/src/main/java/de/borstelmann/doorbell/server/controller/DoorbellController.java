package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.openapi.api.DoorbellApi;
import de.borstelmann.doorbell.server.openapi.model.DoorbellRequest;
import de.borstelmann.doorbell.server.openapi.model.DoorbellResponse;
import de.borstelmann.doorbell.server.services.DoorbellService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class DoorbellController implements DoorbellApi {

    private final DoorbellService doorbellService;
    private final ModelMapper modelMapper;

    @Override
    public ResponseEntity<DoorbellResponse> createDoorbell(Long userId, DoorbellRequest doorbellRequest) {
        DoorbellDevice doorbellDevice = convertToDomainModel(doorbellRequest);
        DoorbellDevice doorbell = doorbellService.createDoorbell(userId, doorbellDevice);
        DoorbellResponse doorbellResponse = convertToResponse(doorbell);
        return ResponseEntity.ok(doorbellResponse);
    }

    @Override
    public ResponseEntity<List<DoorbellResponse>> getAllDoorbells(Long userId) {
        List<DoorbellDevice> doorbells = doorbellService.getAllDoorbells(userId);
        List<DoorbellResponse> doorbellResponses = doorbells.stream()
                .map(this::convertToResponse)
                .toList();
        return ResponseEntity.ok(doorbellResponses);
    }

    @Override
    public ResponseEntity<DoorbellResponse> getDoorbell(Long doorbellId) {
        DoorbellDevice doorbell = doorbellService.getDoorbell(doorbellId);
        DoorbellResponse doorbellResponse = convertToResponse(doorbell);
        return ResponseEntity.ok(doorbellResponse);
    }

    @Override
    public ResponseEntity<Void> deleteDoorbell(Long doorbellId) {
        doorbellService.deleteDoorbell(doorbellId);
        return ResponseEntity.noContent().build();
    }

    private DoorbellResponse convertToResponse(DoorbellDevice doorbell) {
        return modelMapper.map(doorbell, DoorbellResponse.class);
    }

    private DoorbellDevice convertToDomainModel(DoorbellRequest doorbellRequest) {
        return modelMapper.map(doorbellRequest, DoorbellDevice.class);
    }

}
