package uk.nhs.digital.docstore.authoriser;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ODSAPIClientTest {
    @Test
    void returnsExpectedResponse() throws IOException {
        var odsClient = new ODSAPIClient();
        var response = odsClient.getOrgData("X26");

        assert (response.toString().contains("X26"));
        assert (response.toString().contains("PrimaryRoleId"));
    }

    @Test
    void throwsErrorIfResponseCodeIsNot200() {
        var odsClient = new ODSAPIClient();
        assertThrows(IOException.class, () -> odsClient.getOrgData(""));
    }
}
