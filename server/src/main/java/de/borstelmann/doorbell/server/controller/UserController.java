package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.openapi.api.UserApi;
import de.borstelmann.doorbell.server.openapi.model.UserRequest;
import de.borstelmann.doorbell.server.openapi.model.UserResponse;
import de.borstelmann.doorbell.server.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final ModelMapper modelMapper;
    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
        User userToCreate = convertToDomainModel(userRequest);
        User createdUser = userService.createUser(userToCreate);
        UserResponse userResponse = convertToResponse(createdUser);
        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responseUsers = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseUsers);
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long userId) {
        User user = userService.getUserById(userId);
        UserResponse userResponse = convertToResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    private User convertToDomainModel(UserRequest userRequest) {
        return modelMapper.map(userRequest, User.class);
    }

    private UserResponse convertToResponse(User createdUser) {
        return modelMapper.map(createdUser, UserResponse.class);
    }
}
