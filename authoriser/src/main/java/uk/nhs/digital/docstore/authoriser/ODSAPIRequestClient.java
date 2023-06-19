package uk.nhs.digital.docstore.authoriser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
