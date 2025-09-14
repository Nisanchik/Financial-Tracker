package ru.mirea.newrav1k.userservice.controller.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mirea.newrav1k.userservice.exception.UserServiceException;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class AuthenticationExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ProblemDetail> handlePasswordMatchException(UserServiceException exception,
                                                                      HttpServletRequest request,
                                                                      Locale locale) {
        log.error(exception.getMessage(), exception);
        String detail = this.messageSource.getMessage(exception.getMessage(), new Object[0], exception.getMessage(), locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatus(), detail);
        problemDetail.setProperties(Map.of("timestamp", Instant.now()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.of(problemDetail).build();
    }

}