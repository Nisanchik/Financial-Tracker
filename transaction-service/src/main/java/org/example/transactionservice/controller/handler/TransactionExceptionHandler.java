package org.example.transactionservice.controller.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transactionservice.exception.TransactionNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class TransactionExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTransactionNotFoundException(TransactionNotFoundException exception, Locale locale) {
        log.error(exception.getMessage(), exception);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                this.messageSource.getMessage(exception.getMessage(), new Object[0], exception.getMessage(), locale));
        return ResponseEntity.of(problemDetail).build();
    }

}