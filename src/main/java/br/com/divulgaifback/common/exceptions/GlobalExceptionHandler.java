package br.com.divulgaifback.common.exceptions;

import br.com.divulgaifback.common.exceptions.custom.DuplicateException;
import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.common.exceptions.custom.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.Instant;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<CustomException> resourceNotFound(RuntimeException e, HttpServletRequest request) {
        var exception = CustomException.builder()
                .status(HttpStatus.NOT_FOUND)
                .timestamp(Instant.now())
                .error(e.getMessage())
                .path(request.getRequestURI())
                .build();

        log.error("NotFoundException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception);
    }

    @ExceptionHandler(DuplicateException.class)
    protected ResponseEntity<CustomException> duplicatedResource(RuntimeException e, HttpServletRequest request) {
        var exception = CustomException.builder()
                .status(HttpStatus.CONFLICT)
                .timestamp(Instant.now())
                .error(e.getMessage())
                .path(request.getRequestURI())
                .build();

        log.error("DuplicateException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception);
    }

    @ExceptionHandler(ValidationException.class)
    protected ResponseEntity<CustomException> validationImpediment(RuntimeException e, HttpServletRequest request) {
        var exception = CustomException.builder()
                .status(HttpStatus.PRECONDITION_FAILED)
                .timestamp(Instant.now())
                .error(e.getMessage())
                .path(request.getRequestURI())
                .build();

        log.error("ValidationException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CustomException> invalidArgument(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation failed");

        var exception = CustomException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(Instant.now())
                .error(errorMessage)
                .path(request.getRequestURI())
                .build();

        log.error("ValidationException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<CustomException> invalidJsonFormat(HttpMessageNotReadableException e, HttpServletRequest request) {
        var exception = CustomException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(Instant.now())
                .error("Invalid JSON format")
                .path(request.getRequestURI())
                .build();

        log.error("HttpMessageNotReadableException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception);
    }

    public void sendErrorResponse(HttpServletResponse response, HttpStatus status, String error, String path)
            throws IOException {
        response.setStatus(status.value());
        CustomException customException = CustomException.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .path(path)
                .build();
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), customException);
    }
}
