package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.test.authentication.OAuthIntegrationTest;
import de.cronn.assertions.validationfile.normalization.IdNormalizer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static de.borstelmann.doorbell.server.controller.RequestUtils.createFulfillmentRequest;

public class GoogleHomeIntegrationTest extends OAuthIntegrationTest {

    @Test
    void testFulfillment() throws Exception {
        createSampleUser();

        String fulfillmentRequest = """
                        {
                            "requestId": "ff36a3cc-ec34-11e6-b1a0-64510650abcf",
                            "inputs": [{
                              "intent": "action.devices.SYNC"
                            }]
                        }
                """;

        HttpHeaders headers = new HttpHeaders();
        String oAuth2 = obtainToken();
        headers.setBearerAuth(oAuth2);
        assertIsOkay(createFulfillmentRequest(fulfillmentRequest, headers), getAgentUserIdNormalizer());
    }

    @NotNull
    private IdNormalizer getAgentUserIdNormalizer() {
        return new IdNormalizer("\"agentUserId\"\\s?:\\s?\"(\\d+)\"");
    }

}
