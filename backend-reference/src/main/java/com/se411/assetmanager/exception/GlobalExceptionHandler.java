package com.se411.assetmanager.exception;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.se411.assetmanager.dto.ErrorResponse;

/**
 * Catches exceptions thrown by any controller and turns them into a clear JSON error
 * (see ErrorResponse) with the right HTTP status code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404: the id does not exist. */
    @ExceptionHandler(ComputerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ComputerNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    /** 409: the serial number is already used. */
    @ExceptionHandler(DuplicateSerialNumberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateSerialNumberException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(),
                Map.of("serialNumber", "This serial number is already in use."));
    }

    /** 400: page or size is out of range. */
    @ExceptionHandler(InvalidPageRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadPage(InvalidPageRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    /** 400: a @Valid rule in Computer was broken. Returns one message per field. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // An empty field breaks several rules at once (@NotBlank and @Size).
            // In that case we prefer the simple "... is required." message.
            boolean isRequiredRule = "NotBlank".equals(error.getCode()) || "NotNull".equals(error.getCode());
            if (isRequiredRule || !fieldErrors.containsKey(error.getField())) {
                fieldErrors.put(error.getField(), error.getDefaultMessage());
            }
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed. Please fix the highlighted fields.", fieldErrors);
    }

    /** 400: the body is missing, is not valid JSON, or has an unknown status value. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "The request body is missing or is not valid JSON. "
                        + "Status must be one of AVAILABLE, IN_USE, IN_REPAIR, RETIRED.",
                Map.of());
    }

    /** 400: a URL value has the wrong type, e.g. /api/computers/abc or ?page=x. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Parameter '" + ex.getName() + "' must be a whole number.", Map.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Map<String, String> fieldErrors) {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message, fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
