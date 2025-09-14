package ru.mirea.newrav1k.userservice.model.dto;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String username,
        String firstname,
        String lastname
) {

}