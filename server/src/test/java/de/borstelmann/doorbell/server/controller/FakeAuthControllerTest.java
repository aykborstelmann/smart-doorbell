package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.controller.FakeAuthController;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FakeAuthController.class)
class FakeAuthControllerTest {
    private static final String REDIRECT_URL = "https://redirect";
    private static final String state = "abcd";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirect() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(createFakeAuthRequest(REDIRECT_URL, state))
                .andExpect(status().isMovedPermanently())
                .andReturn()
                .getResponse();

        String redirectUrl = response.getHeader("Location");
        assertThat(redirectUrl).isEqualTo("https://redirect?code=xxxxxx&state=abcd");
    }

    @NotNull
    private MockHttpServletRequestBuilder createFakeAuthRequest(String redirectUrl, String state) {
        return get("/api/v1/fakeauth")
                .param("redirect_uri", redirectUrl)
                .param("state", state);
    }
}