package it.interno.platform.starter.web.exceptions;

import org.springframework.http.HttpStatus;

public class PongException extends BusinessException {

    public PongException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "503", message);
    }
}
