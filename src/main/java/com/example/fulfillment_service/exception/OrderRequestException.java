package com.example.fulfillment_service.exception;

public class OrderRequestException extends RuntimeException {
    public OrderRequestException(String message) {
        super(message);
    }
}
