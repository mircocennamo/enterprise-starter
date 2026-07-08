package it.interno.platform.starter.web.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(value = "/pong-service/")
public interface PongClient {

    @GetExchange("/api/pong")
    String getPong();
}
