package de.borstelmann.doorbell.server.systemtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.test.UnitTestClock;
import de.borstelmann.doorbell.server.test.authentication.OAuthIntegrationTest;
import de.borstelmann.doorbell.server.test.websocket.StompConfig;
import de.cronn.assertions.validationfile.normalization.IdNormalizer;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import({StompConfig.class, GoogleHomeMockConfig.class})
public abstract class AbstractSimulatedBuzzerSystemTest extends OAuthIntegrationTest {

    @Autowired
    protected MockWebServer mockGoogleHomeApi;

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
        User sampleUser = createSampleUserWithGoogleHomeConnected();
        sampleDoorbellDevice = createSampleDoorbellDevice(sampleUser);
        bearer = obtainToken();

        doorbellBuzzerSimulator = createDoorbellBuzzerSimulator();
        doorbellBuzzerSimulator.connect(String.valueOf(sampleDoorbellDevice.getId()));

        extractAllRequests();
    }

    protected DoorbellBuzzerSimulator createDoorbellBuzzerSimulator() {
        return new DoorbellBuzzerSimulator(stompClient, webSocketUrl(), clock);
    }

    private void extractAllRequests() throws InterruptedException {
        while (true)  {
            if (getRecordedRequest() == null) {
                break;
            }
        }
    }

    @AfterEach
    protected void resetSimulator() {
        doorbellBuzzerSimulator.reset();
    }

    private String webSocketUrl() {
        return String.format("ws://localhost:%d/api/v1/websocket", localServerPort);
    }

    @Test
    void testOpen() throws Exception {
        openDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsOpen();
        awaitAssertQueryResponse();
        assertGoogleHomeRequestIsSent("open");
    }

    @Test
    void testOpenAndWait() throws Exception {
        openDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsOpen();
        assertGoogleHomeRequestIsSent("open");

        clock.setInstant(clock.instant().plus(6, ChronoUnit.SECONDS));
        doorbellBuzzerSimulator.loop();

        assertThat(doorbellBuzzerSimulator.isOpen()).isFalse();
        awaitAssertQueryResponse();
        assertGoogleHomeRequestIsSent("close");
    }

    @Test
    void testOpenAndClose() throws Exception {
        openDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsOpen();
        assertGoogleHomeRequestIsSent("open");

        closeDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsClosed();
        awaitAssertQueryResponse();
        assertGoogleHomeRequestIsSent("close");
    }

    protected void awaitAssertQueryResponse() {
        await().untilAsserted(this::assertQueryState);
    }

    protected abstract void closeDoorbell() throws Exception;

    protected abstract void openDoorbell() throws Exception;

    protected abstract void assertQueryState() throws Exception;

    public void assertGoogleHomeRequestIsSent(String suffix) throws InterruptedException, IOException {
        String content = readRequestBody(getRecordedRequest());
        assertWithFormattedJsonFileWithSuffix(content, new IdNormalizer("\"requestId\" : \"(.*)\""), "report-state-%s".formatted(suffix));
    }

    private static String readRequestBody(RecordedRequest recordedRequest) throws InterruptedException, IOException {
        if (recordedRequest == null) {
            return null;
        }

        try (
                GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(recordedRequest.getBody().readByteArray()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8))
        ) {
            return reader.readLine();
        }
    }

    @NotNull
    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper.copy().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    private RecordedRequest getRecordedRequest() throws InterruptedException {
        return mockGoogleHomeApi.takeRequest(100, TimeUnit.MILLISECONDS);
    }

}
