package de.borstelmann.doorbell.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cronn.assertions.validationfile.junit5.JUnit5ValidationFileAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

import static de.borstelmann.doorbell.server.controller.RequestUtils.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GoogleHomeSystemTest implements JUnit5ValidationFileAssertions {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAuthentication() throws Exception {
        mockMvc.perform(createFakeAuthRequest("https://redirect/", "state"))
                .andExpect(status().isMovedPermanently())
                .andExpect(MockMvcResultMatchers.redirectedUrl("https://redirect/?code=xxxxxx&state=state"));
    }

    @Test
    void testToken() throws Exception {
        var responseBody = mockMvc.perform(createFakeTokenRequest("authorization_code"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        assertWithJsonFile(responseBody);
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

        var responseBody = mockMvc.perform(createFulfillmentRequest(fulfillmentRequest, headers))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertWithJsonFile(responseBody);
    }

    private String formatJson(String json) {
        try {
            Object o = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assertWithJsonFile(String responseBody) {
        JUnit5ValidationFileAssertions.super.assertWithJsonFile(formatJson(responseBody));
    }
}
