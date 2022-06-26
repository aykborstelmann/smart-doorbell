package de.borstelmann.doorbell.server.systemtest;

import de.borstelmann.doorbell.server.dto.OpenMessage;
import de.borstelmann.doorbell.server.dto.StateMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.time.Clock;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static de.borstelmann.doorbell.server.test.websocket.WebSocketClientUtil.makeHandler;
import static org.awaitility.Awaitility.await;

public class DoorbellBuzzerSimulator {
    private final Clock clock;
    private final WebSocketStompClient webSocketStompClient;
    private final String url;
    private State state = new State();

    public DoorbellBuzzerSimulator(WebSocketStompClient webSocketStompClient, String url, Clock clock) {
        this.webSocketStompClient = webSocketStompClient;
        this.url = url;
        this.clock = clock;
    }

    public void connect(String login) throws ExecutionException, InterruptedException {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.setLogin(login);


        CountDownLatch countDownLatch = new CountDownLatch(2);
        state.session = webSocketStompClient.connect(url, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(@NotNull StompSession session, @NotNull StompHeaders connectedHeaders) {
                session.setAutoReceipt(true);
                session.subscribe("/user/commands/open", makeHandler(OpenMessage.class, payload -> handleOnOpen(payload))).addReceiptTask(countDownLatch::countDown);
                session.subscribe("/user/commands/close", makeHandler(String.class, payload -> close())).addReceiptTask(countDownLatch::countDown);
            }
        }).get();
        countDownLatch.await();
    }

    public void reset() {
        state.session.disconnect();
        await().until(() -> !state.session.isConnected());
        state = new State();
    }

    public void loop() {
        if (state.startTime <= 0) {
            return;
        }

        if (clock.millis() - state.startTime > state.timer) {
            close();
        }
    }

    public boolean isOpen() {
        return state.isOpen;
    }

    private void handleOnOpen(OpenMessage openMessage) {
        open(openMessage.getTimer());
    }

    private void open(long timer) {
        state.isOpen = true;
        state.startTime = clock.millis();
        state.timer = timer;
        sendState();
    }

    private void close() {
        state.isOpen = false;
        state.startTime = 0;
        sendState();
    }

    private void sendState() {
        StateMessage payload = new StateMessage();
        payload.setIsOpened(state.isOpen);
        state.session.send("/state", payload);
    }

    public void awaitDoorbellIsClosed() {
        await().until(() -> !isOpen());
    }

    public void awaitDoorbellIsOpen() {
        await().until(this::isOpen);
    }

    private static class State {
        public StompSession session;
        public boolean isOpen;
        public long startTime;
        public long timer;
    }

}
