package de.borstelmann.doorbell.server.systemtest;

import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.api.services.homegraph.v1.HomeGraphService.Devices.ReportStateAndNotification;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationRequest;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(GoogleHomeMockConfig.class)
class MockGoogleHomeGraphDispatcherTest {

    @Autowired
    private HomeGraphService homeGraphService;

    @Test
    void testSendGoogleHomeRequest_hasSameRequestId() throws IOException {
        String requestId = UUID.randomUUID().toString();

        ReportStateAndNotificationRequest payload = new ReportStateAndNotificationRequest()
                .setRequestId(requestId);

        ReportStateAndNotification request = homeGraphService.devices().reportStateAndNotification(payload);
        ReportStateAndNotificationResponse response = request.execute();

        assertThat(response.getRequestId()).isEqualTo(requestId);
    }
}