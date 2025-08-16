package com.example.coreBanking.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) { super(message); }
}


