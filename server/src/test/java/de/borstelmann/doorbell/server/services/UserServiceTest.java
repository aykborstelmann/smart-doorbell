package de.borstelmann.doorbell.server.services;

import de.borstelmann.doorbell.server.domain.model.User;
import de.borstelmann.doorbell.server.domain.repository.UserRepository;
import de.borstelmann.doorbell.server.error.NotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetAllUsers() {
        List<User> usersToReturn = List.of(
                User.builder()
                        .id(0L)
                        .build(),
                User.builder()
                        .id(1L)
                        .build()
        );

        Mockito.when(userRepository.findAll()).thenReturn(usersToReturn);

        List<User> users = userService.getAllUsers();

        assertThat(users).isEqualTo(usersToReturn);
    }

    @Test
    void testGetUserById() {
        User userToReturn = User.builder()
                .id(0L)
                .build();

        Mockito.when(userRepository.findById(0L)).thenReturn(Optional.of(userToReturn));

        User users = userService.getUserById(0L);

        assertThat(users).isEqualTo(userToReturn);
    }

    @Test
    void testGetUserById_notFound() {
        Mockito.when(userRepository.findById(0L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(0L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with ID 0 not found");
    }

    @Test
    void testDeleteUserById() {
        Mockito.when(userRepository.existsById(0L)).thenReturn(true);
        userService.deleteUserById(0L);
        Mockito.verify(userRepository).deleteById(0L);
    }

    @Test
    void testDeleteUserById_notFound() {
        Mockito.when(userRepository.existsById(0L)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUserById(0L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with ID 0 not found");
        Mockito.verify(userRepository, never()).deleteById(0L);
    }

    @Test
    void testGetOrCreateUserByOAuthId() {
        String oAuthId = "oAuthId";
        doAnswer(inv -> {
            User user = inv.getArgument(0, User.class);
            user.setId(0L);
            return user;
        }).when(userRepository).save(any());

        userService.getOrCreateUserByOAuthId(oAuthId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue())
                .extracting(User::getOAuthId)
                .isEqualTo(oAuthId);
    }

    @Test
    void testEnableGoogleHomeForUser() {
        Long userId = 1L;
        User user = new User();
        doReturn(Optional.of(user)).when(userRepository).findById(userId);

        userService.enableGoogleHomeForUser(userId);

        assertThat(user.isGoogleHomeConnected()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void testDisableGoogleHomeForUser() {
        Long userId = 1L;
        User user = new User();
        doReturn(Optional.of(user)).when(userRepository).findById(userId);

        userService.disableGoogleHomeForUser(userId);

        assertThat(user.isGoogleHomeConnected()).isFalse();
        verify(userRepository).save(user);
    }
}