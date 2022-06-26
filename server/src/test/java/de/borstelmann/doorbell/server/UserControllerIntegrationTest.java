package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.test.RequestUtils;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.test.authentication.OAuthIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserControllerIntegrationTest extends OAuthIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetAllUsers() throws Exception {
        createSampleUser();

        assertIsOkay(RequestUtils.createGetAllUsersRequest(obtainToken()));
    }

    @Test
    void testGetUserById() throws Exception {
        User user = createSampleUser();

        assertIsOkay(RequestUtils.createGetUserByIdRequest(user.getId(), obtainToken()));
    }

    @Test
    void testGetUserById_notFound() throws Exception {
        assertNotFound(RequestUtils.createGetUserByIdRequest(0L, obtainToken()));
    }

    @Test
    void testDeleteUserById() throws Exception {
        User user = createSampleUser();

        assertNoContent(RequestUtils.createDeleteUserRequest(user.getId(), obtainToken()));
    }

    @Test
    void testDeleteUserById_notFound() throws Exception {
        assertNotFound(RequestUtils.createDeleteUserRequest(0L, obtainToken()));
    }
}