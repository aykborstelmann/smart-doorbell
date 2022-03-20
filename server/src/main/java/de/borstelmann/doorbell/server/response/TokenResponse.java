package de.borstelmann.doorbell.server.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder()
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    @JsonProperty(value = "token_type")
    private String tokenType;
    @JsonProperty(value = "access_token")
    private String accessToken;
    @JsonProperty(value = "expires_in")
    private int expiresIn;
    @JsonProperty(value = "refresh_token")
    private String refreshToken;
}
