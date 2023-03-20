package uk.nhs.digital.docstore.model;

public enum ScanResult {
    CLEAN("CLEAN"),
    INFECTED("INFECTED"),
    NOT_SCANNED("NOT_SCANNED");

    private final String result;

    ScanResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return result;
    }
}
