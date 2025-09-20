package ru.mirea.newrav1k.transactionservice.controller.handler;

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
import ru.mirea.newrav1k.transactionservice.exception.TransactionServiceException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class TransactionExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(TransactionServiceException.class)
    public ResponseEntity<ProblemDetail> handleTransactionServiceException(TransactionServiceException exception, Locale locale) {
        log.error(exception.getMessage(), exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatus(),
                this.messageSource.getMessage(exception.getMessage(), new Object[0], exception.getMessage(), locale));
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException exception,
                                                                   HttpServletRequest httpServletRequest,
                                                                   Locale locale) {
        log.error(exception.getMessage(), exception);
        List<String> errors = exception.getBindingResult().getAllErrors()
                .stream()
                .map(error -> {
                    if (error.getDefaultMessage() != null && !error.getDefaultMessage().isEmpty()) {
                        return this.messageSource.getMessage(
                                error.getDefaultMessage(), new Object[0], error.getDefaultMessage(), locale);
                    }
                    return "Некорректный " + error.getObjectName();
                })
                .toList();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setProperties(Map.of("errors", errors));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setInstance(URI.create(httpServletRequest.getRequestURI()));

        return ResponseEntity.of(problemDetail).build();
    }

}