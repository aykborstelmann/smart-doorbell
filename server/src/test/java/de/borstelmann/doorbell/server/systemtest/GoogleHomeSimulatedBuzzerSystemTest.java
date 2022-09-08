package de.borstelmann.doorbell.server.systemtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationRequest;
import com.google.auth.http.HttpCredentialsAdapter;
import de.borstelmann.doorbell.server.response.google.home.GoogleHomeDeviceService;
import de.borstelmann.doorbell.server.test.RequestUtils;
import de.cronn.assertions.validationfile.normalization.IdNormalizer;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Import(GoogleHomeSimulatedBuzzerSystemTest.GoogleHomeMockConfig.class)
public class GoogleHomeSimulatedBuzzerSystemTest extends AbstractSimulatedBuzzerSystemTest {

    private static final MockWebServer mockGoogleHomeApi = new MockWebServer();

    @BeforeAll
    static void setupMockApi() throws IOException {
        mockGoogleHomeApi.start();
    }

    @Override
    protected void assertQueryState() throws Exception {
        var requestBody = """
                {
                  "inputs": [
                    {
                      "intent": "action.devices.QUERY",
                      "payload": {
                        "devices": [
                          {
                            "id": "%d"
                          }
                        ]
                      }
                    }
                  ],
                  "requestId": "17833958777429973331"
                }
                """.formatted(sampleDoorbellDevice.getId());

        assertIsOkay(RequestUtils.createFulfillmentRequest(requestBody, bearer));

        ReportStateAndNotificationRequest reportStateAndNotificationRequest = verifyReportStateRequest();
        assertWithFormattedJsonFileWithSuffix(reportStateAndNotificationRequest, new IdNormalizer("\"requestId\" : \"(.*)\""), "report-state");

        System.out.println(mockGoogleHomeApi.takeRequest());
    }

    @Override
    protected void closeDoorbell() throws Exception {
        var requestBody = """
                {
                  "inputs": [
                    {
                      "intent": "action.devices.EXECUTE",
                      "payload": {
                        "commands": [
                          {
                            "devices": [
                              {
                                "id": "%d"
                              }
                            ],
                            "execution": [
                              {
                                "command": "action.devices.commands.LockUnlock",
                                "params": {
                                  "followUpToken": "[followUpToken]",
                                  "lock": true
                                }
                              }
                            ]
                          }
                        ]
                      }
                    }
                  ],
                  "requestId": "4982066045573259553"
                }
                """.formatted(sampleDoorbellDevice.getId());

        mockMvc.perform(RequestUtils.createFulfillmentRequest(requestBody, bearer));
    }

    @Override
    protected void openDoorbell() throws Exception {
        var requestBody = """
                {
                  "inputs": [
                    {
                      "intent": "action.devices.EXECUTE",
                      "payload": {
                        "commands": [
                          {
                            "devices": [
                              {
                                "id": "%d"
                              }
                            ],
                            "execution": [
                              {
                                "command": "action.devices.commands.LockUnlock",
                                "params": {
                                  "followUpToken": "[followUpToken]",
                                  "lock": false
                                }
                              }
                            ]
                          }
                        ]
                      }
                    }
                  ],
                  "requestId": "4982066045573259553"
                }
                """.formatted(sampleDoorbellDevice.getId());

        mockMvc.perform(RequestUtils.createFulfillmentRequest(requestBody, bearer));
    }

    @NotNull
    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper.copy().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @DynamicPropertySource
    static void registerGoogleHomeApiUrl(DynamicPropertyRegistry registry) {
        registry.add("googleHomeApiurl", () -> mockGoogleHomeApi.url("/").toString());
    }

    @Configuration
    static class GoogleHomeMockConfig {

        @Bean
        @Primary
        public HomeGraphService homeGraphService(@Value("${googleHomeApiurl}") String googleHomeGraphUrl) throws GeneralSecurityException, IOException {
            HomeGraphService.Builder builder = new HomeGraphService.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new MockGoogleCredential(new MockGoogleCredential.Builder())
            );

            return builder
                    .setRootUrl(googleHomeGraphUrl)
                    .setApplicationName("HomeGraphExample/1.0")
                    .build();
        }

    }


}
