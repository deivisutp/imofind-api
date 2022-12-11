package br.com.deivisutp.imofindapi.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ResourceExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StandardError> badRequestException(BadRequestException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new StandardError(HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UnathorizedException.class)
    public ResponseEntity<StandardError> unauthorizedException(UnathorizedException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new StandardError(HttpStatus.UNAUTHORIZED, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<StandardError> forbiddenException(ForbiddenException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new StandardError(HttpStatus.FORBIDDEN, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<StandardError> notFoundException(NotFoundException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new StandardError(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<StandardError> internalServerErrorException(InternalServerErrorException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new StandardError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<StandardError> runtimeException(RuntimeException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new StandardError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handlerException(Exception e, Object body, HttpStatus httpstatus,
                                                   HttpHeaders headers, WebRequest request) {
        return ResponseEntity.status(httpstatus)
                .body(new StandardError(httpstatus, e.getMessage(), request.getContextPath()));
    }
}
