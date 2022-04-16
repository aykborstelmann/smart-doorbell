package de.borstelmann.doorbell.server.systemtest;

import de.borstelmann.doorbell.server.controller.RequestUtils;

public class ApiSimulatedBuzzerSystemTest extends AbstractSimulatedBuzzerSystemTest {

    @Override
    protected void queryDoorbell() throws Exception {
        mockMvc.perform(RequestUtils.createGetDoorbellRequest(sampleDoorbellDevice.getId(), bearer))
                .andExpect(this::assertWithFormattedJsonFile);
    }

    @Override
    protected void closeDoorbell() throws Exception {
        mockMvc.perform(RequestUtils.createCloseDoorbellRequest(sampleDoorbellDevice.getId(), bearer));
    }

    @Override
    protected void openDoorbell() throws Exception {
        mockMvc.perform(RequestUtils.createOpenDoorbellRequest(sampleDoorbellDevice.getId(), bearer));
    }

}
