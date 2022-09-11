package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.test.RequestUtils;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.dto.OpenMessage;
import de.borstelmann.doorbell.server.dto.StateMessage;
import de.borstelmann.doorbell.server.test.authentication.OAuthIntegrationTest;
import de.borstelmann.doorbell.server.test.websocket.StompConfig;
import de.borstelmann.doorbell.server.test.websocket.WebSocketClientUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(StompConfig.class)
public class WebSocketIntegrationTest extends OAuthIntegrationTest {

    private DoorbellDevice sampleDoorbellDevice;
    private StompSession stompSession;

    @LocalServerPort
    private Integer port;

    @Autowired
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() throws InterruptedException, ExecutionException {
        User sampleUser = createSampleUserWithGoogleHomeConnected();
        sampleDoorbellDevice = createSampleDoorbellDevice(sampleUser);
        stompSession = getStompSessionWithLogin(String.valueOf(sampleDoorbellDevice.getId()));
    }

    @AfterEach
    public void disconnectStompSession() {
        if (stompSession.isConnected()) {
            stompSession.disconnect();
            await().until(() -> !stompSession.isConnected());
        }
    }

    @Test
    void testConnect_withWrongDoorbellDeviceId() {
        String login = sampleDoorbellDevice.getId() != 0L ? "0" : "1";
        Assertions.assertThatThrownBy(() -> getStompSessionWithLogin(login))
                .hasCauseInstanceOf(ConnectionLostException.class);
    }

    @Test
    void testConnect() throws Exception {
        mockMvc.perform(RequestUtils.createGetDoorbellRequest(sampleDoorbellDevice.getId(), obtainToken()))
                .andExpect(this::assertWithFormattedJsonFile);
    }

    @Test
    void testDisconnect() {
        stompSession.disconnect();

        await().untilAsserted(() ->
                mockMvc.perform(RequestUtils.createGetDoorbellRequest(sampleDoorbellDevice.getId(), obtainToken()))
                        .andExpect(this::assertWithFormattedJsonFile)
        );
    }

    @Test
    void testSendState() {
        StateMessage stateMessage = new StateMessage();
        stateMessage.setIsOpened(true);
        stompSession.send("/state", stateMessage);

        await().untilAsserted(() ->
                mockMvc.perform(RequestUtils.createGetDoorbellRequest(sampleDoorbellDevice.getId(), obtainToken()))
                        .andExpect(this::assertWithFormattedJsonFile)
        );
    }

    @Test
    void testSubscribeOpen() throws Exception {
        AtomicReference<OpenMessage> payloadRef = new AtomicReference<>();
        subscribeAndWait(stompSession, "/user/commands/open", WebSocketClientUtil.makeHandler(OpenMessage.class, payloadRef::set));

        mockMvc.perform(RequestUtils.createOpenDoorbellRequest(sampleDoorbellDevice.getId(), obtainToken()))
                .andExpect(status().isNoContent());

        await().untilAsserted(() ->
                assertThat(payloadRef)
                        .hasValueMatching(openMessage -> openMessage.getTimer() == 5000L)
        );
    }

    @Test
    void testSubscribeClose() throws Exception {
        AtomicBoolean hasBeenCalled = new AtomicBoolean(false);
        subscribeAndWait(stompSession, "/user/commands/close", WebSocketClientUtil.makeHandler(String.class, s -> hasBeenCalled.set(true)));

        mockMvc.perform(RequestUtils.createCloseDoorbellRequest(sampleDoorbellDevice.getId(), obtainToken()))
                .andExpect(status().isNoContent());

        await().untilAsserted(() ->
                assertThat(hasBeenCalled).isTrue()
        );
    }

    private void subscribeAndWait(StompSession stompSession, String destination, StompFrameHandler handler) throws InterruptedException {
        CountDownLatch subscribeCountDown = new CountDownLatch(1);
        stompSession.subscribe(destination, handler).addReceiptTask(subscribeCountDown::countDown);
        subscribeCountDown.await();
    }

    private String getWebSocketUrl() {
        return String.format("ws://localhost:%d/api/v1/websocket", port);
    }

    private StompSession getStompSessionWithLogin(String login) throws InterruptedException, ExecutionException {
        StompSessionHandlerAdapter handler = new StompSessionHandlerAdapter() {
        };

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.setLogin(login);
        StompSession stompSession = stompClient.connect(getWebSocketUrl(), new WebSocketHttpHeaders(), stompHeaders, handler).get();
        stompSession.setAutoReceipt(true);
        return stompSession;
    }

}
