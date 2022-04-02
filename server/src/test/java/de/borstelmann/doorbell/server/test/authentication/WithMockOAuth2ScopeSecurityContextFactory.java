package de.borstelmann.doorbell.server.test.authentication;

import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession;
import de.borstelmann.doorbell.server.domain.model.User;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

public class WithMockOAuth2ScopeSecurityContextFactory implements WithSecurityContextFactory<WithMockOAuth2Scope> {
    @Override
    public SecurityContext createSecurityContext(WithMockOAuth2Scope annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = User.builder()
                .id(annotation.userId())
                .oAuthId(annotation.oAuthId())
                .build();

        context.setAuthentication(new CustomUserSession(user, null, Collections.emptyList()));
        return context;
    }
}
