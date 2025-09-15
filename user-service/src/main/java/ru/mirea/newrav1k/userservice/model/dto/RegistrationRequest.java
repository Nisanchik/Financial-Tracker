package ru.mirea.newrav1k.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotNull(message = "error.username_cannot_be_null")
        @Email(message = "error.username_is_not_email")
        String username,

        @NotNull(message = "error.firstname_cannot_be_null")
        @Size(min = 3, message = "error.firstname_incorrect_size")
        String firstname,

        @NotNull(message = "error.lastname_cannot_be_null")
        @Size(min = 3, message = "error.lastname_incorrect_size")
        String lastname,

        @NotNull(message = "error.password_cannot_be_null")
        @Size(min = 6, message = "error.password_incorrect_size")
        String password,

        @NotNull(message = "error.password_cannot_be_null")
        @Size(min = 6, message = "error.password_incorrect_size")
        String confirmPassword
) {

}