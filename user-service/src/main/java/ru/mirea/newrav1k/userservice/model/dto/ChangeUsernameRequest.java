package ru.mirea.newrav1k.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ChangeUsernameRequest(
        @NotNull(message = "error.username_cannot_be_null")
        @Email(message = "error.username_is_not_email")
        String username,

        @NotNull(message = "error.password_cannot_be_null")
        String confirmPassword
) {

}