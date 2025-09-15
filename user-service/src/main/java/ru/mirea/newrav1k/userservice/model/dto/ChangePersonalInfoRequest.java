package ru.mirea.newrav1k.userservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePersonalInfoRequest(
        @NotNull(message = "error.firstname_cannot_be_null")
        @Size(min = 3, message = "error.firstname_incorrect_size")
        String firstname,

        @NotNull(message = "error.lastname_cannot_be_null")
        @Size(min = 3, message = "error.lastname_incorrect_size")
        String lastname
) {

}