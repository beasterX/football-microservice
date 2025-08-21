package com.footballstore.apigateway.utils.exceptions;

public class InvalidWarehouseCapacityException extends RuntimeException {
    public InvalidWarehouseCapacityException(String message) {
        super(message);
    }
}
