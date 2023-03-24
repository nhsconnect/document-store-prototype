package uk.nhs.digital.docstore.model;

import java.util.Arrays;

public enum ScanResult {
    CLEAN("Clean"),
    INFECTED("Infected"),
    NOT_SCANNED("Not Scanned"),
    SUSPICIOUS("Suspicious"),
    ERROR("Error"),
    UNSCANNABLE("Unscannable"),
    UNKNOWN("Unknown");

    private final String result;

    ScanResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return result;
    }

    public static ScanResult scanResultFromString(String result) {
        return Arrays.stream(ScanResult.values())
                .filter(scanResult -> scanResult.result.equals(result))
                .findAny()
                .orElse(UNKNOWN);
    }
}
