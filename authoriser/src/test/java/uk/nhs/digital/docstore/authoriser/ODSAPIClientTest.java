package uk.nhs.digital.docstore.authoriser;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ODSAPIClientTest {
    @Test
    void returnsExpectedResponse() throws IOException {
        var response = ODSAPIClient.getOrgData("X26");

        assert (response.contains("PrimaryRoleId"));
        assert (response.contains("X26"));
    }

    @Test
    void throwsErrorIfResponseCodeIsNot200() {
        assertThrows(IOException.class, () -> ODSAPIClient.getOrgData(""));
    }
}
