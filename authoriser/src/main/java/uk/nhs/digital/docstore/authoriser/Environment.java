package uk.nhs.digital.docstore.authoriser;

public class Environment {
    public static String get(String COGNITO_KEY_ID) {
        return System.getenv(COGNITO_KEY_ID);
    }
}
