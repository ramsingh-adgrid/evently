package com.evently.evt_bff.client;

import com.evently.evt_bff.exception.BadRequestException;
import com.evently.evt_bff.exception.DuplicateResourceException;
import com.evently.evt_bff.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String message = "Downstream service error";
        try {
            if (response.body() != null) {
                try (InputStream inputStream = response.body().asInputStream()) {
                    Map<?, ?> map = objectMapper.readValue(inputStream, Map.class);
                    if (map != null && map.get("message") != null) {
                        message = map.get("message").toString();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Feign error response body", e);
        }

        log.error("Feign request failed: status={}, message={}", response.status(), message);

        return switch (response.status()) {
            case 400 -> new BadRequestException(message);
            case 404 -> new ResourceNotFoundException(message);
            case 409 -> new DuplicateResourceException(message);
            default -> new RuntimeException(message);
        };
    }
}
