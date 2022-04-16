package de.borstelmann.doorbell.server.systemtest;

import de.borstelmann.doorbell.server.test.RequestUtils;

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

}
