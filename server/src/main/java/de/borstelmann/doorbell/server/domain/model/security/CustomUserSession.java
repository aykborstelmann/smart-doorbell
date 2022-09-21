package de.borstelmann.doorbell.server.domain.model.security;

import de.borstelmann.doorbell.server.domain.model.User;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

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

}
