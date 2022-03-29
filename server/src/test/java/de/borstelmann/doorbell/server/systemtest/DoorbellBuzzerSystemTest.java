package de.borstelmann.doorbell.server.systemtest;

import de.borstelmann.doorbell.server.controller.RequestUtils;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.test.SpringIntegrationTest;
import de.borstelmann.doorbell.server.test.StompConfig;
import de.borstelmann.doorbell.server.test.UnitTestClock;
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
public class DoorbellBuzzerSystemTest extends SpringIntegrationTest {

    @LocalServerPort
    private Integer localServerPort;

    @Autowired
    private WebSocketStompClient stompClient;

    @Autowired
    private UnitTestClock clock;

    private DoorbellBuzzerSimulator doorbellBuzzerSimulator;
    private DoorbellDevice sampleDoorbellDevice;

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        User sampleUser = createSampleUser();
        sampleDoorbellDevice = createSampleDoorbellDevice(sampleUser);

        doorbellBuzzerSimulator = new DoorbellBuzzerSimulator(stompClient, webSocketUrl(), clock);
        doorbellBuzzerSimulator.connect(String.valueOf(sampleDoorbellDevice.getId()));
    }

    @AfterEach
    void tearDown() {
        doorbellBuzzerSimulator.reset();
        doorbellDeviceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testOpenViaApi() throws Exception {
        mockMvc.perform(RequestUtils.createOpenDoorbellRequest(sampleDoorbellDevice.getId()));
        await().untilAsserted(() -> {
            assertThat(doorbellBuzzerSimulator.isOpen()).isTrue();
            mockMvc.perform(RequestUtils.createGetDoorbellRequest(sampleDoorbellDevice.getId()))
                    .andExpect(this::assertWithFormattedJsonFile);
        });
    }

    @Test
    void testOpenAndWaitViaApi() throws Exception {
        mockMvc.perform(RequestUtils.createOpenDoorbellRequest(sampleDoorbellDevice.getId()));
        await().untilAsserted(() ->
                assertThat(doorbellBuzzerSimulator.isOpen()).isTrue()
        );
        clock.setInstant(clock.instant().plus(6, ChronoUnit.SECONDS));
        doorbellBuzzerSimulator.loop();
        assertThat(doorbellBuzzerSimulator.isOpen()).isFalse();
        await().untilAsserted(() ->
                mockMvc.perform(RequestUtils.createGetDoorbellRequest(sampleDoorbellDevice.getId()))
                        .andExpect(this::assertWithFormattedJsonFile)
        );
    }

    @Test
    void testOpenAndCloseViaApi() throws Exception {
        mockMvc.perform(RequestUtils.createOpenDoorbellRequest(sampleDoorbellDevice.getId()));
        await().untilAsserted(() ->
                assertThat(doorbellBuzzerSimulator.isOpen()).isTrue()
        );
        mockMvc.perform(RequestUtils.createCloseDoorbellRequest(sampleDoorbellDevice.getId()));
        await().untilAsserted(() -> {
            assertThat(doorbellBuzzerSimulator.isOpen()).isFalse();
            mockMvc.perform(RequestUtils.createGetDoorbellRequest(sampleDoorbellDevice.getId()))
                    .andExpect(this::assertWithFormattedJsonFile);
        });
    }

    public String webSocketUrl() {
        return String.format("ws://localhost:%d/api/v1/websocket", localServerPort);
    }

}
