package de.borstelmann.doorbell.server.controller;

import com.google.actions.api.smarthome.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DoorbellSmartHomeApp extends SmartHomeApp {

    @Autowired(required = false)
    public void setGoogleCredentials(GoogleCredentials googleCredentials) {
        setCredentials(googleCredentials);
    }

    @Override
    public void onDisconnect(@NotNull DisconnectRequest disconnectRequest, @Nullable Map<?, ?> map) {

    }

    @NotNull
    @Override
    public ExecuteResponse onExecute(@NotNull ExecuteRequest executeRequest, @Nullable Map<?, ?> map) {
        ExecuteResponse executeResponse = new ExecuteResponse();
        executeResponse.requestId = executeRequest.requestId;
        executeResponse.payload = new ExecuteResponse.Payload();
        executeResponse.payload.setCommands(new ExecuteResponse.Payload.Commands[]{});
        return executeResponse;
    }

    @NotNull
    @Override
    public QueryResponse onQuery(@NotNull QueryRequest queryRequest, @Nullable Map<?, ?> map) {
        QueryResponse queryResponse = new QueryResponse();
        queryResponse.requestId = queryRequest.requestId;
        queryResponse.payload = new QueryResponse.Payload();
        queryResponse.payload.setDevices(Map.of());
        return queryResponse;
    }

    @NotNull
    @Override
    public SyncResponse onSync(@NotNull SyncRequest syncRequest, @Nullable Map<?, ?> headers) {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.requestId = syncRequest.requestId;
        syncResponse.payload = new SyncResponse.Payload();

        String userId = (String) headers.get("authorization");
        syncResponse.payload.agentUserId = userId;
        syncResponse.payload.devices = new SyncResponse.Payload.Device[]{};
        return syncResponse;
    }
}
