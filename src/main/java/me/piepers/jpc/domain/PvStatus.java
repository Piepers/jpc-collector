package me.piepers.jpc.domain;

import java.util.Arrays;

public enum PvStatus {
    WAITING(0, "Waiting"),
    NORMAL(1, "Normal"),
    FAULT(3, "Fault"),
    UNKNOWN(-1, "Unknown");

    private final int code;
    private final String description;

    PvStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PvStatus resolve(int code) {
        return Arrays
                .stream(PvStatus.values())
                .filter(value -> value.getCode() == code)
                .findFirst()
                .orElse(UNKNOWN);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
