package de.borstelmann.doorbell.server.controller;

import com.google.actions.api.smarthome.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DoorbellSmartHomeAppTest {
    private static final String REQUEST_ID = "1";
    private static final String AGENT_USER_ID = "Bearer test";
    private static final Map<String, String> HEADERS = Map.of("authorization", AGENT_USER_ID);

    private final DoorbellSmartHomeApp doorbellSmartHomeApp = new DoorbellSmartHomeApp();

    @Test
    void testOnSync() {
        SyncRequest syncRequest = new SyncRequest();
        syncRequest.requestId = REQUEST_ID;

        SyncResponse syncResponse = doorbellSmartHomeApp.onSync(syncRequest, HEADERS);

        assertThat(syncResponse.requestId).isEqualTo(REQUEST_ID);
        assertThat(syncResponse.payload).isNotNull();
        assertThat(syncResponse.payload.agentUserId).isEqualTo(AGENT_USER_ID);
        assertThat(syncResponse.payload.devices).isEmpty();
    }

    @Test
    void testOnExecute() {
        ExecuteRequest executeRequest = new ExecuteRequest();
        executeRequest.requestId = REQUEST_ID;

        ExecuteResponse executeResponse = doorbellSmartHomeApp.onExecute(executeRequest, HEADERS);

        assertThat(executeResponse.requestId).isEqualTo(REQUEST_ID);
        assertThat(executeResponse.payload).isNotNull();
        assertThat(executeResponse.payload.getCommands()).isEmpty();
    }

    @Test
    void testOnQuery() {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.requestId = REQUEST_ID;

        QueryResponse queryResponse = doorbellSmartHomeApp.onQuery(queryRequest, HEADERS);

        assertThat(queryResponse.requestId).isEqualTo(REQUEST_ID);
        assertThat(queryResponse.payload).isNotNull();
        assertThat(queryResponse.payload.getDevices()).isEmpty();
    }
}