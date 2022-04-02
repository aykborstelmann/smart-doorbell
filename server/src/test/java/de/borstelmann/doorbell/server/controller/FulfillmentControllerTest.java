package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static de.borstelmann.doorbell.server.controller.RequestUtils.createFulfillmentRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FulfillmentController.class)
class FulfillmentControllerTest {

    @MockBean
    private DoorbellSmartHomeApp doorbellSmartHomeApp;

    @MockBean
    private UserService userService;

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

        final MockHttpServletResponse response = mockMvc.perform(createFulfillmentRequest(requestBody, headers)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        final String responseBody = response.getContentAsString();
        assertThat(responseBody).isEqualTo(expectedResponse);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(headersCaptor.getValue()).containsEntry("Key", "Value");
    }

}