package it.interno.platform.starter.core.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;


public record ApiResponse<T> (
         boolean success,
         Instant timestamp,
         int status,
         List<ApiError> errors,
         T data,
         Map<String,Object> metadata
){}
