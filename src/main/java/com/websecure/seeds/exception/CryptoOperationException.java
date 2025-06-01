package com.websecure.seeds.exception;

public class CryptoOperationException extends Exception {

    public CryptoOperationException(String message) {
        super(message);
    }

    public CryptoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
