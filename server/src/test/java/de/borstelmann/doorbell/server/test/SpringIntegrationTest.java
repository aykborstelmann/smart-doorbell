package de.borstelmann.doorbell.server.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.cronn.testutils.h2.H2Util;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(H2Util.class)
public abstract class SpringIntegrationTest extends BaseTest {

    public static final String SAMPLE_OAUTH_ID = "oauthId";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected DoorbellDeviceRepository doorbellDeviceRepository;

    @AfterEach
    protected void tearDown(@Autowired H2Util h2Util) {
        h2Util.resetDatabase();
    }

    protected ResultActions assertIsOkay(RequestBuilder createDoorbellBuzzerRequest) throws Exception {
        return mockMvc.perform(createDoorbellBuzzerRequest)
                .andExpect(status().isOk())
                .andExpect(this::assertWithFormattedJsonFile);
    }

    protected void assertNotFound(RequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(this::assertWithFormattedJsonFile);
    }

    protected void assertNoContent(RequestBuilder deleteUserRequest) throws Exception {
        mockMvc.perform(deleteUserRequest)
                .andExpect(status().isNoContent());
    }

    protected DoorbellDevice createSampleDoorbellDevice(User user) {
        DoorbellDevice doorbell = DoorbellDevice.builder()
                .name("Doorbell")
                .user(user)
                .build();
        doorbellDeviceRepository.saveAndFlush(doorbell);
        return doorbell;
    }

    protected User createSampleUser() {
        User user = User.builder()
                .oAuthId(SAMPLE_OAUTH_ID)
                .build();
        userRepository.saveAndFlush(user);
        return user;
    }

    @NotNull
    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
