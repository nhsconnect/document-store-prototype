package uk.nhs.digital.docstore.authoriser;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class ODSAPIRequestClient {
    public JSONObject getResponse(String odsCode) {
        try {
            return new JSONObject(ODSAPIClient.getOrgData(odsCode));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}