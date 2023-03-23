package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;

public class ScanResultTest {

    @Test
    public void testReturnsScanResultFromStringIdentifier() {
        ScanResult scanResult = ScanResult.scanResultFromString("Not Scanned");
        assert scanResult == ScanResult.NOT_SCANNED;
    }

    @Test
    public void testReturnsUnknownIfScanResultIsSomethingElse() {
        ScanResult scanResult = ScanResult.scanResultFromString("Gremlin is a dog");
        assert scanResult == ScanResult.UNKNOWN;
    }
}
