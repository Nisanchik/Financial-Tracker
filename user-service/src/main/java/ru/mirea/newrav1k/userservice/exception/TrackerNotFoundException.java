package ru.mirea.newrav1k.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.TRACKER_NOT_FOUND;

@Getter
public class TrackerNotFoundException extends UserServiceException {

    public TrackerNotFoundException() {
        super(TRACKER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

}