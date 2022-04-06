package de.borstelmann.doorbell.server.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class RequestUtils {

    @NotNull
    public static MockHttpServletRequestBuilder createFulfillmentRequest(String requestBody, HttpHeaders headers) {
        return post("/api/v1/fulfillment")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);
    }


    @NotNull
    public static MockHttpServletRequestBuilder createFulfillmentRequest(String requestBody, String bearer) {
        return post("/api/v1/fulfillment")
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);
    }


    @NotNull
    public static RequestBuilder createGetAllUsersRequest(String bearer) {
        return get("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer));
    }

    @NotNull
    public static RequestBuilder createGetUserByIdRequest(long userId, String bearer) {
        return get("/api/v1/users/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer));
    }

    @NotNull
    public static RequestBuilder createDeleteUserRequest(long userId, String bearer) {
        return delete("/api/v1/users/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer));
    }

    @NotNull
    public static RequestBuilder createCreateDoorbellRequest(long userId, String doorbellRequest, String bearer) {
        return post("/api/v1/users/{userId}/doorbells", userId)
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(doorbellRequest);
    }

    public static RequestBuilder createGetAllDoorbellsRequest(long userId, String bearer) {
        return get("/api/v1/users/{userId}/doorbells", userId)
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer));
    }

    public static RequestBuilder createGetDoorbellRequest(long doorbellId, String bearer) {
        return get("/api/v1/doorbells/{doorbellId}", doorbellId)
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer));
    }

    public static RequestBuilder createDeleteDoorbellRequest(long doorbellId, String bearer) {
        return delete("/api/v1/doorbells/{doorbellId}", doorbellId)
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer));
    }

    public static RequestBuilder createOpenDoorbellRequest(long doorbellId, String bearer) {
        return post("/api/v1/doorbells/{doorbellId}/open", doorbellId)
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer));
    }

    public static RequestBuilder createCloseDoorbellRequest(long doorbellId, String bearer) {
        return post("/api/v1/doorbells/{doorbellId}/close", doorbellId)
                .header(HttpHeaders.AUTHORIZATION, getBearer(bearer));
    }

    @NotNull
    private static String getBearer(String bearer) {
        return "Bearer " + bearer;
    }
}
