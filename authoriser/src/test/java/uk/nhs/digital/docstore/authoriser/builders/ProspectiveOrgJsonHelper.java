package uk.nhs.digital.docstore.authoriser.builders;

import org.json.JSONObject;

public class ProspectiveOrgJsonHelper {

    final String threeOrgJsonString =
            "{\n"
                    + "  \"array\": [\n"
                    + "    {\n"
                    + "      \"org_name\": \"Town GP\",\n"
                    + "      \"ods_code\": \"A100\",\n"
                    + "      \"org_type\": \"GPP\"\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"org_name\": \"City clinic\",\n"
                    + "      \"ods_code\": \"A142\",\n"
                    + "      \"org_type\": \"GPP\"\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"org_name\": \"National care support\",\n"
                    + "      \"ods_code\": \"A410\",\n"
                    + "      \"org_type\": \"PCSE\"\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";

    public JSONObject getJsonForThreeOrgs() {
        return new JSONObject(threeOrgJsonString);
    }

    public String getJsonAsStringForThreeOrgs() {
        return threeOrgJsonString;
    }
}
