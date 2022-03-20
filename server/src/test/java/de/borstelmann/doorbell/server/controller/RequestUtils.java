package de.borstelmann.doorbell.server.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RequestUtils {
    @NotNull
    public static MockHttpServletRequestBuilder createFakeAuthRequest(String redirectUrl, String state) {
        return get("/api/v1/fakeauth")
                .param("redirect_uri", redirectUrl)
                .param("state", state);
    }

    @NotNull
    public static MockHttpServletRequestBuilder createFakeTokenRequest(String grantType) {
        return post("/api/v1/faketoken")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grant_type", grantType)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @NotNull
    public static MockHttpServletRequestBuilder createFulfillmentRequest(String requestBody, HttpHeaders headers) {
        return post("/api/v1/fulfillment")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);
    }
}
