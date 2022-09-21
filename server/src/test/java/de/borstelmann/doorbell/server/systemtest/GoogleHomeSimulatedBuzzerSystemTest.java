package de.borstelmann.doorbell.server.systemtest;

import de.borstelmann.doorbell.server.test.RequestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GoogleHomeSimulatedBuzzerSystemTest extends AbstractSimulatedBuzzerSystemTest {

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

    @Test
    void testDisconnectConnectInformsGoogleHome() throws ExecutionException, InterruptedException, IOException {
        doorbellBuzzerSimulator.reset();
        assertGoogleHomeRequestIsSent("disconnect");

        doorbellBuzzerSimulator.connect(String.valueOf(sampleDoorbellDevice.getId()));
        assertGoogleHomeRequestIsSent("connect");
    }

}
