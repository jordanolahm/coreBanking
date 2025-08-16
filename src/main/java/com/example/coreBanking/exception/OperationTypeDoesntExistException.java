package com.example.coreBanking.exception;

public class OperationTypeDoesntExistException extends RuntimeException {
    public OperationTypeDoesntExistException(String message) {
        super(message);
    }
}


