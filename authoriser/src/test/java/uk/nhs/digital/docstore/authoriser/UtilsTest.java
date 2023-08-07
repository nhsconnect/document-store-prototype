package uk.nhs.digital.docstore.authoriser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class UtilsTest {
    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    void decodeURL() {

        var value = "%7B%22org_name%22%3A%22NHSID+DEV%22%2C%22org_code%22%3A%22A9A5A%22%7D";
        var result = Utils.decodeURL(value);
        Assertions.assertEquals("{\"org_name\":\"NHSID DEV\",\"org_code\":\"A9A5A\"}", result);
    }

    @Test
    void getAmplifyBaseUrlTestWithMissingWorkspaceEnvVariable() {
        this.environmentVariables.set("WORKSPACE", null);

        var expected = "https://access-request-fulfilment.patient-deductions.nhs.uk";

        var result = Utils.getAmplifyBaseUrl();

        Assertions.assertEquals(expected, result);
    }

    @Test
    void getAmplifyBaseUrlTestWithValidWorkspaceEnvVariable() {
        String WORKSPACE = "test";

        this.environmentVariables.set("WORKSPACE", WORKSPACE);

        var expected = "https://test.access-request-fulfilment.patient-deductions.nhs.uk";

        var result = Utils.getAmplifyBaseUrl();

        Assertions.assertEquals(expected, result);
    }
}
