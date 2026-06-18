package org.example.hotelbookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    @Schema(description = "ID người dùng")
    private Integer id;

    @Schema(description = "Họ và tên", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Email đăng nhập", example = "user@example.com")
    private String email;

    @Schema(description = "Số điện thoại", example = "0987654321")
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày sinh", example = "1995-05-20")
    private LocalDate dob;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo tài khoản")
    private LocalDateTime createdAt;

    @Schema(description = "Trạng thái hoạt động (True: Đang hoạt động, False: Bị khóa)", example = "true")
    private Boolean activate;

}
