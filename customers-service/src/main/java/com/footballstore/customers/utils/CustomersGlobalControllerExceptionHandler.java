package com.footballstore.customers.utils;

import com.footballstore.customers.utils.exceptions.DuplicateEmailException;
import com.footballstore.customers.utils.exceptions.InvalidInputException;
import com.footballstore.customers.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class CustomersGlobalControllerExceptionHandler {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public CustomersHttpErrorInfo handleNotFoundException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(NOT_FOUND, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(DuplicateEmailException.class)
    public CustomersHttpErrorInfo handleDuplicateEmailException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public CustomersHttpErrorInfo handleInvalidInputException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }

    private CustomersHttpErrorInfo createHttpErrorInfo(HttpStatus status, WebRequest req, Exception ex) {
        String path = req.getDescription(false);
        String message = ex.getMessage();
        log.debug("Returning HTTP {} for {}: {}", status, path, message);
        return new CustomersHttpErrorInfo(status, path, message);
    }
}
