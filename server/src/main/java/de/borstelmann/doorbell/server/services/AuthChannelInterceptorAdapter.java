package de.borstelmann.doorbell.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    private final WebSocketAuthenticatorService webSocketAuthenticatorService;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        final StompHeaderAccessor accessor = Objects.requireNonNull(MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class));

        if (StompCommand.CONNECT == accessor.getCommand()) {
            authenticateUser(accessor);
        }

        return message;
    }

    private void authenticateUser(StompHeaderAccessor accessor) {
        final String username = accessor.getLogin();
        final String password = accessor.getPasscode();

        final UsernamePasswordAuthenticationToken user = webSocketAuthenticatorService.getAuthenticatedOrFail(username, password);
        accessor.setUser(user);

        log.info("Connected " + user);
    }

}
