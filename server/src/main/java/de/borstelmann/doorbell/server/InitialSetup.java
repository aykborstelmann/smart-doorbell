package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class InitialSetup {

    private final UserRepository userRepository;

    @PostConstruct
    public void createUser() {
        userRepository.save(new User());
    }
}
