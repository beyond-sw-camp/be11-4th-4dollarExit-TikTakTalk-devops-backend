package com.TTT.TTT.Common.controller;

import com.TTT.TTT.Common.dtos.CommonErrorDto;
import jakarta.persistence.EntityNotFoundException;
import kotlin.io.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFound(EntityNotFoundException e) {
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgument(IllegalArgumentException e) {
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgument(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity accesDeny(AccessDeniedException e) {
        return new ResponseEntity(new CommonErrorDto(HttpStatus.FORBIDDEN.value(), e.getMessage()), HttpStatus.FORBIDDEN);
    }
}
