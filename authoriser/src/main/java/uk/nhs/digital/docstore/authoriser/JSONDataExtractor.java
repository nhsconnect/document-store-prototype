package uk.nhs.digital.docstore.authoriser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONObject;
import uk.nhs.digital.docstore.authoriser.enums.PermittedOrgs;
import uk.nhs.digital.docstore.authoriser.models.Organisation;

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

    public Optional<Organisation> getProspectiveOrgs(JSONObject orgData) {
        var jsonRoles =
                orgData.getJSONObject("Organisation").getJSONObject("Roles").getJSONArray("Role");

        for (int i = 0; i < jsonRoles.length(); i++) {
            var jsonRole = jsonRoles.getJSONObject(i);
            var roleCode = jsonRole.getString("id");

            if (roleCode.equals(PermittedOrgs.PCSE.roleCode)) {
                var orgName = orgData.getJSONObject("Organisation").getString("Name");
                return Optional.of(new Organisation(orgName, PermittedOrgs.PCSE.type));
            } else if (roleCode.equals(PermittedOrgs.GPP.roleCode)) {
                var orgName = orgData.getJSONObject("Organisation").getString("Name");
                return Optional.of(new Organisation(orgName, PermittedOrgs.GPP.type));
            } else if (roleCode.equals(PermittedOrgs.DEV.roleCode)) {
                var orgName = orgData.getJSONObject("Organisation").getString("Name");
                return Optional.of(new Organisation(orgName, PermittedOrgs.GPP.type));
            }
        }
        return Optional.empty();
    }
}
