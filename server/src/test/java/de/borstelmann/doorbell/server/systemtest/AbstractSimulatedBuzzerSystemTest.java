package de.borstelmann.doorbell.server.systemtest;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.test.UnitTestClock;
import de.borstelmann.doorbell.server.test.authentication.OAuthIntegrationTest;
import de.borstelmann.doorbell.server.test.websocket.StompConfig;
import de.cronn.testutils.h2.H2Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(StompConfig.class)
public abstract class AbstractSimulatedBuzzerSystemTest extends OAuthIntegrationTest {

    @Autowired
    protected UnitTestClock clock;

    @LocalServerPort
    private Integer localServerPort;

    @Autowired
    private WebSocketStompClient stompClient;

    protected DoorbellBuzzerSimulator doorbellBuzzerSimulator;
    protected DoorbellDevice sampleDoorbellDevice;
    protected String bearer;

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        User sampleUser = createSampleUser();
        sampleDoorbellDevice = createSampleDoorbellDevice(sampleUser);
        bearer = obtainToken();

        doorbellBuzzerSimulator = new DoorbellBuzzerSimulator(stompClient, webSocketUrl(), clock);
        doorbellBuzzerSimulator.connect(String.valueOf(sampleDoorbellDevice.getId()));
    }

    @AfterEach
    @Override
    protected void tearDown(@Autowired H2Util h2Util) {
        doorbellBuzzerSimulator.reset();
        super.tearDown(h2Util);
    }

    private String webSocketUrl() {
        return String.format("ws://localhost:%d/api/v1/websocket", localServerPort);
    }

    @Test
    void testOpen() throws Exception {
        openDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsOpen();
        awaitAssertQueryResponse();
    }

    @Test
    void testOpenAndWait() throws Exception {
        openDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsOpen();

        clock.setInstant(clock.instant().plus(6, ChronoUnit.SECONDS));
        doorbellBuzzerSimulator.loop();

        assertThat(doorbellBuzzerSimulator.isOpen()).isFalse();
        awaitAssertQueryResponse();
    }

    @Test
    void testOpenAndClose() throws Exception {
        openDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsOpen();

        closeDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsClosed();
        awaitAssertQueryResponse();
    }

    private void awaitAssertQueryResponse() {
        await().untilAsserted(this::assertQueryState);
    }

    protected abstract void assertQueryState() throws Exception;

    protected abstract void closeDoorbell() throws Exception;

    protected abstract void openDoorbell() throws Exception;
}
