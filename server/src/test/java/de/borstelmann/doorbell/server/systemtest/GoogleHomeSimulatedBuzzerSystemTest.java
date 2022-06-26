package de.borstelmann.doorbell.server.systemtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.api.services.homegraph.v1.model.ReportStateAndNotificationRequest;
import de.borstelmann.doorbell.server.test.RequestUtils;
import de.cronn.assertions.validationfile.normalization.IdNormalizer;
import org.jetbrains.annotations.NotNull;

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

        ReportStateAndNotificationRequest reportStateAndNotificationRequest = verifyReportStateRequest();
        assertWithFormattedJsonFileWithSuffix(reportStateAndNotificationRequest, new IdNormalizer("\"requestId\" : \"(.*)\""), "report-state");
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

}
