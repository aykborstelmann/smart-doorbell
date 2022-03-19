package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.response.TokenResponse;
import de.borstelmann.doorbell.server.response.TokenResponse.TokenResponseBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/faketoken")
public class FakeTokenController {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> postFakeToken(@RequestParam("grant_type") String grantType) {
        TokenResponseBuilder tokenResponse = TokenResponse.builder()
                .tokenType("bearer")
                .accessToken("123access")
                .expiresIn(86400);

        if (grantType.equals("authorization_code")) {
            tokenResponse.refreshToken("123refresh");
        }

        return ResponseEntity.ok().body(tokenResponse.build());
    }

}
