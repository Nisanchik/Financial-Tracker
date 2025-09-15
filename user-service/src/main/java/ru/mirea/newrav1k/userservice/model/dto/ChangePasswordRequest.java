package ru.mirea.newrav1k.userservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotNull(message = "error.password_cannot_be_null")
        @Size(min = 6, message = "error.password_incorrect_size")
        String password,

        @NotNull(message = "error.password_cannot_be_null")
        @Size(min = 6, message = "error.password_incorrect_size")
        String confirmPassword
) {

}
