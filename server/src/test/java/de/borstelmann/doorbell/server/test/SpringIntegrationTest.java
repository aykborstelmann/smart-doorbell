package de.borstelmann.doorbell.server.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.cronn.assertions.validationfile.normalization.IdNormalizer;
import de.cronn.assertions.validationfile.normalization.IncrementingIdProvider;
import de.cronn.assertions.validationfile.normalization.ValidationNormalizer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class SpringIntegrationTest extends BaseTest {

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected DoorbellDeviceRepository doorbellDeviceRepository;

    @Override
    public void assertWithFormattedJsonFile(String responseBody) {
        String formattedJson = formatJson(responseBody);
        super.assertWithJsonFile(formattedJson, getJsonIdNormalizer());
    }

    @Override
    public void assertWithFormattedJsonFile(String responseBody, ValidationNormalizer validationNormalizer) {
        String formattedJson = formatJson(responseBody);
        super.assertWithFormattedJsonFile(getJsonIdNormalizer().normalize(formattedJson), validationNormalizer);
    }

    @NotNull
    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private ValidationNormalizer getJsonIdNormalizer() {
        return new IdNormalizer(new IncrementingIdProvider(), "", false, "\"id\"\\s?:\\s?(\\d+)");
    }

    public IdNormalizer getErrorMessageIdNormalizer() {
        return new IdNormalizer(new IncrementingIdProvider(), "", false, "ID\\s(\\d+)");
    }

    protected ResultActions assertIsOkay(RequestBuilder createDoorbellBuzzerRequest) throws Exception {
        return mockMvc.perform(createDoorbellBuzzerRequest)
                .andExpect(status().isOk())
                .andExpect(this::assertWithFormattedJsonFile);
    }

    protected void assertBadRequest(RequestBuilder request, ValidationNormalizer validationNormalizer) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertWithFormattedJsonFile(result, validationNormalizer));
    }

    protected void assertNotFound(RequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(this::assertWithFormattedJsonFile);
    }

    protected void assertNotFound(RequestBuilder request, ValidationNormalizer validationNormalizer) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(result -> assertWithFormattedJsonFile(result, validationNormalizer));
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
                .name("name")
                .build();
        userRepository.saveAndFlush(user);
        return user;
    }

    protected void assertNoContent(RequestBuilder deleteUserRequest) throws Exception {
        mockMvc.perform(deleteUserRequest)
                .andExpect(status().isNoContent());
    }
}
