package uk.nhs.digital.docstore.authoriser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.handlers.TokenRequestHandler;

public class ODSAPIClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(TokenRequestHandler.class);

    private static final String url =
            "https://directory.spineservices.nhs.uk/ORD/2-0-0/organisations/";

    public JSONObject getOrgData(String odsCode) throws IOException {
        var requestUrl = new URL(url + odsCode);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode != 200) {
            LOGGER.error("Response code: " + responseCode + " problem retrieving ODS code");
            throw new IOException(connection.getResponseMessage());
        }

        var response = getResponse(connection);

        connection.disconnect();

        return response;
    }

    private static JSONObject getResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        return new JSONObject(response.toString());
    }
}
