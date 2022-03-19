package de.borstelmann.doorbell.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.borstelmann.doorbell.server.controller.FakeTokenController;
import de.borstelmann.doorbell.server.response.TokenResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FakeTokenController.class)
class FakeTokenControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnFakeToken() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(createFakeTokenRequest("something-else"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        TokenResponse tokenResponse = objectMapper.readValue(response.getContentAsString(), TokenResponse.class);

        assertThat(tokenResponse.getTokenType()).isEqualTo("bearer");
        assertThat(tokenResponse.getAccessToken()).isEqualTo("123access");
        assertThat(tokenResponse.getExpiresIn()).isEqualTo(86400);
        assertThat(tokenResponse.getRefreshToken()).isNull();
    }

    @Test
    void shouldReturnFakeTokenWithRefreshToken() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(createFakeTokenRequest("authorization_code"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        TokenResponse tokenResponse = objectMapper.readValue(response.getContentAsString(), TokenResponse.class);

        assertThat(tokenResponse.getTokenType()).isEqualTo("bearer");
        assertThat(tokenResponse.getAccessToken()).isEqualTo("123access");
        assertThat(tokenResponse.getExpiresIn()).isEqualTo(86400);
        assertThat(tokenResponse.getRefreshToken()).isEqualTo("123refresh");
    }

    @NotNull
    private MockHttpServletRequestBuilder createFakeTokenRequest(String grantType) {
        return post("/api/v1/faketoken")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grant_type", grantType);
    }
}