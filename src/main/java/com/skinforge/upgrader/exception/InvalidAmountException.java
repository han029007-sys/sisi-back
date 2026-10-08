package com.skinforge.upgrader.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(BigDecimal amount) {
        super("Invalid amount: " + amount);
    }
}
