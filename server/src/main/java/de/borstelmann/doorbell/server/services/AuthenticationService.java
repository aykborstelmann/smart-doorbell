package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.error.BadRequestException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationService {

    @NotNull
    public User getCurrentUserOrThrow() {
        return Optional.ofNullable(getCurrentUserSession())
                .map(CustomUserSession::getUser)
                .orElseThrow(() -> new BadRequestException("Current session does not have a user"));
    }

    @Nullable
    private CustomUserSession getCurrentUserSession() {
        return (CustomUserSession) Optional.ofNullable(getAuthentication())
                .filter(authentication -> authentication instanceof CustomUserSession)
                .orElse(null);
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
