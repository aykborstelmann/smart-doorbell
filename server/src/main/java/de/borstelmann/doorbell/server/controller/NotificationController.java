package de.borstelmann.doorbell.server.controller;


import de.borstelmann.doorbell.server.openapi.api.NotificationApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class NotificationController implements NotificationApi {
    @Override
    public ResponseEntity<Void> notify(Long doorbellId) {
        return null;
    }
}
