package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> NotFoundException.createUserNotFoundException(id));
    }

    public void deleteUserById(Long id) {
        boolean exists = userRepository.existsById(id);
        if (!exists) {
            throw NotFoundException.createUserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    public User getOrCreateUserByOAuthId(String oAuthId) {
        return userRepository.findByOAuthId(oAuthId)
                .orElseGet(() -> createUserWithOAuthId(oAuthId));
    }

    private User createUserWithOAuthId(String oAuthId) {
        return userRepository.save(User.builder().oAuthId(oAuthId).build());
    }

    public void enableGoogleHomeForUser(Long id) {
        User user = getUserById(id);
        user.setGoogleHomeConnected(true);
        userRepository.save(user);
    }

    public void disableGoogleHomeForUser(Long id) {
        User user = getUserById(id);
        user.setGoogleHomeConnected(false);
        userRepository.save(user);
    }
}
