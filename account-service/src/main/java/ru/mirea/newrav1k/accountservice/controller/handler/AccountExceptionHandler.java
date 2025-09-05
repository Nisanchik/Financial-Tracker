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
import ru.mirea.newrav1k.accountservice.exception.AccountServiceException;
import ru.mirea.newrav1k.accountservice.exception.ConcurrentModificationException;
import ru.mirea.newrav1k.accountservice.exception.InsufficientBalanceException;

import java.util.Locale;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.GENERIC_ERROR;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class AccountExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(AccountServiceException.class)
    public ResponseEntity<ProblemDetail> handleAccountServiceException(AccountServiceException exception, Locale locale) {
        String message = this.messageSource.getMessage(exception.getMessage(), exception.getArgs(), exception.getMessage(), locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(getHttpStatus(exception), message);
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception exception, Locale locale) {
        log.error(exception.getMessage(), exception);
        String message = this.messageSource.getMessage(GENERIC_ERROR, null,
                "Internal server error", locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, message);
        return ResponseEntity.of(problemDetail).build();
    }

    private HttpStatus getHttpStatus(AccountServiceException exception) {
        if (exception instanceof InsufficientBalanceException) {
            return HttpStatus.BAD_REQUEST;
        } else if (exception instanceof ConcurrentModificationException) {
            return HttpStatus.CONFLICT;
        } else if (exception instanceof AccountBalanceException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}