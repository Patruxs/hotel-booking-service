package org.example.hotelbookingservice.mapper;

import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.example.hotelbookingservice.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {


    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    // --- 2. Update Request -> Entity (Dùng cho API Update Profile) ---
    // Logic: Chỉ cập nhật các trường không null từ Request vào Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)          // Không update ID
    @Mapping(target = "email", ignore = true)       // Không update Email
    @Mapping(target = "password", ignore = true)    // Không update Password
    @Mapping(target = "activate", ignore = true)    // Không update trạng thái kích hoạt
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "hotels", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);

    // --- 3. Registration Request -> Entity (Dùng cho API Register) --
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activate", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "hotels", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    User toUser(RegisterRequest request);


}
