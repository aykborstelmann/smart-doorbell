package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.DoorbellDevice;
import de.borstelmann.doorbell.server.domain.repository.DoorbellDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class WebSocketAuthenticatorService {
    private final DoorbellDeviceRepository doorbellDeviceRepository;

    public UsernamePasswordAuthenticationToken getAuthenticatedOrFail(final String username, final String password) throws AuthenticationException {
        validateUsernameNotEmpty(username);

        DoorbellDevice doorbellDevice = findUserOrThrow(username, password);
        return new UsernamePasswordAuthenticationToken(
                doorbellDevice.getId(),
                null,
                Collections.emptyList()
        );
    }

    private void validateUsernameNotEmpty(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Username was null or empty.");
        }
    }

    private DoorbellDevice findUserOrThrow(String username, String password) {
        Long id = Long.valueOf(username);
        return doorbellDeviceRepository.findById(id)
                .orElseThrow(() -> new BadCredentialsException(String.format("Doorbell with ID %s could not be found", username)));
    }

}
