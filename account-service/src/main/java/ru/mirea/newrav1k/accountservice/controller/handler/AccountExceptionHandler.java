package ru.mirea.newrav1k.accountservice.controller.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mirea.newrav1k.accountservice.exception.AccountBalanceException;
import ru.mirea.newrav1k.accountservice.exception.AccountNotFoundException;
import ru.mirea.newrav1k.accountservice.exception.InsufficientBalanceException;

import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class AccountExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAccountNotFoundException(AccountNotFoundException exception,
                                                                        Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                this.messageSource.getMessage(exception.getMessage(), new Object[0], "account.not.found", locale));
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientBalanceException(InsufficientBalanceException exception,
                                                                            Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                this.messageSource.getMessage(exception.getMessage(), new Object[0], "insufficient.balance", locale));
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(AccountBalanceException.class)
    public ResponseEntity<ProblemDetail> handleAccountBalanceException(AccountBalanceException exception,
                                                                       Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                this.messageSource.getMessage(exception.getMessage(), new Object[0], exception.getMessage(), locale));
        return ResponseEntity.of(problemDetail).build();
    }

}