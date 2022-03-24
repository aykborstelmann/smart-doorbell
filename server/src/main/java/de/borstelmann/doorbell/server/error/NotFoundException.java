package de.borstelmann.doorbell.server.error;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.ResponseBody;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    @ResponseBody
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @NotNull
    public static NotFoundException createUserNotFoundException(long userId) {
        return new NotFoundException(String.format("User with ID %d not found", userId));
    }
}
