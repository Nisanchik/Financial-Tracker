package ru.mirea.newrav1k.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotNull
        @Email
        String username,

        @NotNull
        @Size(min = 6)
        String firstname,

        @NotNull
        @Size(min = 6)
        String lastname,

        @NotNull
        @Size(min = 6)
        String password,

        @NotNull
        @Size(min = 6)
        String confirmPassword
) {

}