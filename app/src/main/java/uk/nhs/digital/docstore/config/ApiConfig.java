package uk.nhs.digital.docstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApiConfig.class);
    public static final String AMPLIFY_BASE_URL_ENV_VAR = "AMPLIFY_BASE_URL";

    public static String getAmplifyBaseUrl() {
        String url = System.getenv(AMPLIFY_BASE_URL_ENV_VAR);
        if (url == null) {
            logger.warn("Missing required environment variable: " + AMPLIFY_BASE_URL_ENV_VAR);
            return "__unset__AMPLIFY_BASE_URL";
        }
        return url;
    }
}
