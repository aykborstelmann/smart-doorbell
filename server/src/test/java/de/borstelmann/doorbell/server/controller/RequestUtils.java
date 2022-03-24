package de.borstelmann.doorbell.server.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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

    @NotNull
    public static MockHttpServletRequestBuilder createCreateUserRequest(String userRequest) {
        return post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequest);
    }

    @NotNull
    public static RequestBuilder createGetAllUsersRequest() {
        return get("/api/v1/users");
    }

    @NotNull
    public static RequestBuilder createGetUserByIdRequest(long userId) {
        return get("/api/v1/users/{userId}", userId);
    }

    @NotNull
    public static RequestBuilder createDeleteUserRequest(long userId) {
        return delete("/api/v1/users/{userId}", userId);
    }

    @NotNull
    public static RequestBuilder createCreateDoorbellRequest(long userId, String doorbellRequest) {
        return post("/api/v1/users/{userId}/doorbells", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(doorbellRequest);
    }

    public static RequestBuilder createGetAllDoorbellsRequest(long userId) {
        return get("/api/v1/users/{userId}/doorbells", userId);
    }

    public static RequestBuilder createGetDoorbellRequest(long doorbellId) {
        return get("/api/v1/doorbells/{doorbellId}", doorbellId);
    }

    public static RequestBuilder createDeleteDoorbellRequest(long doorbellId) {
        return delete("/api/v1/doorbells/{doorbellId}", doorbellId);
    }

}
