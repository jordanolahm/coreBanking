package com.example.coreBanking.exception;

public class AccountAlreadyExistException extends RuntimeException {
    public AccountAlreadyExistException(String message) { super(message); }
}


