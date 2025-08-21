package com.footballstore.orders.utils.exceptions;

public class StockExceededException extends RuntimeException {
    public StockExceededException(String message) {
        super(message);
    }
}
