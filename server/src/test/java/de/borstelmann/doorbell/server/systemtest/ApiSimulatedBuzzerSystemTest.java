package de.borstelmann.doorbell.server.systemtest;

import de.borstelmann.doorbell.server.test.RequestUtils;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiSimulatedBuzzerSystemTest extends AbstractSimulatedBuzzerSystemTest {

    @Override
    protected void assertQueryState() throws Exception {
        assertIsOkay(RequestUtils.createGetDoorbellRequest(sampleDoorbellDevice.getId(), bearer));
    }

    @Override
    protected void closeDoorbell() throws Exception {
        mockMvc.perform(RequestUtils.createCloseDoorbellRequest(sampleDoorbellDevice.getId(), bearer));
    }

    @Override
    protected void openDoorbell() throws Exception {
        mockMvc.perform(RequestUtils.createOpenDoorbellRequest(sampleDoorbellDevice.getId(), bearer));
    }

    @Test
    void testOpenDoorbell_withoutGoogleHome_doesntSendRequest() throws Exception {
        disconnectGoogleHome();
        openDoorbell();
        doorbellBuzzerSimulator.awaitDoorbellIsOpen();
        awaitAssertQueryResponse();
        assertNoGoogleHomeRequestIsSent();
    }

    private void disconnectGoogleHome() throws Exception {
        var request = """
                 {
                   "inputs": [
                     {
                       "intent": "action.devices.DISCONNECT"
                     }
                   ],
                   "requestId": "12514232070250909450"
                 }
                                 
                """;

        mockMvc.perform(RequestUtils.createFulfillmentRequest(request, bearer));
    }

    private void assertNoGoogleHomeRequestIsSent() throws InterruptedException {
        RecordedRequest recordedRequest = mockGoogleHomeApi.takeRequest(10, TimeUnit.MILLISECONDS);
        assertThat(recordedRequest).isNull();
    }
}
