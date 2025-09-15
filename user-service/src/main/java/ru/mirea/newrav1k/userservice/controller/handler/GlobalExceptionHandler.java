package ru.mirea.newrav1k.userservice.controller.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException exception,
                                                                   HttpServletRequest request,
                                                                   Locale locale) {
        log.error(exception.getMessage(), exception);
        String detail = this.messageSource.getMessage(exception.getMessage(), new Object[0], exception.getMessage(), locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

        problemDetail.setProperties(Map.of("timestamp", Instant.now()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.of(problemDetail).build();
    }

}