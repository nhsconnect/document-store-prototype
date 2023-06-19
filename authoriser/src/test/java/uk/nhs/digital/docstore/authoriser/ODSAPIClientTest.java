package uk.nhs.digital.docstore.authoriser;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ODSAPIClientTest {
    @Test
    void returnsExpectedResponse() throws IOException {
        var response = ODSAPIClient.getOrgData("X26");

        assert (response.contains("PrimaryRoleId"));
        assert (response.contains("X26"));
    }
}
