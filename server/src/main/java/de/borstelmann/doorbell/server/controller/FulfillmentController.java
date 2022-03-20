package de.borstelmann.doorbell.server.controller;

import com.google.actions.api.smarthome.SmartHomeApp;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/fulfillment")
public class FulfillmentController {

    private final SmartHomeApp smartHomeApp;

    @CrossOrigin(origins = "*")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fulfillment(@RequestBody String requestBody, @RequestHeader Map<?, ?> requestHeaders) throws ExecutionException, InterruptedException {
        final String result = smartHomeApp.handleRequest(requestBody, requestHeaders).get();
        return ResponseEntity
                .ok()
                .body(result);
    }
}
