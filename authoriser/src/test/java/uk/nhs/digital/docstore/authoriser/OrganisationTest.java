package uk.nhs.digital.docstore.authoriser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.models.Organisation;

class OrganisationTest {

    @Test
    void shouldDeserialise() throws JsonProcessingException {
        var mapper = new ObjectMapper();

        var organisation = new JSONObject();
        organisation.put("ods_code", "some-code");
        organisation.put("org_name", "some-name");
        organisation.put("org_type", "some-type");

        var expected = mapper.readValue(organisation.toString(), Organisation.class);

        Assertions.assertEquals(expected.getOdsCode(), "some-code");
    }

    @Disabled
    @Test
    void shouldSerialise() {}
}
