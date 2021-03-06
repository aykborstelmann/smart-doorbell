package de.borstelmann.doorbell.server.controller;

import com.google.actions.api.smarthome.*;
import de.borstelmann.doorbell.server.test.authentication.WithMockOAuth2Scope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(DoorbellSmartHomeApp.class)
class DoorbellSmartHomeAppTest {
    private static final String REQUEST_ID = "1";
    private static final long AGENT_USER_ID = 1L;
    public static final Map<Object, Object> EMPTY_HEADERS = Map.of();

    @Autowired
    private DoorbellSmartHomeApp doorbellSmartHomeApp;

    @Test
    @WithMockOAuth2Scope(userId = AGENT_USER_ID)
    void testOnSync() {
        SyncRequest syncRequest = new SyncRequest();
        syncRequest.requestId = REQUEST_ID;

        SyncResponse syncResponse = doorbellSmartHomeApp.onSync(syncRequest, EMPTY_HEADERS);

        assertThat(syncResponse.requestId).isEqualTo(REQUEST_ID);
        assertThat(syncResponse.payload).isNotNull();
        assertThat(syncResponse.payload.agentUserId).isEqualTo(String.valueOf(AGENT_USER_ID));
        assertThat(syncResponse.payload.devices).isEmpty();
    }

    @Test
    void testOnExecute() {
        ExecuteRequest executeRequest = new ExecuteRequest();
        executeRequest.requestId = REQUEST_ID;

        ExecuteResponse executeResponse = doorbellSmartHomeApp.onExecute(executeRequest, EMPTY_HEADERS);

        assertThat(executeResponse.requestId).isEqualTo(REQUEST_ID);
        assertThat(executeResponse.payload).isNotNull();
        assertThat(executeResponse.payload.getCommands()).isEmpty();
    }

    @Test
    void testOnQuery() {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.requestId = REQUEST_ID;

        QueryResponse queryResponse = doorbellSmartHomeApp.onQuery(queryRequest, EMPTY_HEADERS);

        assertThat(queryResponse.requestId).isEqualTo(REQUEST_ID);
        assertThat(queryResponse.payload).isNotNull();
        assertThat(queryResponse.payload.getDevices()).isEmpty();
    }
}