package de.borstelmann.doorbell.server.domain.model.security;

import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.error.BadRequestException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Optional;

@Getter
public class CustomUserSession extends AbstractAuthenticationToken {
    final private User user;
    final private Jwt jwt;

    public CustomUserSession(User user, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.jwt;
    }

    @Override
    public Object getPrincipal() {
        return this.user;
    }

    @Nullable
    private static CustomUserSession getCurrentUserSession() {
        return (CustomUserSession) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(authentication -> authentication instanceof CustomUserSession)
                .orElse(null);
    }

    @NotNull
    public static User getCurrentUserOrThrow() {
        return Optional.ofNullable(getCurrentUserSession())
                .map(CustomUserSession::getUser)
                .orElseThrow(() -> new BadRequestException("Current session does not have a user"));
    }
}
