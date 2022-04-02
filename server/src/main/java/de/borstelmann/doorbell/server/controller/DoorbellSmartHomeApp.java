package de.borstelmann.doorbell.server.controller;

import com.google.actions.api.smarthome.*;
import com.google.auth.oauth2.GoogleCredentials;
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.error.BadRequestException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

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

        User user = Optional.ofNullable(CustomUserSession.getCurrentUser())
                .orElseThrow(() -> new BadRequestException("Current session does not have a user"));

        syncResponse.payload.agentUserId = String.valueOf(user.getId());
        syncResponse.payload.devices = new SyncResponse.Payload.Device[]{};
        return syncResponse;
    }
}
