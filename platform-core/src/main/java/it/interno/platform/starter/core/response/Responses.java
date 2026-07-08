package it.interno.platform.starter.core.response;

import java.util.List;
import java.util.Map;

public class Responses {

    private Responses(){}

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                java.time.Instant.now(),
                200,
                List.of(),
                data,
                Map.of()
        );
    }


    public static ApiResponse<Void> error(String code, String message, int status) {
        return new ApiResponse<>(
                false,
                java.time.Instant.now(),
                status,
                List.of(new ApiError(code,message)),
                null,
                Map.of()
        );
    }
}
