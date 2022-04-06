package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.services.DoorbellBuzzerStateService;
import de.borstelmann.doorbell.server.test.authentication.OAuthIntegrationTest;
import de.cronn.assertions.validationfile.normalization.IdNormalizer;
import de.cronn.assertions.validationfile.normalization.ValidationNormalizer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import static de.borstelmann.doorbell.server.controller.RequestUtils.createFulfillmentRequest;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class GoogleHomeIntegrationTest extends OAuthIntegrationTest {

    private String token;
    private DoorbellDevice sampleDoorbellDevice;

    @MockBean
    private DoorbellBuzzerStateService doorbellBuzzerStateService;

    @BeforeEach
    void setUp() {
        User sampleUser = createSampleUser();
        sampleDoorbellDevice = createSampleDoorbellDevice(sampleUser);
        token = obtainToken();
    }

    @AfterEach
    void tearDown() {
        doorbellDeviceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testSync() throws Exception {
        String fulfillmentRequest = """
                        {
                            "requestId": "ff36a3cc-ec34-11e6-b1a0-64510650abcf",
                            "inputs": [{
                              "intent": "action.devices.SYNC"
                            }]
                        }
                """;


        assertIsOkay(createFulfillmentRequest(fulfillmentRequest, token), getNormalizers());
    }


    @Test
    void testQuery_disconnected_closed() throws Exception {
        String fulfillmentRequest = """
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

        assertIsOkay(createFulfillmentRequest(fulfillmentRequest, token), getNormalizers());
    }

    @Test
    void testQuery_connected_closed() throws Exception {
        sampleDoorbellDevice.setIsConnected(true);
        doorbellDeviceRepository.save(sampleDoorbellDevice);

        String fulfillmentRequest = """
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

        assertIsOkay(createFulfillmentRequest(fulfillmentRequest, token), getNormalizers());
    }

    @Test
    void testQuery_connected_opened() throws Exception {
        sampleDoorbellDevice.setIsConnected(true);
        sampleDoorbellDevice.setIsOpened(true);
        doorbellDeviceRepository.save(sampleDoorbellDevice);

        String fulfillmentRequest = """
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

        assertIsOkay(createFulfillmentRequest(fulfillmentRequest, token), getNormalizers());
    }


    @Test
    void testExecute_unlock() throws Exception {
        sampleDoorbellDevice.setIsConnected(true);
        doorbellDeviceRepository.save(sampleDoorbellDevice);

        String fulfillmentRequest = """
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

        assertIsOkay(createFulfillmentRequest(fulfillmentRequest, token), getNormalizers());

        verify(doorbellBuzzerStateService).openDoor(sampleDoorbellDevice.getId());
    }

    @Test
    void testExecute_lock() throws Exception {
        sampleDoorbellDevice.setIsConnected(true);
        doorbellDeviceRepository.save(sampleDoorbellDevice);

        String fulfillmentRequest = """
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

        assertIsOkay(createFulfillmentRequest(fulfillmentRequest, token), getNormalizers());

        verify(doorbellBuzzerStateService).closeDoor(sampleDoorbellDevice.getId());
    }

    @Test
    void testExecute_disconnected() throws Exception {
        sampleDoorbellDevice.setIsConnected(false);
        doorbellDeviceRepository.save(sampleDoorbellDevice);

        String fulfillmentRequest = """
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

        assertIsOkay(createFulfillmentRequest(fulfillmentRequest, token), getNormalizers());

        verify(doorbellBuzzerStateService, never()).openDoor(sampleDoorbellDevice.getId());
    }

    @NotNull
    private ValidationNormalizer getNormalizers() {
        IdNormalizer agentUserIdNormalizer = new IdNormalizer("\"agentUserId\"\\s?:\\s?\"(\\d+)\"");
        IdNormalizer queryDeviceIdNormalizer = new IdNormalizer("\"(\\d)\"\\s?:");
        IdNormalizer executeDeviceIdNormalizer = new IdNormalizer("\\[\\s?\"(\\d+)\"\\s?]");
        IdNormalizer deviceIdNormalizer = new IdNormalizer("\"id\"\\s?:\\s?\"(\\d+)\"");
        return ValidationNormalizer.combine(agentUserIdNormalizer, queryDeviceIdNormalizer, executeDeviceIdNormalizer, deviceIdNormalizer);
    }
}
