package de.borstelmann.doorbell.server.error;

import org.jetbrains.annotations.NotNull;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    @NotNull
    public static BadRequestException createDoorbellDoesNotBelongToUser(long userId, long doorbellId) {
        return new BadRequestException(String.format("Doorbell with ID %d does not belong to user with ID %d", doorbellId, userId));
    }

}
