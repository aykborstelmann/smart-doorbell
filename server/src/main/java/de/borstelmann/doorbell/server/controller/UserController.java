package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.openapi.api.UserApi;
import de.borstelmann.doorbell.server.openapi.model.UserRequest;
import de.borstelmann.doorbell.server.openapi.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1")
@RestController
public class UserController implements UserApi {

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return null;
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long userId) {
        return null;
    }

}
