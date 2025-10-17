package ru.mirea.nisanchik.categoryservice.controller.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mirea.nisanchik.categoryservice.exception.CategoryServiceException;

import java.net.URI;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CategoryExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(CategoryServiceException.class)
    public ResponseEntity<ProblemDetail> handleCategoryServiceException(CategoryServiceException exception,
                                                                        HttpServletRequest request, Locale locale) {
        log.error(exception.getMessage(), exception);
        String localizedMessage = this.messageSource.getMessage(exception.getMessage(), new Object[0], exception.getMessage(), locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatus(), localizedMessage);
        problemDetail.setProperty("timestamp", System.currentTimeMillis());
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception,
                                                                               HttpServletRequest request, Locale locale) {
        log.error(exception.getMessage(), exception);
        String localizedMessage = exception.getBindingResult().getAllErrors()
                .stream()
                .filter(error -> error.getDefaultMessage() != null)
                .map(error -> this.messageSource.getMessage(error.getDefaultMessage(), new Object[0], error.getDefaultMessage(), locale))
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, localizedMessage);
        problemDetail.setProperty("timestamp", System.currentTimeMillis());
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.of(problemDetail).build();
    }

}