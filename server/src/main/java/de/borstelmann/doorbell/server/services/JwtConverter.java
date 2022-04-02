package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.services.UserService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserService userService;

    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        User user = userService.getOrCreateUserByOAuthId(jwt.getSubject());
        return new CustomUserSession(user, jwt, Collections.emptyList());
    }

}
