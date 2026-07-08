package it.interno.platform.starter.web.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String code;


    protected BusinessException(HttpStatusCode statusCode, String code,String message) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
    }

}
