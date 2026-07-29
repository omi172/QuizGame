package com.quizapp.service;

import com.quizapp.dto.RegisterRequest;
import com.quizapp.model.Role;
import com.quizapp.model.User;
import com.quizapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        request.setRole("PARTICIPANT");
    }

    @Test
    @DisplayName("Should register user with encoded password and selected role")
    void registerUser_savesUserWithEncodedPasswordAndSelectedRole() {
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals("alice", saved.getUsername());
        assertEquals("hashed-password", saved.getPassword());
        assertEquals(Role.PARTICIPANT, saved.getRole());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should assign ADMIN role when requested")
    void registerUser_withAdminRole_isStoredAsAdmin() {
        request.setRole("ADMIN");
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals(Role.ADMIN, saved.getRole());
    }

    @Test
    @DisplayName("Should default to PARTICIPANT when role is invalid")
    void registerUser_withInvalidRole_defaultsToParticipant() {
        request.setRole("NOT_A_REAL_ROLE");

        // Use lenient() or stub only when saved, in case exception-handling logic changes
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals(Role.PARTICIPANT, saved.getRole());
    }

    @Test
    @DisplayName("Should delegate existsByUsername check to repository")
    void existsByUsername_delegatesToRepository() {
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        boolean exists = userService.existsByUsername("bob");

        assertTrue(exists);
        verify(userRepository, times(1)).existsByUsername("bob");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user is not found")
    void loadUserByUsername_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("ghost"));
        verify(userRepository, times(1)).findByUsername("ghost");
    }

    @Test
    @DisplayName("Should load user details and map role to authority string")
    void loadUserByUsername_mapsRoleToAuthority() {
        User user = new User();
        user.setUsername("carol");
        user.setEmail("carol@example.com");
        user.setPassword("hashed");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("carol");

        assertNotNull(details);
        assertEquals("carol", details.getUsername());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}