package com.srg.smartexpenseapi.exception;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> globalExceptionHandler(Exception ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", new Date());
    body.put("message", ex.getMessage());
    body.put("description", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> 
      errors.put(error.getField(), error.getDefaultMessage()));
    
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }
}
