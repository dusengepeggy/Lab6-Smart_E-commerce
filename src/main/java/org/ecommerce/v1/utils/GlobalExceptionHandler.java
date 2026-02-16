package org.ecommerce.v1.utils;

import com.example.e_commerce.dto.JsonResponseDto.ErrorResponse;
import com.example.e_commerce.utils.exceptions.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrityViolation(
            DataIntegrityViolationException ex) {

        String message = ex.getMostSpecificCause().getMessage();
        String userMessage;

        if (message.contains("email")) {
            userMessage = "User with this email already exists";
        } else if (message.contains("username")) {
            userMessage = "Username already exists";
        } else if (message.contains("foreign key")) {
            userMessage = "Referenced resource does not exist";
        }
        else {
            userMessage = "Invalid database operation";
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                userMessage,
                LocalDateTime.now().toString(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        Map<String,String > errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err->{
            FieldError fieldError = (FieldError) err;
            String fieldName = fieldError.getField();
            String errorMessage = err.getDefaultMessage();
            errors.put(fieldName,errorMessage);
            
        });
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "A Validation Error occured",
                LocalDateTime.now().toString(),
                errors
        );


        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler({EmptyResultDataAccessException.class,NotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now().toString(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getMessage(),
                LocalDateTime.now().toString()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
