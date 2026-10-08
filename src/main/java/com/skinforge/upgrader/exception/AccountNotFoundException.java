package com.skinforge.upgrader.exception;

public class AccountNotFoundException extends NotFoundException {

    public AccountNotFoundException(long id) {
        super("Account with id " + id + " not found");
    }
}
