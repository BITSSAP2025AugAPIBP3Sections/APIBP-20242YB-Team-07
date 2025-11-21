package com.cooknect.gateway_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

@ControllerAdvice
public class Exception {
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> handleResourceAccessException(ResourceAccessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Downstream service unavailable: " + ex.getMessage());
    }
}
