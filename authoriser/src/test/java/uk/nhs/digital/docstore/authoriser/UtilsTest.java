package uk.nhs.digital.docstore.authoriser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {

    @Test
    void decodeURL() {

        var value = "%7B%22org_name%22%3A%22NHSID+DEV%22%2C%22org_code%22%3A%22A9A5A%22%7D";
        var result = Utils.decodeURL(value);
        Assertions.assertEquals("{\"org_name\":\"NHSID DEV\",\"org_code\":\"A9A5A\"}", result);
    }
}
