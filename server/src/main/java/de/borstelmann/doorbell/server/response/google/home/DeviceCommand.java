package de.borstelmann.doorbell.server.response.google.home;

import java.util.Arrays;

public enum DeviceCommand {

    LOCK_COMMAND("action.devices.commands.LockUnlock");

    private final String commandName;

    DeviceCommand(String commandName) {
        this.commandName = commandName;
    }

    public static DeviceCommand fromCommandName(String commandName) {
        return Arrays.stream(DeviceCommand.values())
                .filter(deviceCommand -> deviceCommand.commandName.equalsIgnoreCase(commandName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return commandName;
    }

    public static class LockCommandParamsKeys {
        public static final String LOCK = "lock";
    }

}
