package br.com.divulgaifback.common.exceptions;

import br.com.divulgaifback.common.exceptions.custom.DuplicateException;
import br.com.divulgaifback.common.exceptions.custom.ForbiddenException;
import br.com.divulgaifback.common.exceptions.custom.EmailException;
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
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

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

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<CustomException> forbiddenResource(RuntimeException e, HttpServletRequest request) {
        var exception = CustomException.builder()
                .status(HttpStatus.FORBIDDEN)
                .timestamp(Instant.now())
                .error(e.getMessage())
                .path(request.getRequestURI())
                .build();

        log.error("ForbiddenException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception);
    }

    @ExceptionHandler(ValidationException.class)
    protected ResponseEntity<CustomException> validationImpediment(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        var exception = CustomException.builder()
                .status(HttpStatus.PRECONDITION_FAILED)
                .timestamp(Instant.now())
                .error(String.join(", ", errorMessages))
                .path(request.getRequestURI())
                .build();

        log.error("ValidationException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CustomException> invalidArgument(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        var exception = CustomException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(Instant.now())
                .error(String.join(", ", errorMessages))
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

    @ExceptionHandler(DateTimeParseException.class)
    protected ResponseEntity<CustomException> invalidParams(HttpMessageNotReadableException e, HttpServletRequest request) {
        var exception = CustomException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(Instant.now())
                .error("Invalid parameters in request")
                .path(request.getRequestURI())
                .build();

        log.error("DateTimeParseException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<CustomException> unsupportedMedia(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        var exception = CustomException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(Instant.now())
                .error("Unsupported media request sent as content")
                .path(request.getRequestURI())
                .build();

        log.error("HttpMediaTypeNotSupportedException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception);
    }

    @ExceptionHandler(EmailException.class)
    protected ResponseEntity<CustomException> failedToSendEmail(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        var exception = CustomException.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(Instant.now())
                .error("Failed to send email")
                .path(request.getRequestURI())
                .build();

        log.error("EmailException: {} - Path: {}", exception.getError(), exception.getPath());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception);
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
