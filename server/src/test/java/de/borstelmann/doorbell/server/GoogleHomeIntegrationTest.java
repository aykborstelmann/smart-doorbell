package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.test.SpringIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

import static de.borstelmann.doorbell.server.controller.RequestUtils.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GoogleHomeIntegrationTest extends SpringIntegrationTest {

    @Test
    void testAuthentication() throws Exception {
        mockMvc.perform(createFakeAuthRequest("https://redirect/", "state"))
                .andExpect(status().isMovedPermanently())
                .andExpect(MockMvcResultMatchers.redirectedUrl("https://redirect/?code=xxxxxx&state=state"));
    }

    @Test
    void testToken() throws Exception {
        mockMvc.perform(createFakeTokenRequest("authorization_code"))
                .andExpect(status().isOk())
                .andExpect(this::assertWithFormattedJsonFile);
    }

    @Test
    void testFulfillment() throws Exception {
        String fulfillmentRequest = """
                        {
                            "requestId": "ff36a3cc-ec34-11e6-b1a0-64510650abcf",
                            "inputs": [{
                              "intent": "action.devices.SYNC"
                            }]
                        }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.put("authorization", Collections.singletonList("Bearer ACCESS_TOKEN"));

        mockMvc.perform(createFulfillmentRequest(fulfillmentRequest, headers))
                .andExpect(status().isOk())
                .andExpect(this::assertWithFormattedJsonFile);
    }

}
