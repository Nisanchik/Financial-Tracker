package ru.mirea.newrav1k.userservice.model.dto;

import java.util.UUID;

public record TrackerResponse(
        UUID id,
        String username,
        String firstname,
        String lastname
) {

}