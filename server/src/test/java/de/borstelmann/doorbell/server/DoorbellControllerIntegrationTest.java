package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.test.RequestUtils;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.openapi.model.DoorbellRequest;
import de.borstelmann.doorbell.server.openapi.model.DoorbellResponse;
import de.borstelmann.doorbell.server.test.authentication.OAuthIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class DoorbellControllerIntegrationTest extends OAuthIntegrationTest {

    public static final OffsetDateTime SAMPLE_DATE_TIME = OffsetDateTime.of(2020, 3, 30, 15, 8, 0, 0, ZoneOffset.UTC);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoorbellDeviceRepository doorbellDeviceRepository;

    @Test
    void testCreateDoorbell() throws Exception {
        User user = createSampleUser();

        DoorbellRequest doorbellRequest = new DoorbellRequest().name("name");
        String body = objectMapper.writeValueAsString(doorbellRequest);

        assertIsOkay(RequestUtils.createCreateDoorbellRequest(user.getId(), body, obtainToken()))
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    DoorbellResponse doorbellResponse = objectMapper.readValue(responseBody, DoorbellResponse.class);
                    assertThat(doorbellDeviceRepository.findById(doorbellResponse.getId()))
                            .isPresent()
                            .get()
                            .extracting(DoorbellDevice::getUser)
                            .extracting(User::getId)
                            .isEqualTo(user.getId());
                });
    }

    @Test
    void testCreateDoorbell_userNotFound() throws Exception {
        DoorbellRequest doorbellRequest = new DoorbellRequest().name("name");
        String body = objectMapper.writeValueAsString(doorbellRequest);

        assertNotFound(RequestUtils.createCreateDoorbellRequest(0L, body, obtainToken()));

        assertThat(doorbellDeviceRepository.findAll()).isEmpty();
    }

    @Test
    void testGetAllDoorbells() throws Exception {
        User user = createSampleUser();
        createSampleDoorbellDevice(user);

        assertIsOkay(RequestUtils.createGetAllDoorbellsRequest(user.getId(), obtainToken()));
    }

    @Test
    void testGetAllDoorbells_notFound() throws Exception {
        assertNotFound(RequestUtils.createGetAllDoorbellsRequest(0L, obtainToken()));
    }

    @Test
    void testGetDoorbell() throws Exception {
        User user = createSampleUser();

        DoorbellDevice doorbell = DoorbellDevice.builder()
                .name("Doorbell")
                .user(user)
                .isConnected(false)
                .isOpened(true)
                .lastNotified(SAMPLE_DATE_TIME)
                .build();

        doorbellDeviceRepository.saveAndFlush(doorbell);

        assertIsOkay(RequestUtils.createGetDoorbellRequest(doorbell.getId(), obtainToken()));
    }

    @Test
    void testGetDoorbell_doorbellIdNotFound() throws Exception {
        assertNotFound(RequestUtils.createGetDoorbellRequest(1L, obtainToken()));
    }

    @Test
    void testDeleteDoorbell() throws Exception {
        User user = createSampleUser();
        DoorbellDevice doorbell = createSampleDoorbellDevice(user);

        assertNoContent(RequestUtils.createDeleteDoorbellRequest(doorbell.getId(), obtainToken()));
    }

    @Test
    void testDeleteDoorbell_doorbellIdNotFound() throws Exception {
        assertNotFound(RequestUtils.createDeleteDoorbellRequest(1L, obtainToken()));
    }

}
