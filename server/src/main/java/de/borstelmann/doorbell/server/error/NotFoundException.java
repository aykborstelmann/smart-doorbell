package de.borstelmann.doorbell.server.error;

import org.jetbrains.annotations.NotNull;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    @NotNull
    public static NotFoundException createUserNotFoundException(long userId) {
        return new NotFoundException(String.format("User with ID %d not found", userId));
    }

    @NotNull
    public static NotFoundException createDoorbellNotFoundException(long doorbellId) {
        return new NotFoundException(String.format("Doorbell with ID %d not found", doorbellId));
    }
}
