package com.gym.gym_api.service;

import com.gym.gym_api.dto.user.UserRequest;
import com.gym.gym_api.dto.user.UserResponse;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder
        );
    }

    @Test
    void createUser_shouldRejectDuplicateUsername() {

        UserRequest request = mock(UserRequest.class);

        when(request.getUsername())
                .thenReturn("john");

        when(userRepository.existsByUsername("john"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
    }

    @Test
    void createUser_shouldRejectDuplicateEmail() {

        UserRequest request = mock(UserRequest.class);

        when(request.getUsername())
                .thenReturn("john");

        when(request.getEmail())
                .thenReturn("john@example.com");

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
    }

    @Test
    void createUser_shouldCreateUserAndHashPassword() {

        UserRequest request = mock(UserRequest.class);

        when(request.getUsername())
                .thenReturn("john");

        when(request.getEmail())
                .thenReturn("john@example.com");

        when(request.getPassword())
                .thenReturn("password123");

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        User savedUser = mock(User.class);

        when(savedUser.getId()).thenReturn(1L);
        when(savedUser.getUsername()).thenReturn("john");
        when(savedUser.getEmail()).thenReturn("john@example.com");
        when(savedUser.getCreatedAt()).thenReturn(null);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response =
                userService.createUser(request);

        assertEquals(1L, response.getId());
        assertEquals("john", response.getUsername());
        assertEquals(
                "john@example.com",
                response.getEmail()
        );

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void createUser_shouldSaveHashedPassword() {

        UserRequest request = mock(UserRequest.class);

        when(request.getUsername())
                .thenReturn("john");

        when(request.getEmail())
                .thenReturn("john@example.com");

        when(request.getPassword())
                .thenReturn("password123");

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        User savedUser = new User();
        savedUser.setUsername("john");
        savedUser.setEmail("john@example.com");
        savedUser.setPasswordHash("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();

        assertEquals(
                "hashed-password",
                userToSave.getPasswordHash()
        );
    }
}