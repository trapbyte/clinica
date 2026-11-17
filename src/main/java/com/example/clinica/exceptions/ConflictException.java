package com.example.clinica.exceptions;

// 409 - duplicados y choques de agenda (RN4, RN5)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
