package uk.nhs.digital.docstore.authoriser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void shouldSerialise() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var expected =
                new JSONObject(
                        "{\"org_name\": \"Test name\",\n"
                                + "    \"ods_code\": \"odsCode\",\n"
                                + "    \"org_type\": \"GP Practice\"}");

        var org = new Organisation("odsCode", "Test name", "GP Practice");
        var actual = new JSONObject(mapper.writeValueAsString(org));

        assert (expected.similar(actual));
    }
}
