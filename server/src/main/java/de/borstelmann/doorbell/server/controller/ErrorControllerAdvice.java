package de.borstelmann.doorbell.server.controller;

import de.borstelmann.doorbell.server.error.NotFoundException;
import de.borstelmann.doorbell.server.openapi.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorControllerAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception e) {
        ErrorResponse errorResponse = convertToErrorResponse(e);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    private ErrorResponse convertToErrorResponse(Exception e) {
        return new ErrorResponse()
                .message(e.getMessage());
    }
}
