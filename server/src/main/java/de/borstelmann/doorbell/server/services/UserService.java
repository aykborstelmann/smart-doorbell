package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.error.NotFoundException;
import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

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

}
