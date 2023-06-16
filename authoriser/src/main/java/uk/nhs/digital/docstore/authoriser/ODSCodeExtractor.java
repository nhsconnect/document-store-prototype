package uk.nhs.digital.docstore.authoriser;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class ODSCodeExtractor {
    public static List<String> getCodes(JSONObject userInfo) {
        ArrayList<String> codes = new ArrayList<>();
        codes.add(userInfo.getJSONArray("nhsid_user_orgs").getJSONObject(0).getString("org_code"));
        return codes;
    }
}
