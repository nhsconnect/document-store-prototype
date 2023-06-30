package uk.nhs.digital.docstore.authoriser;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class JSONDataExtractor {
    public List<String> getOdsCodesFromUserInfo(JSONObject userInfo) {
        ArrayList<String> codes = new ArrayList<>();
        var orgs = userInfo.getJSONArray("nhsid_nrbac_roles");

        for (int i = 0; i < orgs.length(); i++) {
            var org = orgs.getJSONObject(i);
            var odsCode = org.getString("org_code");
            if (!codes.contains(odsCode)) {
                codes.add(odsCode);
            }
        }
        return codes;
    }

    public List<String> getGpAndPcseRolesFromOrgData(JSONObject orgData) {
        ArrayList<String> roleCodes = new ArrayList<>();
        var jsonRoles =
                orgData.getJSONObject("Organisation").getJSONObject("Roles").getJSONArray("Role");

        final var gpCode = "RO76";
        final var pcseCode = "RO157";
        final var devCode = "RO198";

        for (int i = 0; i < jsonRoles.length(); i++) {
            var jsonRole = jsonRoles.getJSONObject(i);
            var roleCode = jsonRole.getString("id");

            if (roleCode.equals(gpCode) || roleCode.equals(pcseCode) || roleCode.equals(devCode)) {
                roleCodes.add(roleCode);
            }
        }
        return roleCodes;
    }
}
