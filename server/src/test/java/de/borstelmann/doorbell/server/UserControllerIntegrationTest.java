package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.controller.RequestUtils;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.openapi.model.UserRequest;
import de.borstelmann.doorbell.server.openapi.model.UserResponse;
import de.borstelmann.doorbell.server.test.SpringIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIntegrationTest extends SpringIntegrationTest {

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest().name("createUser");

        assertIsOkay(RequestUtils.createCreateUserRequest(objectMapper.writeValueAsString(userRequest)))
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    UserResponse userResponse = objectMapper.readValue(responseBody, UserResponse.class);

                    assertThat(userRepository.getById(userResponse.getId()))
                            .isNotNull();
                });
    }

    @Test
    void testGetAllUsers() throws Exception {
        createSampleUser();

        assertIsOkay(RequestUtils.createGetAllUsersRequest());
    }

    @Test
    void testGetUserById() throws Exception {
        User user = createSampleUser();

        assertIsOkay(RequestUtils.createGetUserByIdRequest(user.getId()));
    }

    @Test
    void testGetUserById_notFound() throws Exception {
        assertNotFound(RequestUtils.createGetUserByIdRequest(0L));
    }

    @Test
    void testDeleteUserById() throws Exception {
        User user = createSampleUser();

        assertNoContent(RequestUtils.createDeleteUserRequest(user.getId()));
    }

    @Test
    void testDeleteUserById_notFound() throws Exception {
        assertNotFound(RequestUtils.createDeleteUserRequest(0L));
    }
}