package uk.nhs.digital.docstore.authoriser;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class ODSCodeExtractor {
    public static List<String> getCodes(JSONObject userInfo) {
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
}
