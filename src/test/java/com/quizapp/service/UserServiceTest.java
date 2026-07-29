package com.quizapp.service;

import com.quizapp.dto.RegisterRequest;
import com.quizapp.model.Role;
import com.quizapp.model.User;
import com.quizapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

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
    void registerUser_savesUserWithEncodedPasswordAndSelectedRole() {
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertEquals("alice", saved.getUsername());
        assertEquals("hashed-password", saved.getPassword());
        assertEquals(Role.PARTICIPANT, saved.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_withAdminRole_isStoredAsAdmin() {
        request.setRole("ADMIN");
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertEquals(Role.ADMIN, saved.getRole());
    }

    @Test
    void registerUser_withInvalidRole_defaultsToParticipant() {
        request.setRole("NOT_A_REAL_ROLE");
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertEquals(Role.PARTICIPANT, saved.getRole());
    }

    @Test
    void existsByUsername_delegatesToRepository() {
        when(userRepository.existsByUsername("bob")).thenReturn(true);
        assertTrue(userService.existsByUsername("bob"));
    }

    @Test
    void loadUserByUsername_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("ghost"));
    }

    @Test
    void loadUserByUsername_mapsRoleToAuthority() {
        User user = new User("carol", "carol@example.com", "hashed", Role.ADMIN);
        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("carol");

        assertEquals("carol", details.getUsername());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
