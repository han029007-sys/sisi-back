package com.skinforge.upgrader.exception;

public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException(long id) {
        super("Payment with id " + id + " not found");
    }
}
