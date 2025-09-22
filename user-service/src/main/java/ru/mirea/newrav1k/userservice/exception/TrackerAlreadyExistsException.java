package ru.mirea.newrav1k.userservice.exception;

import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.TRACKER_ALREADY_EXISTS;

public class TrackerAlreadyExistsException extends UserServiceException {

    public TrackerAlreadyExistsException() {
        super(TRACKER_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }

}