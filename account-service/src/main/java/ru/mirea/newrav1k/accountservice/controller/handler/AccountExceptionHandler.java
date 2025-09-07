package ru.mirea.newrav1k.accountservice.controller.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mirea.newrav1k.accountservice.exception.AccountBalanceException;
import ru.mirea.newrav1k.accountservice.exception.AccountNotFoundException;
import ru.mirea.newrav1k.accountservice.exception.AccountServiceException;
import ru.mirea.newrav1k.accountservice.exception.AccountStateException;
import ru.mirea.newrav1k.accountservice.exception.ConcurrentModificationException;
import ru.mirea.newrav1k.accountservice.exception.InsufficientBalanceException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.GENERIC_ERROR;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class AccountExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(AccountServiceException.class)
    public ResponseEntity<ProblemDetail> handleAccountServiceException(AccountServiceException exception, Locale locale) {
        String message = this.messageSource.getMessage(exception.getMessageCode(), exception.getArgs(), exception.getMessageCode(), locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(getHttpStatus(exception), message);
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException exception, Locale locale) {
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

        // TODO: Добавить кастомный ответ с подробной информацией об ошибке и поле

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
        if (exception instanceof InsufficientBalanceException ||
                exception instanceof AccountNotFoundException || exception instanceof AccountBalanceException) {
            return HttpStatus.BAD_REQUEST;
        } else if (exception instanceof ConcurrentModificationException || exception instanceof AccountStateException) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}