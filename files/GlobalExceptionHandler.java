package org.example.bc_maps_system.exception;

import org.example.bc_maps_system.model.ErrorEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorEntity> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorEntity error = ErrorEntity.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorEntity> handleRuntime(RuntimeException ex) {
        ErrorEntity error = ErrorEntity.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}