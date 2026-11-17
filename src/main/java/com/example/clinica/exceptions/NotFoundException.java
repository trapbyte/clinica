package com.example.clinica.exceptions;

// 404 - el recurso solicitado no existe
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
