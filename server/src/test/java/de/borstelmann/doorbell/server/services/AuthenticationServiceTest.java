package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.error.BadRequestException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.doReturn;

@MockitoSettings
class AuthenticationServiceTest {

    @Spy
    private AuthenticationService authenticationService;

    @Test
    void testGetCurrentUserOrThrow_noUser() {
        BadRequestException badRequestException = catchThrowableOfType(() -> authenticationService.getCurrentUserOrThrow(), BadRequestException.class);
        assertThat(badRequestException)
                .hasMessage("Current session does not have a user");
    }

    @Test
    void testGetCurrentUserOrThrow_withUser() {
        User expectedUser = new User();
        doReturn(new CustomUserSession(expectedUser, null, Collections.emptyList())).when(authenticationService).getAuthentication();

        User currentUser = authenticationService.getCurrentUserOrThrow();

        assertThat(currentUser).isEqualTo(expectedUser);
    }
}