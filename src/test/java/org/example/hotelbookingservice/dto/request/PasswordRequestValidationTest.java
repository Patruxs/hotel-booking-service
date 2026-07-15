package org.example.hotelbookingservice.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.request.auth.ResetPasswordRequest;
import org.example.hotelbookingservice.dto.request.user.ChangePasswordRequest;
import org.example.hotelbookingservice.dto.request.user.CreateStaffRequest;
import org.example.hotelbookingservice.enums.UserRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void passwordRequests_whenPasswordHasSevenCharacters_shouldRejectAtRequestValidation() {
        assertHasPasswordSizeViolation(registerRequest("Abc1!xy"), "password");
        assertHasPasswordSizeViolation(resetPasswordRequest("Abc1!xy"), "newPassword");
        assertHasPasswordSizeViolation(changePasswordRequest("Abc1!xy"), "newPassword");
        assertHasPasswordSizeViolation(createStaffRequest("Abc1!xy"), "password");
    }

    private static void assertHasPasswordSizeViolation(Object request, String propertyPath) {
        Set<ConstraintViolation<Object>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath()).hasToString(propertyPath);
                    assertThat(violation.getMessage()).contains("at least 8 characters");
                });
    }

    private static RegisterRequest registerRequest(String password) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyen Van A");
        request.setEmail("customer@example.com");
        request.setPhone("0987654321");
        request.setPassword(password);
        request.setDob(LocalDate.of(1995, 5, 20));
        return request;
    }

    private static ResetPasswordRequest resetPasswordRequest(String password) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword(password);
        request.setConfirmPassword(password);
        return request;
    }

    private static ChangePasswordRequest changePasswordRequest(String password) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPassword1!");
        request.setNewPassword(password);
        request.setConfirmPassword(password);
        return request;
    }

    private static CreateStaffRequest createStaffRequest(String password) {
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFullName("Tran Van Receptionist");
        request.setEmail("receptionist@hotel.com");
        request.setPhone("0901234567");
        request.setPassword(password);
        request.setDob(LocalDate.of(1995, 5, 20));
        request.setRole(UserRole.RECEPTIONIST);
        return request;
    }
}
