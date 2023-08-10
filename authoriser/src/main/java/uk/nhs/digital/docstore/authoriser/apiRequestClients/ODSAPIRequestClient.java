package uk.nhs.digital.docstore.authoriser.apiRequestClients;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ODSAPIRequestClient {
    public JSONObject getResponse(String odsCode) {
        var odsClient = new ODSAPIClient();
        try {
            return odsClient.getOrgData(odsCode);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
