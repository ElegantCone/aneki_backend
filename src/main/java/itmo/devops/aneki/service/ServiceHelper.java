package itmo.devops.aneki.service;

import itmo.devops.aneki.error.ApiException;
import org.springframework.http.HttpStatus;

public class ServiceHelper {

    protected void requireNonBlank(String value, String field, boolean spacesAllowed) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Field '" + field + "' is required");
        }
        if (!spacesAllowed && value.contains(" ")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Field '" + field + "' is not valid. Spaces not allowed");
        }
    }
}
