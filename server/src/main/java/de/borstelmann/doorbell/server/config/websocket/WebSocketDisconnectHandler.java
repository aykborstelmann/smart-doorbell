package de.borstelmann.doorbell.server.config.websocket;

import de.borstelmann.doorbell.server.services.DoorbellStateChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketDisconnectHandler implements ApplicationListener<SessionDisconnectEvent> {

    private final SimpUserRegistry simpUserRegistry;
    private final DoorbellStateChangeService doorbellStateChangeService;

    @Override
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
}
