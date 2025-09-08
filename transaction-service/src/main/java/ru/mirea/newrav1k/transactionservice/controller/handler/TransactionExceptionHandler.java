package ru.mirea.newrav1k.transactionservice.controller.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mirea.newrav1k.transactionservice.exception.TransactionNotFoundException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionProcessingException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionServiceException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(getHttpStatus(exception),
                this.messageSource.getMessage(exception.getMessageCode(), new Object[0], exception.getMessageCode(), locale));
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

    private HttpStatus getHttpStatus(TransactionServiceException exception) {
        if (exception instanceof TransactionNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (exception instanceof TransactionProcessingException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}