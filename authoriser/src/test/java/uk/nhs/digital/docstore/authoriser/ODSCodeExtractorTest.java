package uk.nhs.digital.docstore.authoriser;

import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ODSCodeExtractorTest {

    @Test
    void returnsSingularODSCode() {
        String odsCode = "A9A5A";
        String singleOrgUser =
                "{\n"
                        + "    \"uid\": \"555042709107\",\n"
                        + "    \"sub\": \"555042709107\",\n"
                        + "    \"nhsid_useruid\": \"555042709107\",\n"
                        + "    \"nhsid_nrbac_roles\": [\n"
                        + "        {\n"
                        + "            \"role_name\": \"\\\"Support\\\":\\\"Systems"
                        + " Support\\\":\\\"Systems Support Access Role\\\"\",\n"
                        + "            \"activities\": [\n"
                        + "                \"View Sensitive Results\",\n"
                        + "                \"View Detailed Health Records\",\n"
                        + "                \"Perform Clinical Coding\"\n"
                        + "            ],\n"
                        + "            \"person_orgid\": \"555042719109\",\n"
                        + "            \"person_roleid\": \"555042720102\",\n"
                        + "            \"role_code\": \"S8001:G8005:R8015\",\n"
                        + "            \"activity_codes\": [\n"
                        + "                \"B1666\",\n"
                        + "                \"B0360\",\n"
                        + "                \"B0790\"\n"
                        + "            ],\n"
                        + "            \"org_code\": \""
                        + odsCode
                        + "\"\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"nhsid_user_orgs\": [\n"
                        + "        {\n"
                        + "            \"org_name\": \"NHSID DEV\",\n"
                        + "            \"org_code\": \""
                        + odsCode
                        + "\"\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"name\": \"TestUserOne Caius Mr\",\n"
                        + "    \"given_name\": \"Caius\",\n"
                        + "    \"family_name\": \"TestUserOne\"\n"
                        + "}";
        var userInfo = new JSONObject(singleOrgUser);

        var codes = ODSCodeExtractor.getCodes(userInfo);

        assert (codes.get(0)).equals(odsCode);
    }

    @Test
    @Disabled
    void returnsMultipleODSCodes() {
        String odsCode1 = "A9A5A";
        String odsCode2 = "B1B1B";
        String multipleOrgUser =
                "{\n"
                        + "    \"nhsid_useruid\": \"910000000001\",\n"
                        + "    \"name\": \"USERQ RANDOM Mr\",\n"
                        + "    \"nhsid_nrbac_roles\": [\n"
                        + "        {\n"
                        + "            \"org_code\": \""
                        + odsCode1
                        + "\",\n"
                        + "            \"person_orgid\": \"555254239107\",\n"
                        + "            \"person_roleid\": \"555254240100\",\n"
                        + "            \"role_code\": \"S8000:G8000:R8001\",\n"
                        + "            \"role_name\": \"\\\"Clinical\\\":\\\"Clinical"
                        + " Provision\\\":\\\"Nurse Access Role\\\"\"\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"org_code\": \"RBA\",\n"
                        + "            \"person_orgid\": \"555254239107\",\n"
                        + "            \"person_roleid\": \"555254242102\",\n"
                        + "            \"role_code\": \"S8000:G8000:R8000\",\n"
                        + "            \"role_name\": \"\\\"Clinical\\\":\\\"Clinical"
                        + " Provision\\\":\\\"Clinical Practitioner Access Role\\\"\"\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"org_code\": \""
                        + odsCode2
                        + "\",\n"
                        + "            \"person_orgid\": \"555254239107\",\n"
                        + "            \"person_roleid\": \"555254241101\",\n"
                        + "            \"role_code\": \"S8000:G8000:R8003\",\n"
                        + "            \"role_name\": \"\\\"Clinical\\\":\\\"Clinical"
                        + " Provision\\\":\\\"Health Professional Access Role\\\"\"\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}";
        var userInfo = new JSONObject(multipleOrgUser);

        var codes = ODSCodeExtractor.getCodes(userInfo);

        assert (codes.get(0)).equals(odsCode1);
        assert (codes.get(2)).equals(odsCode2);
    }
}
