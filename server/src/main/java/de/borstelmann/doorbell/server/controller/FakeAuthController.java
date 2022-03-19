package de.borstelmann.doorbell.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/fakeauth")
public class FakeAuthController {

    @GetMapping
    public ResponseEntity<?> postFakeAuth(@RequestParam("redirect_uri") String redirectUri, @RequestParam("state") String state) {
        String finalRedirectUrl = String.format("%s?code=%s&state=%s", redirectUri, "xxxxxx", state);

        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .header("Location", finalRedirectUrl)
                .build();
    }

}