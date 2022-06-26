package de.borstelmann.doorbell.server.error;

public class ForbiddenException extends RuntimeException {
    private final long id;

    public ForbiddenException(long id) {
        super("Current user does not have access to %d".formatted(id));
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
