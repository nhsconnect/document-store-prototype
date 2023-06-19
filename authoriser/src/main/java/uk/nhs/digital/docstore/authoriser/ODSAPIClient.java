package uk.nhs.digital.docstore.authoriser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ODSAPIClient {
    private static final String url =
            "https://directory.spineservices.nhs.uk/ORD/2-0-0/organisations/";

    public static String getOrgData(String odsCode) throws IOException {
        var requestUrl = new URL(url + odsCode);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();

        System.out.println("Response Body: " + response);

        connection.disconnect();

        return response.toString();
    }
}
