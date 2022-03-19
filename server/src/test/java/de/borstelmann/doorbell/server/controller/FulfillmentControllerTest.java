package de.borstelmann.doorbell.server.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FulfillmentController.class)
class FulfillmentControllerTest {

    @MockBean
    private DoorbellSmartHomeApp doorbellSmartHomeApp;

    @Autowired
    private MockMvc mockMvc;

    @Captor
    private ArgumentCaptor<Map<String, String>> headersCaptor;

    @Test
    void shouldDelegateToSmartHomeApp() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Key", List.of("Value"));

        final String requestBody = "{}";
        final String expectedResponse = "{}";

        doReturn(CompletableFuture.completedFuture(expectedResponse))
                .when(doorbellSmartHomeApp).handleRequest(eq(requestBody), headersCaptor.capture());

        final MockHttpServletResponse response = mockMvc.perform(createFulfillmentRequest(requestBody, headers))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        final String responseBody = response.getContentAsString();
        assertThat(responseBody).isEqualTo(expectedResponse);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(headersCaptor.getValue()).containsEntry("Key", "Value");
    }

    @NotNull
    private MockHttpServletRequestBuilder createFulfillmentRequest(String requestBody, HttpHeaders headers) {
        return post("/api/v1/fulfillment")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);
    }

}