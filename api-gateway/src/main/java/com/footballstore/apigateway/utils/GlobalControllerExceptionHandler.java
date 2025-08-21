package com.footballstore.apigateway.utils;

import com.footballstore.apigateway.utils.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public HttpErrorInfo handleNotFoundException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, ex);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler({
            InvalidInputException.class,
//            StockExceededException.class,
//            OrderStateException.class,
            InvalidWarehouseCapacityException.class,
            InvalidApparelPricingException.class
    })
    public HttpErrorInfo handleUnprocessableEntity(WebRequest request, Exception ex) {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, ex);
    }

    private HttpErrorInfo createHttpErrorInfo(HttpStatus status, WebRequest request, Exception ex) {
        String path = request.getDescription(false);
        String message = ex.getMessage();
        log.debug("Returning HTTP status: {} for path: {}, message: {}", status, path, message);
        return new HttpErrorInfo(status, path, message);
    }
}
