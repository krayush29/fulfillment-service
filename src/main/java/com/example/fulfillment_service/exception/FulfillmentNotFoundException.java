package com.example.fulfillment_service.exception;

public class FulfillmentNotFoundException extends RuntimeException {
    public FulfillmentNotFoundException(String message) {
        super(message);
    }
}
