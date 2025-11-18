package com.cooknect.recipe_service.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String msg) { super(msg); }
}
