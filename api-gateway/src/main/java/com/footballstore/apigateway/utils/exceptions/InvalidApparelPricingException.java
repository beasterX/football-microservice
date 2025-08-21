package com.footballstore.apigateway.utils.exceptions;

public class InvalidApparelPricingException extends RuntimeException {
    public InvalidApparelPricingException(String message) {
        super(message);
    }
}
