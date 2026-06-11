package org.example.hotelbookingservice.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.dto.request.auth.LoginRequest;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.request.user.ChangePasswordRequest;
import org.example.hotelbookingservice.dto.request.user.CreateStaffRequest;
import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.dto.response.LoginResponse;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.example.hotelbookingservice.entity.*;
import org.example.hotelbookingservice.enums.UserRole;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.mapper.BookingMapper;
import org.example.hotelbookingservice.mapper.UserMapper;
import org.example.hotelbookingservice.repository.BookingRepository;
import org.example.hotelbookingservice.repository.RoleRepository;
import org.example.hotelbookingservice.repository.UserRepository;
import org.example.hotelbookingservice.repository.UserroleRepository;
import org.example.hotelbookingservice.security.JwtUtils;
import org.example.hotelbookingservice.services.IUserService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserroleRepository userroleRepository;
    private final JwtUtils jwtUtils;
    private final BookingMapper bookingMapper;


    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest registrationRequest) {

        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserRole roleEnum = UserRole.CUSTOMER;

        User userToSave = User.builder()
                .fullName(registrationRequest.getFullName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phone(registrationRequest.getPhone())
                .dob(registrationRequest.getDob())
                .activate(Boolean.TRUE)
                .userRoles(new LinkedHashSet<>())
                .build();


        User savedUser = userRepository.save(userToSave);

        //Find role in db
        Role roleEntity = roleRepository.findByName(roleEnum.name())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));


        UserroleId userroleId = new UserroleId();
        userroleId.setUserId(savedUser.getId());
        userroleId.setRoleId(roleEntity.getId());

        Userrole userrole = new Userrole();
        userrole.setId(userroleId);
        userrole.setUser(savedUser);
        userrole.setRole(roleEntity);

        userroleRepository.save(userrole);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        if (Boolean.FALSE.equals(user.getActivate())){
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD_EXCEPTION);
        }

        String token = jwtUtils.generateToken(user.getEmail());

        if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        //Take first role from Set<Userrole>
        Role roleEntity = user.getUserRoles().iterator().next().getRole();

        // Switch Role (String) to UserRole (Enum)
        UserRole roleEnum = UserRole.valueOf(roleEntity.getName());

        return LoginResponse.builder()
                .token(token)
                .role(roleEnum)
                .isActive(user.getActivate())
                .expirationTime("6 months")
                .build();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserResponse> userResponses = userMapper.toUserResponseList(users);

        return userMapper.toUserResponseList(users);
    }

    @Override
    public UserResponse getOwnAccountDetails() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));


        log.info("Inside getOwnAccountDetails user email is {}", email);

        UserResponse userResponse = userMapper.toUserResponse(user);

        return userMapper.toUserResponse(user);
    }

    @Override
    public User getCurrentLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));
    }

    @Override
    @Transactional
    public UserResponse updateOwnAccount(UserUpdateRequest request) {
        User existingUser = getCurrentLoggedInUser();
        log.info("Inside update user");

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            existingUser.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            existingUser.setPhone(request.getPhone());
        }

        if (request.getDob() != null) {
            existingUser.setDob(request.getDob());
        }

        User savedUser = userRepository.save(existingUser);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteOwnAccount() {
       User user = getCurrentLoggedInUser();
       userRepository.delete(user);
    }

    @Override
    public List<BookingResponse> getMyBookingHistory() {
        User user = getCurrentLoggedInUser();

        List<Booking> bookingList = bookingRepository.findByUserId(Long.valueOf(user.getId()));

        return bookingMapper.toBookingResponseList(bookingList);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        User user = getCurrentLoggedInUser();

        // Check if old password is correct
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        // Check that the new password does not match the old password
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new AppException(ErrorCode.PASSWORD_CHANGE_INVALID);
        }

        //Encrypt new password and save to DB
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void lockUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        user.setActivate(false);
        userRepository.save(user);
    }

    @Override
    public void unlockUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setActivate(true);
        userRepository.save(user);
    }

    @Override
    public void logout() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String email = auth.getName();
            log.info("User {} is logging out.", email);
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public UserResponse createStaff(CreateStaffRequest request) {
        // Validate: email must not already exist
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Validate: role must be RECEPTIONIST or MANAGER only
        UserRole roleEnum = request.getRole();
        if (roleEnum == UserRole.CUSTOMER || roleEnum == UserRole.ADMIN) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Create user entity
        User staffUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .dob(request.getDob())
                .activate(Boolean.TRUE)
                .userRoles(new LinkedHashSet<>())
                .build();

        User savedUser = userRepository.save(staffUser);

        // Find role entity in DB
        Role roleEntity = roleRepository.findByName(roleEnum.name())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_EXCEPTION));

        // Create user-role assignment
        UserroleId userroleId = new UserroleId();
        userroleId.setUserId(savedUser.getId());
        userroleId.setRoleId(roleEntity.getId());

        Userrole userrole = new Userrole();
        userrole.setId(userroleId);
        userrole.setUser(savedUser);
        userrole.setRole(roleEntity);

        userroleRepository.save(userrole);

        log.info("Staff account created: {} with role {}", request.getEmail(), roleEnum.name());

        return userMapper.toUserResponse(savedUser);
    }
}
