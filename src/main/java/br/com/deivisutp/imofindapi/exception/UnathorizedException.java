package br.com.deivisutp.imofindapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnathorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnathorizedException() {
        super();
    }

    public UnathorizedException(String message) {
        super(message);
    }

    public UnathorizedException(Throwable cause) {
        super(cause);
    }

    public UnathorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
