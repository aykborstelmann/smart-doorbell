package de.borstelmann.doorbell.server.test.authentication;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockOAuth2ScopeSecurityContextFactory.class)
public @interface WithMockOAuth2Scope {

    long DEFAULT_USER_ID = 0L;

    long userId() default DEFAULT_USER_ID;

    String oAuthId() default "oAuthId";

}
