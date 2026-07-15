package org.example.hotelbookingservice.mapper;

import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.example.hotelbookingservice.entity.User;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {


    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    // --- 2. Update Request -> Entity (Used for Update Profile API) ---
    // Logic: Only update non-null fields from Request to Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)          // Do not update ID
    @Mapping(target = "email", ignore = true)       // Do not update Email
    @Mapping(target = "passwordHash", ignore = true)    // Do not update Password
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "activate", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)    // Do not update activation status
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "hotels", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);

    // --- 3. Registration Request -> Entity (Used for Register API) --
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "hotels", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    User toUser(RegisterRequest request);

    default LocalDateTime map(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

}
