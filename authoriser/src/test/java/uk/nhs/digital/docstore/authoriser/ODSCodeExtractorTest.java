package uk.nhs.digital.docstore.authoriser;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class ODSCodeExtractorTest {
    @Test
    void returnsSingularODSCode() {
        var userInfo = new JSONObject();
        userInfo.put("org_code", "A9A5A");
        var codes = ODSCodeExtractor.getCodes(userInfo);

        assert(codes.get(0)).equals("A9A5A");
    }
}
