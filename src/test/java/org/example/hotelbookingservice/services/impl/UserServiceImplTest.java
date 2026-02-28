package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.example.hotelbookingservice.entity.Role;
import org.example.hotelbookingservice.entity.User;
import org.example.hotelbookingservice.entity.Userrole;
import org.example.hotelbookingservice.enums.UserRole;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.mapper.UserMapper;
import org.example.hotelbookingservice.repository.RoleRepository;
import org.example.hotelbookingservice.repository.UserRepository;
import org.example.hotelbookingservice.repository.UserroleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserroleRepository userroleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest mockRegisterRequest;
    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRegisterRequest = new RegisterRequest();
        mockRegisterRequest.setFullName("Test User");
        mockRegisterRequest.setEmail("test@example.com");
        mockRegisterRequest.setPassword("password123");
        mockRegisterRequest.setPhone("1234567890");
        mockRegisterRequest.setDob(LocalDate.of(1990, 1, 1));

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setFullName("Test User");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword123");

        mockRole = new Role();
        mockRole.setId(1);
        mockRole.setName(UserRole.CUSTOMER.name());
    }

    @Test
    void registerUser_ValidRequest_Success() {
        // Given
        when(userRepository.findByEmail(mockRegisterRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(mockRegisterRequest.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(roleRepository.findByName(UserRole.CUSTOMER.name())).thenReturn(Optional.of(mockRole));
        when(userroleRepository.save(any(Userrole.class))).thenReturn(new Userrole());

        UserResponse mockUserResponse = new UserResponse();
        mockUserResponse.setId(1);
        mockUserResponse.setEmail("test@example.com");
        when(userMapper.toUserResponse(mockUser)).thenReturn(mockUserResponse);

        // When
        UserResponse result = userService.registerUser(mockRegisterRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(passwordEncoder, times(1)).encode(mockRegisterRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userroleRepository, times(1)).save(any(Userrole.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsAppException() {
        // Given
        when(userRepository.findByEmail(mockRegisterRequest.getEmail())).thenReturn(Optional.of(mockUser));

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(mockRegisterRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_EXISTED);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userroleRepository, never()).save(any(Userrole.class));
    }

    @Test
    void registerUser_RoleNotFound_ThrowsAppException() {
        // Given
        when(userRepository.findByEmail(mockRegisterRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(mockRegisterRequest.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(roleRepository.findByName(UserRole.CUSTOMER.name())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(mockRegisterRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_EXCEPTION);

        verify(passwordEncoder, times(1)).encode(mockRegisterRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userroleRepository, never()).save(any(Userrole.class));
    }
}
