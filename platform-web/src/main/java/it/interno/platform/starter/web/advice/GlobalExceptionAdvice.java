package it.interno.platform.starter.web.advice;


import it.interno.platform.starter.web.exceptions.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionAdvice {

    // ✅ 404 - Resource not found
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleNotFound(BusinessException ex, HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(ex.getStatusCode());

        problem.setTitle("BusinessException");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("traceparent", MDC.get("traceparent"));
        problem.setProperty("errorCode", ex.getCode());

        return problem;
    }

    // ✅ 400 - Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Validation error");
        problem.setDetail("Invalid request payload");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );

        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "VALIDATION_ERROR");

        return problem;
    }

    // ✅ 500 - Generic error
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problem.setTitle("Internal server error");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "INTERNAL_ERROR");

        return problem;
    }
}

