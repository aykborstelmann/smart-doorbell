package de.borstelmann.doorbell.server.config.websocket;

import de.borstelmann.doorbell.server.services.DoorbellStateChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketConnectionChangeHandler {

    private final SimpUserRegistry simpUserRegistry;
    private final DoorbellStateChangeService doorbellStateChangeService;

    @EventListener
    public void onApplicationEvent(SessionDisconnectEvent event) {
        Principal user = event.getUser();

        Optional.ofNullable(user)
                .ifPresent(this::setDisconnectedIfLastSession);

        log.info("Disconnected " + user);
    }

    private void setDisconnectedIfLastSession(Principal user) {
        String username = user.getName();
        boolean hasRemainingSessions = simpUserRegistry.getUser(username) != null;
        if (hasRemainingSessions) {
            return;
        }

        Long doorbellId = Long.valueOf(username);
        doorbellStateChangeService.setIsConnected(doorbellId, false);
    }

    @EventListener
    public void onApplicationEvent(SessionConnectEvent event) {
        Principal user = event.getUser();

        Optional.ofNullable(user)
                .map(Principal::getName)
                .map(Long::parseLong)
                .ifPresent(id -> doorbellStateChangeService.setIsConnected(id, true));

        log.info("Disconnected " + user);
    }
}
