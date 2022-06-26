package de.borstelmann.doorbell.server.response.google.home;

public enum DeviceTrait {
    LOCK_UNLOCK("action.devices.traits.LockUnlock");

    private final String traitName;

    DeviceTrait(String traitName) {
        this.traitName = traitName;
    }

    @Override
    public String toString() {
        return this.traitName;
    }

    public static class LockUnlockStateKeys {
        public static final String IS_LOCKED = "isLocked";
        public static final String IS_JAMMED = "isJammed";

        private LockUnlockStateKeys() {
        }
    }
}
