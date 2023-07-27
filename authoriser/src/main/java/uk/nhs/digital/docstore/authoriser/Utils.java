package uk.nhs.digital.docstore.authoriser;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    public static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static String decodeURL(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public static String getAmplifyBaseUrl() {
        String AMPLIFY_BASE_URL_ENV_VAR = "AMPLIFY_BASE_URL";
        String workspace = System.getenv("WORKSPACE");
        String url =
                (workspace == null || workspace.isEmpty())
                        ? "https://access-request-fulfilment.patient-deductions.nhs.uk"
                        : String.format(
                                "https://%s.access-request-fulfilment.patient-deductions.nhs.uk",
                                workspace);
        if (url == null) {
            LOGGER.warn("Missing required environment variable: " + AMPLIFY_BASE_URL_ENV_VAR);
            return "__unset__AMPLIFY_BASE_URL";
        }
        LOGGER.debug("get amplify base url:" + url);
        return url;
    }
}
