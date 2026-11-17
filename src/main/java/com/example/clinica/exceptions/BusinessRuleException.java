package com.example.clinica.exceptions;

// 422 - la petición es válida pero viola una regla de negocio
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
