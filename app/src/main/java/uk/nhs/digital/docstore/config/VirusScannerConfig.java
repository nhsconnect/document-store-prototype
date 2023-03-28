package uk.nhs.digital.docstore.config;

public class VirusScannerConfig {

    public static final String FALSE = "false";
    public static final String TRUE = "true";
    private final Environment environment;

    public VirusScannerConfig() {
        this(new Environment());
    }

    public VirusScannerConfig(Environment environment) {
        this.environment = environment;
    }

    public String getDocumentStoreBucketName() {
        var virusScannerIsStubbed =
                TRUE.equals(environment.getEnvVar("VIRUS_SCANNER_IS_STUBBED", FALSE));
        return virusScannerIsStubbed
                ? System.getenv("TEST_DOCUMENT_STORE_BUCKET_NAME")
                : System.getenv("DOCUMENT_STORE_BUCKET_NAME");
    }
}
